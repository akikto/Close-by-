-- Close by — Supabase schema additions (Phase 18: Server-side notifications)
-- Apply after schema_phase17.sql.
--
-- Replaces fragile client-side cross-user notification inserts with trusted
-- database logic. Clients may still insert self-notifications; cross-user
-- events are created by triggers or the SECURITY DEFINER RPC.

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
grant execute on function public.create_notification_for_user(uuid, text, text, text, text, uuid) to authenticated;

-- Notify provider when a new request is created
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
            coalesce(new.note, 'You have a new service request.'),
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

-- Notify customer when provider updates request status
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

comment on function public.create_notification_for_user is
    'Trusted server-side notification insert for cross-user events';
