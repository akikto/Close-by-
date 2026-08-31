-- Close by — Supabase schema additions (Phase 11: Saved services)
-- Apply after schema_phase9.sql. Idempotent where possible.

create table if not exists public.saved_services (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users (id) on delete cascade,
    service_id uuid not null references public.services (id) on delete cascade,
    created_at timestamptz not null default now(),
    unique (user_id, service_id)
);

create index if not exists saved_services_user_idx on public.saved_services (user_id);
create index if not exists saved_services_service_idx on public.saved_services (service_id);

alter table public.saved_services enable row level security;

drop policy if exists saved_services_manage on public.saved_services;
create policy saved_services_manage on public.saved_services for all
    using (user_id = auth.uid())
    with check (user_id = auth.uid());
