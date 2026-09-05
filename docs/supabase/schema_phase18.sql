-- Close by — Supabase schema additions (Phase 18: Server-side notifications)
-- Apply after schema_phase17.sql.
--
-- PRODUCTION APPLY (Supabase SQL Editor):
--   1. Backup the database.
--   2. Run this entire file in one transaction if your tooling supports it.
--   3. Verify: SELECT proname FROM pg_proc WHERE proname = 'create_notification_for_user';
--   4. Verify: SELECT tgname FROM pg_trigger WHERE tgname LIKE '%notify%';
--   5. Test as non-admin user: INSERT into notifications for another user_id must fail.
--
-- Trusted notification delivery: cross-user events are created by SECURITY DEFINER
-- triggers. Clients may only insert notifications for themselves (auth.uid()).
-- The internal helper is NOT granted to authenticated roles.

-- ---------------------------------------------------------------------------
-- Internal notification helper (trigger-only; not callable by clients)
-- ---------------------------------------------------------------------------
create or replace function public.create_notification_for_user(
    p_user_id uuid,
    p_type text,
    p_title text,
    p_body text,
    p_reference_type text default null,
    p_reference_id uuid default null
) returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
    v_id uuid;
begin
    if p_user_id is null then
        raise exception 'user_id is required';
    end if;
    if p_type is null or length(trim(p_type)) = 0 then
        raise exception 'type is required';
    end if;
    insert into public.notifications (
        user_id, type, title, body, reference_type, reference_id
    ) values (
        p_user_id, p_type, p_title, p_body, p_reference_type, p_reference_id
    )
    returning id into v_id;
    return v_id;
end;
$$;

revoke all on function public.create_notification_for_user(uuid, text, text, text, text, uuid) from public;
revoke all on function public.create_notification_for_user(uuid, text, text, text, text, uuid) from authenticated;
revoke all on function public.create_notification_for_user(uuid, text, text, text, text, uuid) from anon;

-- ---------------------------------------------------------------------------
-- Tighten notifications INSERT: self only (cross-user via triggers)
-- ---------------------------------------------------------------------------
drop policy if exists notifications_insert on public.notifications;
create policy notifications_insert on public.notifications for insert
    with check (user_id = auth.uid());

-- ---------------------------------------------------------------------------
-- Service request lifecycle
-- ---------------------------------------------------------------------------
create or replace function public.trg_service_request_notify_insert()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_provider_user_id uuid;
begin
    select user_id into v_provider_user_id
    from public.providers
    where id = new.provider_id;

    if v_provider_user_id is not null then
        perform public.create_notification_for_user(
            v_provider_user_id,
            'NEW_PROVIDER_REQUEST',
            'New service request',
            coalesce(nullif(trim(new.note), ''), 'You have a new service request.'),
            'REQUEST',
            new.id
        );
    end if;
    return new;
end;
$$;

drop trigger if exists service_request_notify_insert on public.service_requests;
create trigger service_request_notify_insert
    after insert on public.service_requests
    for each row execute function public.trg_service_request_notify_insert();

create or replace function public.trg_service_request_notify_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_provider_user_id uuid;
    v_title text;
    v_type text;
begin
    if old.status is not distinct from new.status then
        return new;
    end if;

    v_type := case new.status
        when 'ACCEPTED' then 'REQUEST_ACCEPTED'
        when 'REJECTED' then 'REQUEST_REJECTED'
        when 'COMPLETED' then 'REQUEST_COMPLETED'
        when 'CANCELLED' then 'REQUEST_CANCELLED'
        else null
    end;

    if v_type is null then
        return new;
    end if;

    v_title := case new.status
        when 'ACCEPTED' then 'Request accepted'
        when 'REJECTED' then 'Request rejected'
        when 'COMPLETED' then 'Request completed'
        when 'CANCELLED' then 'Request cancelled'
        else 'Request updated'
    end;

    if new.status = 'CANCELLED' then
        select user_id into v_provider_user_id
        from public.providers
        where id = new.provider_id;

        if v_provider_user_id is not null then
            perform public.create_notification_for_user(
                v_provider_user_id,
                v_type,
                v_title,
                'A customer cancelled a service request.',
                'REQUEST',
                new.id
            );
        end if;
    elsif new.customer_id is not null then
        perform public.create_notification_for_user(
            new.customer_id,
            v_type,
            v_title,
            'Your service request status changed.',
            'REQUEST',
            new.id
        );
    end if;

    return new;
end;
$$;

drop trigger if exists service_request_notify_update on public.service_requests;
create trigger service_request_notify_update
    after update on public.service_requests
    for each row execute function public.trg_service_request_notify_update();

-- ---------------------------------------------------------------------------
-- Reviews → notify reviewee
-- ---------------------------------------------------------------------------
create or replace function public.trg_review_notify_insert()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if new.reviewee_id is not null then
        perform public.create_notification_for_user(
            new.reviewee_id,
            'REVIEW_RECEIVED',
            'New review received',
            'Someone left a review on your completed request.',
            'REVIEW',
            new.request_id
        );
    end if;
    return new;
end;
$$;

drop trigger if exists review_notify_insert on public.reviews;
create trigger review_notify_insert
    after insert on public.reviews
    for each row execute function public.trg_review_notify_insert();

