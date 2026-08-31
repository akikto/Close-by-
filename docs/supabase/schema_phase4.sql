-- Close by — Supabase schema additions (Phase 4: Production Service Requests)
-- Apply after schema_phase3.sql. Idempotent where possible.

-- ---------------------------------------------------------------------------
-- Anonymous / session-scoped customer request metadata
-- ---------------------------------------------------------------------------
alter table public.service_requests
    add column if not exists client_session_id text;

alter table public.service_requests
    add column if not exists provider_name text;

alter table public.service_requests
    add column if not exists provider_phone text;

create index if not exists service_requests_client_session_idx
    on public.service_requests (client_session_id);

create index if not exists service_requests_service_id_idx
    on public.service_requests (service_id);

create index if not exists service_requests_requested_date_idx
    on public.service_requests (requested_date);

create index if not exists service_requests_created_at_idx
    on public.service_requests (created_at desc);

-- ---------------------------------------------------------------------------
-- RLS: replace Phase 3 insecure anonymous read (customer_id IS NULL OR ...)
-- Authenticated customers read via customer_id. Providers read their requests.
-- Anonymous listing is handled on-device via remembered request UUIDs (see app).
-- ---------------------------------------------------------------------------
drop policy if exists "Customers read own requests" on public.service_requests;

create policy "Customers read own requests"
    on public.service_requests for select
    using (
        customer_id is not null
        and customer_id = auth.uid()
    );

drop policy if exists "Customers cancel own pending requests" on public.service_requests;

create policy "Customers cancel own pending requests"
    on public.service_requests for update
    using (
        status = 'PENDING'
        and customer_id is not null
        and customer_id = auth.uid()
    )
    with check (
        status in ('PENDING', 'CANCELLED')
        and customer_id is not null
        and customer_id = auth.uid()
    );

-- Providers may only transition statuses on their own requests (existing policy
-- remains; ensure status transitions are validated in app + DB check).
alter table public.service_requests
    drop constraint if exists service_requests_status_check;

alter table public.service_requests
    add constraint service_requests_status_check
    check (status in ('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED'));
