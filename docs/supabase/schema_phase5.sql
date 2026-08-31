-- Close by — Supabase schema additions (Phase 5: Trust & Safety)
-- Apply after schema_phase4.sql. Idempotent where possible.

-- ---------------------------------------------------------------------------
-- Admin role helper (server-side only; never expose service role to Android)
-- ---------------------------------------------------------------------------
create table if not exists public.user_profiles (
    user_id uuid primary key references auth.users (id) on delete cascade,
    display_name text,
    is_admin boolean not null default false,
    customer_rating double precision not null default 0 check (customer_rating >= 0 and customer_rating <= 5),
    customer_review_count integer not null default 0 check (customer_review_count >= 0),
    is_suspended boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select coalesce(
        (select is_admin from public.user_profiles where user_id = auth.uid()),
        false
    );
$$;

-- ---------------------------------------------------------------------------
-- Provider verification
-- ---------------------------------------------------------------------------
alter table public.providers
    add column if not exists verification_status text not null default 'NOT_SUBMITTED'
        check (verification_status in ('NOT_SUBMITTED', 'PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED'));

alter table public.providers
    add column if not exists verification_note text;

alter table public.providers
    add column if not exists is_suspended boolean not null default false;

-- Sync legacy is_verified with verification_status
update public.providers
set is_verified = (verification_status = 'APPROVED')
where verification_status is not null;

create table if not exists public.provider_verification_submissions (
    id uuid primary key default gen_random_uuid(),
    provider_id uuid not null references public.providers (id) on delete cascade,
    submitted_by uuid not null references auth.users (id) on delete cascade,
    business_name text not null,
    contact_phone text not null,
    description text,
    document_url text,
    status text not null default 'PENDING'
        check (status in ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    admin_note text,
    reviewed_by uuid references auth.users (id) on delete set null,
    reviewed_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists provider_verification_provider_idx
    on public.provider_verification_submissions (provider_id);
create index if not exists provider_verification_status_idx
    on public.provider_verification_submissions (status);

-- ---------------------------------------------------------------------------
-- Reviews (two-way rating after completed request)
-- ---------------------------------------------------------------------------
create table if not exists public.reviews (
    id uuid primary key default gen_random_uuid(),
    request_id uuid not null references public.service_requests (id) on delete cascade,
    service_id uuid not null references public.services (id) on delete cascade,
    provider_id uuid not null references public.providers (id) on delete cascade,
    customer_id uuid references auth.users (id) on delete set null,
    reviewer_id uuid not null,
    reviewee_id uuid not null,
    reviewer_role text not null check (reviewer_role in ('CUSTOMER', 'PROVIDER')),
    overall_rating smallint not null check (overall_rating between 1 and 5),
    service_quality smallint check (service_quality is null or service_quality between 1 and 5),
    behaviour smallint check (behaviour is null or behaviour between 1 and 5),
    reliability smallint check (reliability is null or reliability between 1 and 5),
    professionalism smallint check (professionalism is null or professionalism between 1 and 5),
    comment text,
    moderation_status text not null default 'VISIBLE'
        check (moderation_status in ('VISIBLE', 'HIDDEN', 'PENDING')),
    is_visible boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (request_id, reviewer_id, reviewer_role)
);

create index if not exists reviews_provider_idx on public.reviews (provider_id);
create index if not exists reviews_reviewee_idx on public.reviews (reviewee_id);
create index if not exists reviews_request_idx on public.reviews (request_id);

-- Recompute provider rating aggregates
create or replace function public.refresh_provider_rating_stats(p_provider_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    avg_rating double precision;
    cnt integer;
begin
    select coalesce(avg(overall_rating::double precision), 0), count(*)
    into avg_rating, cnt
    from public.reviews
    where provider_id = p_provider_id
      and reviewer_role = 'CUSTOMER'
      and is_visible = true
      and moderation_status = 'VISIBLE';

    update public.providers
    set rating = round(avg_rating::numeric, 2),
        review_count = cnt,
        updated_at = now()
    where id = p_provider_id;
end;
$$;

create or replace function public.trg_reviews_refresh_provider_rating()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    perform public.refresh_provider_rating_stats(
        coalesce(new.provider_id, old.provider_id)
    );
    return coalesce(new, old);
end;
$$;

drop trigger if exists reviews_refresh_provider_rating on public.reviews;
create trigger reviews_refresh_provider_rating
    after insert or update or delete on public.reviews
    for each row execute function public.trg_reviews_refresh_provider_rating();

-- ---------------------------------------------------------------------------
-- Reports
-- ---------------------------------------------------------------------------
create table if not exists public.reports (
    id uuid primary key default gen_random_uuid(),
    reporter_id uuid not null references auth.users (id) on delete cascade,
    target_type text not null
        check (target_type in ('PROVIDER', 'SERVICE', 'REVIEW', 'ADVERTISEMENT', 'USER')),
    target_id uuid not null,
    reason text not null
        check (reason in (
            'FAKE_LISTING', 'WRONG_INFO', 'ABUSE', 'SCAM', 'INAPPROPRIATE',
            'WRONG_LOCATION', 'OTHER'
        )),
    description text,
    status text not null default 'OPEN'
        check (status in ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED')),
    resolved_by uuid references auth.users (id) on delete set null,
    resolved_at timestamptz,
    moderation_note text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists reports_status_idx on public.reports (status);
create index if not exists reports_reporter_idx on public.reports (reporter_id);
create index if not exists reports_target_idx on public.reports (target_type, target_id);

-- ---------------------------------------------------------------------------
-- User blocks
-- ---------------------------------------------------------------------------
create table if not exists public.user_blocks (
    id uuid primary key default gen_random_uuid(),
    blocker_id uuid not null references auth.users (id) on delete cascade,
    blocked_provider_id uuid references public.providers (id) on delete cascade,
    blocked_user_id uuid references auth.users (id) on delete cascade,
    created_at timestamptz not null default now(),
    check (blocked_provider_id is not null or blocked_user_id is not null)
);

create unique index if not exists user_blocks_provider_unique
    on public.user_blocks (blocker_id, blocked_provider_id)
    where blocked_provider_id is not null;

-- ---------------------------------------------------------------------------
-- Admin audit log
-- ---------------------------------------------------------------------------
create table if not exists public.admin_audit_logs (
    id uuid primary key default gen_random_uuid(),
    admin_id uuid not null references auth.users (id) on delete cascade,
    action text not null,
    target_type text not null,
    target_id uuid not null,
    reason text,
    created_at timestamptz not null default now()
);

create index if not exists admin_audit_logs_admin_idx on public.admin_audit_logs (admin_id);
create index if not exists admin_audit_logs_target_idx on public.admin_audit_logs (target_type, target_id);

-- ---------------------------------------------------------------------------
-- RLS
-- ---------------------------------------------------------------------------
alter table public.user_profiles enable row level security;
alter table public.provider_verification_submissions enable row level security;
alter table public.reviews enable row level security;
alter table public.reports enable row level security;
alter table public.user_blocks enable row level security;
alter table public.admin_audit_logs enable row level security;

-- user_profiles
drop policy if exists user_profiles_select on public.user_profiles;
create policy user_profiles_select on public.user_profiles for select
    using (user_id = auth.uid() or public.is_admin());

drop policy if exists user_profiles_insert on public.user_profiles;
create policy user_profiles_insert on public.user_profiles for insert
    with check (user_id = auth.uid());

drop policy if exists user_profiles_update on public.user_profiles;
create policy user_profiles_update on public.user_profiles for update
    using (user_id = auth.uid() or public.is_admin());

-- provider verification
drop policy if exists verification_select on public.provider_verification_submissions;
create policy verification_select on public.provider_verification_submissions for select
    using (
        public.is_admin()
        or submitted_by = auth.uid()
        or exists (
            select 1 from public.providers p
            where p.id = provider_id and p.user_id = auth.uid()
        )
    );

drop policy if exists verification_insert on public.provider_verification_submissions;
create policy verification_insert on public.provider_verification_submissions for insert
    with check (
        submitted_by = auth.uid()
        and exists (
            select 1 from public.providers p
            where p.id = provider_id and p.user_id = auth.uid()
        )
    );

drop policy if exists verification_admin_update on public.provider_verification_submissions;
create policy verification_admin_update on public.provider_verification_submissions for update
    using (public.is_admin());

-- reviews
drop policy if exists reviews_public_read on public.reviews;
create policy reviews_public_read on public.reviews for select
    using (is_visible = true and moderation_status = 'VISIBLE');

drop policy if exists reviews_own_read on public.reviews;
create policy reviews_own_read on public.reviews for select
    using (reviewer_id = auth.uid() or reviewee_id = auth.uid() or public.is_admin());

drop policy if exists reviews_insert on public.reviews;
create policy reviews_insert on public.reviews for insert
    with check (reviewer_id = auth.uid());

drop policy if exists reviews_admin_update on public.reviews;
create policy reviews_admin_update on public.reviews for update
    using (public.is_admin());

-- reports
drop policy if exists reports_insert on public.reports;
create policy reports_insert on public.reports for insert
    with check (reporter_id = auth.uid());

drop policy if exists reports_select on public.reports;
create policy reports_select on public.reports for select
    using (reporter_id = auth.uid() or public.is_admin());

drop policy if exists reports_admin_update on public.reports;
create policy reports_admin_update on public.reports for update
    using (public.is_admin());

-- blocks
drop policy if exists blocks_manage on public.user_blocks;
create policy blocks_manage on public.user_blocks for all
    using (blocker_id = auth.uid())
    with check (blocker_id = auth.uid());

-- audit logs (admin only)
drop policy if exists audit_admin_read on public.admin_audit_logs;
create policy audit_admin_read on public.admin_audit_logs for select
    using (public.is_admin());

drop policy if exists audit_admin_insert on public.admin_audit_logs;
create policy audit_admin_insert on public.admin_audit_logs for insert
    with check (public.is_admin() and admin_id = auth.uid());
