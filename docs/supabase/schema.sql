-- Close by — Supabase / PostgreSQL schema (Phase 1: Real Service Data)
-- Apply in the Supabase SQL editor. Do not duplicate if tables already exist.

-- ---------------------------------------------------------------------------
-- Providers (extends existing base table)
-- ---------------------------------------------------------------------------
create table if not exists public.providers (
    id uuid primary key default gen_random_uuid(),
    name text not null,
    category text not null check (category in ('VEHICLES', 'LABOUR', 'EQUIPMENT')),
    phone_number text not null,
    latitude double precision not null,
    longitude double precision not null,
    is_verified boolean not null default false,
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

alter table public.providers
    add column if not exists is_verified boolean not null default false;

alter table public.providers
    add column if not exists is_active boolean not null default true;

-- ---------------------------------------------------------------------------
-- Services (listings)
-- ---------------------------------------------------------------------------
create table if not exists public.services (
    id uuid primary key default gen_random_uuid(),
    provider_id uuid not null references public.providers (id) on delete cascade,
    category text not null check (category in ('VEHICLES', 'LABOUR', 'EQUIPMENT')),
    subcategory text not null,
    title text not null,
    description text not null default '',
    image_urls text[] not null default '{}',
    latitude double precision not null,
    longitude double precision not null,
    availability text not null default 'AVAILABLE_NOW'
        check (availability in ('AVAILABLE_NOW', 'AVAILABLE_SOON', 'UNAVAILABLE')),
    price_amount double precision not null check (price_amount >= 0),
    price_unit text not null default 'DAY'
        check (price_unit in ('HOUR', 'DAY', 'TRIP', 'JOB', 'NONE')),
    price_is_starting boolean not null default false,
    rating double precision not null default 0 check (rating >= 0 and rating <= 5),
    review_count integer not null default 0 check (review_count >= 0),
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists services_provider_id_idx on public.services (provider_id);
create index if not exists services_category_idx on public.services (category);
create index if not exists services_active_idx on public.services (is_active);

-- ---------------------------------------------------------------------------
-- Row Level Security (public read for active listings; writes via auth later)
-- ---------------------------------------------------------------------------
alter table public.providers enable row level security;
alter table public.services enable row level security;

drop policy if exists "Public read active providers" on public.providers;
create policy "Public read active providers"
    on public.providers for select
    using (is_active = true);

drop policy if exists "Public read active services" on public.services;
create policy "Public read active services"
    on public.services for select
    using (is_active = true);

-- ---------------------------------------------------------------------------
-- Seed data (optional — remove in production)
-- ---------------------------------------------------------------------------
insert into public.providers (id, name, category, phone_number, latitude, longitude, is_verified)
values
    ('11111111-1111-1111-1111-111111111101', 'Ravi Kumar', 'EQUIPMENT', '+910000000001', 12.9716, 77.5946, true),
    ('11111111-1111-1111-1111-111111111102', 'Suresh Electricals', 'LABOUR', '+910000000002', 12.9352, 77.6146, false),
    ('11111111-1111-1111-1111-111111111103', 'Farm Equip Co.', 'VEHICLES', '+910000000003', 13.0, 77.6, true)
on conflict (id) do nothing;

insert into public.services (
    id, provider_id, category, subcategory, title, description,
    image_urls, latitude, longitude, availability,
    price_amount, price_unit, price_is_starting, rating, review_count
)
values
    (
        '22222222-2222-2222-2222-222222222201',
        '11111111-1111-1111-1111-111111111101',
        'EQUIPMENT', 'WATER_PUMP', 'Water Pump',
        'High-capacity water pump suitable for agricultural and construction use.',
        array['https://images.unsplash.com/photo-1581092160562-40aa08e78837?w=400'],
        12.9716, 77.5946, 'AVAILABLE_NOW', 500, 'DAY', false, 4.7, 32
    ),
    (
        '22222222-2222-2222-2222-222222222202',
        '11111111-1111-1111-1111-111111111102',
        'LABOUR', 'ELECTRICIAN', 'Experienced Electrician',
        'Residential and commercial electrical work, wiring, and repairs.',
        array['https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=400'],
        12.9352, 77.6146, 'AVAILABLE_SOON', 300, 'HOUR', false, 4.5, 18
    ),
    (
        '22222222-2222-2222-2222-222222222203',
        '11111111-1111-1111-1111-111111111103',
        'VEHICLES', 'TRACTOR', 'Mahindra Tractor for Hire',
        'Well maintained tractor available for farm and transport work.',
        array['https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=400'],
        13.0, 77.6, 'AVAILABLE_NOW', 1500, 'TRIP', false, 4.9, 51
    )
on conflict (id) do nothing;
