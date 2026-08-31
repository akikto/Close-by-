-- Close by — Supabase schema additions (Phase 9: Account deletion requests)
-- Apply after schema_phase8.sql. Idempotent where possible.

create table if not exists public.account_deletion_requests (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users (id) on delete cascade,
    reason text,
    status text not null default 'PENDING'
        check (status in ('PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED')),
    requested_at timestamptz not null default now(),
    processed_at timestamptz,
    unique (user_id, status)
);

create index if not exists account_deletion_user_idx
    on public.account_deletion_requests (user_id);

alter table public.account_deletion_requests enable row level security;

drop policy if exists account_deletion_insert on public.account_deletion_requests;
create policy account_deletion_insert on public.account_deletion_requests for insert
    with check (user_id = auth.uid());

drop policy if exists account_deletion_select on public.account_deletion_requests;
create policy account_deletion_select on public.account_deletion_requests for select
    using (user_id = auth.uid() or public.is_admin());

drop policy if exists account_deletion_admin_update on public.account_deletion_requests;
create policy account_deletion_admin_update on public.account_deletion_requests for update
    using (public.is_admin());
