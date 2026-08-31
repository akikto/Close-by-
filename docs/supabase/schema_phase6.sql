-- Close by — Supabase schema additions (Phase 6: Notifications)
-- Apply after schema_phase5.sql.

create table if not exists public.notifications (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users (id) on delete cascade,
    type text not null,
    title text not null,
    body text not null,
    reference_type text,
    reference_id uuid,
    is_read boolean not null default false,
    created_at timestamptz not null default now()
);

create index if not exists notifications_user_idx on public.notifications (user_id);
create index if not exists notifications_user_unread_idx
    on public.notifications (user_id, is_read) where is_read = false;
create index if not exists notifications_created_idx
    on public.notifications (created_at desc);

-- FCM device tokens (private; never publicly readable)
create table if not exists public.device_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users (id) on delete cascade,
    token text not null,
    platform text not null default 'android',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (user_id, token)
);

alter table public.notifications enable row level security;
alter table public.device_tokens enable row level security;

drop policy if exists notifications_select on public.notifications;
create policy notifications_select on public.notifications for select
    using (user_id = auth.uid());

drop policy if exists notifications_update on public.notifications;
create policy notifications_update on public.notifications for update
    using (user_id = auth.uid())
    with check (user_id = auth.uid());

drop policy if exists notifications_insert on public.notifications;
create policy notifications_insert on public.notifications for insert
    with check (user_id = auth.uid() or public.is_admin());

drop policy if exists device_tokens_manage on public.device_tokens;
create policy device_tokens_manage on public.device_tokens for all
    using (user_id = auth.uid())
    with check (user_id = auth.uid());