-- ---------------------------------------------------------------------------
-- Provider verification status → notify provider owner
-- ---------------------------------------------------------------------------
create or replace function public.trg_provider_verification_notify_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_type text;
    v_title text;
    v_body text;
begin
    if old.verification_status is not distinct from new.verification_status then
        return new;
    end if;

    v_type := case new.verification_status
        when 'APPROVED' then 'VERIFICATION_APPROVED'
        when 'REJECTED' then 'VERIFICATION_REJECTED'
        when 'SUSPENDED' then 'VERIFICATION_SUSPENDED'
        else null
    end;

    if v_type is null or new.user_id is null then
        return new;
    end if;

    v_title := case new.verification_status
        when 'APPROVED' then 'Verification approved'
        when 'REJECTED' then 'Verification rejected'
        when 'SUSPENDED' then 'Verification suspended'
        else 'Verification updated'
    end;

    v_body := coalesce(
        nullif(trim(new.verification_note), ''),
        case new.verification_status
            when 'APPROVED' then 'Your provider profile is now verified.'
            when 'REJECTED' then 'Your verification request was not approved.'
            when 'SUSPENDED' then 'Your verification status was suspended.'
            else 'Your verification status changed.'
        end
    );

    perform public.create_notification_for_user(
        new.user_id,
        v_type,
        v_title,
        v_body,
        'VERIFICATION',
        new.id
    );

    return new;
end;
$$;

drop trigger if exists provider_verification_notify_update on public.providers;
create trigger provider_verification_notify_update
    after update of verification_status on public.providers
    for each row execute function public.trg_provider_verification_notify_update();

-- ---------------------------------------------------------------------------
-- Advertisement status → notify owner
-- ---------------------------------------------------------------------------
create or replace function public.trg_advertisement_notify_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_type text;
    v_title text;
    v_body text;
begin
    if old.status is not distinct from new.status then
        return new;
    end if;

    v_type := case new.status
        when 'APPROVED' then 'AD_APPROVED'
        when 'REJECTED' then 'AD_REJECTED'
        when 'PAUSED' then 'AD_PAUSED'
        when 'EXPIRED' then 'AD_PAUSED'
        else null
    end;

    if v_type is null then
        return new;
    end if;

    v_title := case new.status
        when 'APPROVED' then 'Advertisement approved'
        when 'REJECTED' then 'Advertisement rejected'
        when 'PAUSED' then 'Advertisement paused'
        when 'EXPIRED' then 'Advertisement expired'
        else 'Advertisement updated'
    end;

    v_body := coalesce(
        nullif(trim(new.rejection_reason), ''),
        case new.status
            when 'APPROVED' then 'Your advertisement is now active.'
            when 'REJECTED' then 'Your advertisement was not approved.'
            when 'PAUSED' then 'Your advertisement has been paused.'
            when 'EXPIRED' then 'Your advertisement has expired.'
            else 'Your advertisement status changed.'
        end
    );

    perform public.create_notification_for_user(
        new.owner_id,
        v_type,
        v_title,
        v_body,
        'AD',
        new.id
    );

    return new;
end;
$$;

drop trigger if exists advertisement_notify_update on public.advertisements;
create trigger advertisement_notify_update
    after update of status on public.advertisements
    for each row execute function public.trg_advertisement_notify_update();

-- ---------------------------------------------------------------------------
-- Report status → notify reporter
-- ---------------------------------------------------------------------------
create or replace function public.trg_report_notify_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if old.status is not distinct from new.status then
        return new;
    end if;

    perform public.create_notification_for_user(
        new.reporter_id,
        'REPORT_STATUS_UPDATED',
        'Report update',
        'Your report status is now: ' || new.status,
        'REPORT',
        new.id
    );

    return new;
end;
$$;

drop trigger if exists report_notify_update on public.reports;
create trigger report_notify_update
    after update of status on public.reports
    for each row execute function public.trg_report_notify_update();

-- ---------------------------------------------------------------------------
-- Account deletion request status → notify requester
-- ---------------------------------------------------------------------------
create or replace function public.trg_account_deletion_notify_update()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if old.status is not distinct from new.status then
        return new;
    end if;

    perform public.create_notification_for_user(
        new.user_id,
        'ACCOUNT_DELETION_REQUESTED',
        'Account deletion update',
        'Your account deletion request status is now: ' || new.status,
        'ACCOUNT',
        null
    );

    return new;
end;
$$;

drop trigger if exists account_deletion_notify_update on public.account_deletion_requests;
create trigger account_deletion_notify_update
    after update of status on public.account_deletion_requests
    for each row execute function public.trg_account_deletion_notify_update();

-- Optional dashboard count (extends Phase 8 view)
create or replace view public.admin_dashboard_stats as
select
    (select count(*) from auth.users) as total_users,
    (select count(*) from public.providers where is_active = true) as total_providers,
    (select count(*) from public.services where is_active = true and deleted_at is null) as active_services,
    (select count(*) from public.provider_verification_submissions where status = 'PENDING') as pending_verifications,
    (select count(*) from public.advertisements where status = 'PENDING') as pending_advertisements,
    (select count(*) from public.reports where status = 'OPEN') as open_reports,
    (select count(*) from public.account_deletion_requests where status = 'PENDING') as pending_deletion_requests;

comment on function public.create_notification_for_user is
    'Internal trigger helper — not granted to clients. Use self-insert RLS for own notifications.';
