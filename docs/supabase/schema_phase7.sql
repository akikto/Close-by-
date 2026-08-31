-- Close by — Supabase schema additions (Phase 7: Advertisements)
-- Apply after schema_phase6.sql.

create table if not exists public.advertisements (
    id uuid primary key default gen_random_uuid(),
    owner_id uuid not null references auth.users (id) on delete cascade,
    business_name text not null,
    title text not null,
    description text not null default '',
    image_url text,
    contact_number text not null,
    latitude double precision not null,
    longitude double precision not null,
    target_radius_meters integer not null default 5000
        check (target_radius_meters > 0),
    start_at timestamptz not null,
    end_at timestamptz not null,
    status text not null default 'PENDING'
        check (status in ('PENDING', 'APPROVED', 'REJECTED', 'PAUSED', 'EXPIRED')),
    approved_by uuid references auth.users (id) on delete set null,
    approved_at timestamptz,
    rejection_reason text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    check (end_at > start_at)
);

create index if not exists advertisements_status_idx on public.advertisements (status);
create index if not exists advertisements_owner_idx on public.advertisements (owner_id);
create index if not exists advertisements_dates_idx on public.advertisements (start_at, end_at);

alter table public.advertisements enable row level security;

-- Public read: approved + active date range only (no exact coordinates in API filters)
drop policy if exists advertisements_public_read on public.advertisements;
create policy advertisements_public_read on public.advertisements for select
    using (
        status = 'APPROVED'
        and start_at <= now()
        and end_at >= now()
    );

drop policy if exists advertisements_owner_read on public.advertisements;
create policy advertisements_owner_read on public.advertisements for select
    using (owner_id = auth.uid() or public.is_admin());

drop policy if exists advertisements_insert on public.advertisements;
create policy advertisements_insert on public.advertisements for insert
    with check (owner_id = auth.uid());

drop policy if exists advertisements_owner_update on public.advertisements;
create policy advertisements_owner_update on public.advertisements for update
    using (owner_id = auth.uid() and status in ('PENDING', 'REJECTED'))
    with check (owner_id = auth.uid());

drop policy if exists advertisements_admin_update on public.advertisements;
create policy advertisements_admin_update on public.advertisements for update
    using (public.is_admin());

-- Storage bucket for ad images
insert into storage.buckets (id, name, public)
values ('ad-images', 'ad-images', true)
on conflict (id) do nothing;

drop policy if exists "Public read ad images" on storage.objects;
create policy "Public read ad images"
    on storage.objects for select
    using (bucket_id = 'ad-images');

drop policy if exists "Owners upload ad images" on storage.objects;
create policy "Owners upload ad images"
    on storage.objects for insert
    with check (bucket_id = 'ad-images' and auth.role() = 'authenticated');
