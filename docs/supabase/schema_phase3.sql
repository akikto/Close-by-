-- Close by — Supabase schema additions (Phase 3: Provider Management)
-- Apply after schema.sql. Safe to re-run (idempotent where possible).

-- ---------------------------------------------------------------------------
-- Providers: link to auth.users + profile image
-- ---------------------------------------------------------------------------
alter table public.providers
    add column if not exists user_id uuid unique references auth.users (id) on delete set null;

alter table public.providers
    add column if not exists profile_image_url text;

alter table public.providers
    add column if not exists rating double precision not null default 0
        check (rating >= 0 and rating <= 5);

alter table public.providers
    add column if not exists review_count integer not null default 0 check (review_count >= 0);

-- ---------------------------------------------------------------------------
-- Services: per-listing contact, optional price, soft-delete
-- ---------------------------------------------------------------------------
alter table public.services
    add column if not exists contact_number text;

alter table public.services
    add column if not exists deleted_at timestamptz;

alter table public.services
    alter column price_amount drop not null;

-- Backfill contact_number from provider phone where missing
update public.services s
set contact_number = p.phone_number
from public.providers p
where s.provider_id = p.id and s.contact_number is null;

-- ---------------------------------------------------------------------------
-- Weekly provider availability (Agent 5 model)
-- ---------------------------------------------------------------------------
create table if not exists public.provider_availability (
    id uuid primary key default gen_random_uuid(),
    provider_id uuid not null references public.providers (id) on delete cascade,
    day_of_week smallint not null check (day_of_week between 1 and 7),
    is_available boolean not null default false,
    start_time time,
    end_time time,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (provider_id, day_of_week)
);

create index if not exists provider_availability_provider_idx
    on public.provider_availability (provider_id);

-- ---------------------------------------------------------------------------
-- Service requests (no payment fields)
-- ---------------------------------------------------------------------------
create table if not exists public.service_requests (
    id uuid primary key default gen_random_uuid(),
    service_id uuid not null references public.services (id) on delete restrict,
    provider_id uuid not null references public.providers (id) on delete restrict,
    customer_id uuid references auth.users (id) on delete set null,
    customer_name text,
    customer_phone text,
    service_title text not null,
    requested_date date not null,
    start_time time not null,
    end_time time not null,
    duration text not null default '',
    budget_amount double precision check (budget_amount is null or budget_amount >= 0),
    budget_currency text not null default 'INR',
    budget_unit text check (budget_unit is null or budget_unit in ('HOUR', 'DAY', 'TRIP', 'JOB', 'OTHER')),
    note text,
    status text not null default 'PENDING'
        check (status in ('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists service_requests_provider_idx on public.service_requests (provider_id);
create index if not exists service_requests_customer_idx on public.service_requests (customer_id);
create index if not exists service_requests_status_idx on public.service_requests (status);

-- ---------------------------------------------------------------------------
-- Storage bucket for service images (public read)
-- ---------------------------------------------------------------------------
insert into storage.buckets (id, name, public)
values ('service-images', 'service-images', true)
on conflict (id) do nothing;

-- ---------------------------------------------------------------------------
-- Row Level Security — provider writes (Email OTP / auth.users)
-- ---------------------------------------------------------------------------

-- Providers: owners can read/update own row (including inactive for dashboard)
drop policy if exists "Providers read own row" on public.providers;
create policy "Providers read own row"
    on public.providers for select
    using (auth.uid() = user_id);

drop policy if exists "Providers update own row" on public.providers;
create policy "Providers update own row"
    on public.providers for update
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);

drop policy if exists "Providers insert own row" on public.providers;
create policy "Providers insert own row"
    on public.providers for insert
    with check (auth.uid() = user_id);

-- Services: owners manage own listings (including inactive / soft-deleted)
drop policy if exists "Providers read own services" on public.services;
create policy "Providers read own services"
    on public.services for select
    using (
        exists (
            select 1 from public.providers p
            where p.id = services.provider_id and p.user_id = auth.uid()
        )
    );

drop policy if exists "Providers insert own services" on public.services;
create policy "Providers insert own services"
    on public.services for insert
    with check (
        exists (
            select 1 from public.providers p
            where p.id = services.provider_id and p.user_id = auth.uid()
        )
    );

drop policy if exists "Providers update own services" on public.services;
create policy "Providers update own services"
    on public.services for update
    using (
        exists (
            select 1 from public.providers p
            where p.id = services.provider_id and p.user_id = auth.uid()
        )
    )
    with check (
        exists (
            select 1 from public.providers p
            where p.id = services.provider_id and p.user_id = auth.uid()
        )
    );

-- Availability
alter table public.provider_availability enable row level security;

drop policy if exists "Public read provider availability" on public.provider_availability;
create policy "Public read provider availability"
    on public.provider_availability for select
    using (true);

drop policy if exists "Providers manage own availability" on public.provider_availability;
create policy "Providers manage own availability"
    on public.provider_availability for all
    using (
        exists (
            select 1 from public.providers p
            where p.id = provider_availability.provider_id and p.user_id = auth.uid()
        )
    )
    with check (
        exists (
            select 1 from public.providers p
            where p.id = provider_availability.provider_id and p.user_id = auth.uid()
        )
    );

-- Service requests
alter table public.service_requests enable row level security;

drop policy if exists "Anyone can create requests" on public.service_requests;
create policy "Anyone can create requests"
    on public.service_requests for insert
    with check (true);

drop policy if exists "Customers read own requests" on public.service_requests;
create policy "Customers read own requests"
    on public.service_requests for select
    using (customer_id is null or customer_id = auth.uid());

drop policy if exists "Providers read own requests" on public.service_requests;
create policy "Providers read own requests"
    on public.service_requests for select
    using (
        exists (
            select 1 from public.providers p
            where p.id = service_requests.provider_id and p.user_id = auth.uid()
        )
    );

drop policy if exists "Providers update own requests" on public.service_requests;
create policy "Providers update own requests"
    on public.service_requests for update
    using (
        exists (
            select 1 from public.providers p
            where p.id = service_requests.provider_id and p.user_id = auth.uid()
        )
    )
    with check (
        exists (
            select 1 from public.providers p
            where p.id = service_requests.provider_id and p.user_id = auth.uid()
        )
    );

-- Storage policies
drop policy if exists "Public read service images" on storage.objects;
create policy "Public read service images"
    on storage.objects for select
    using (bucket_id = 'service-images');

drop policy if exists "Providers upload service images" on storage.objects;
create policy "Providers upload service images"
    on storage.objects for insert
    with check (
        bucket_id = 'service-images'
        and auth.role() = 'authenticated'
    );

drop policy if exists "Providers update own service images" on storage.objects;
create policy "Providers update own service images"
    on storage.objects for update
    using (bucket_id = 'service-images' and auth.role() = 'authenticated');

drop policy if exists "Providers delete own service images" on storage.objects;
create policy "Providers delete own service images"
    on storage.objects for delete
    using (bucket_id = 'service-images' and auth.role() = 'authenticated');
