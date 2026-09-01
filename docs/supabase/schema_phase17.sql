-- Close by — Phase 17 production indexes (Batch 5)
-- Apply after schema_phase12.sql. Forward-only; does not modify prior migrations.

-- Saved services: user lookup
create index if not exists saved_services_user_service_idx
    on public.saved_services (user_id, service_id);

-- Notifications: deduplication-friendly lookup by user + reference
create index if not exists notifications_user_reference_idx
    on public.notifications (user_id, reference_type, reference_id)
    where reference_id is not null;

-- Service requests: customer and provider list queries
create index if not exists service_requests_customer_created_idx
    on public.service_requests (customer_id, created_at desc)
    where customer_id is not null;

create index if not exists service_requests_provider_status_idx
    on public.service_requests (provider_id, status, created_at desc);

-- Reviews: provider listing
create index if not exists reviews_provider_created_idx
    on public.reviews (provider_id, created_at desc);

-- Reports: reporter status
create index if not exists reports_reporter_status_idx
    on public.reports (reporter_id, status);

-- Advertisements: owner campaigns
create index if not exists advertisements_owner_status_idx
    on public.advertisements (owner_id, status, created_at desc);

comment on index saved_services_user_service_idx is
    'Supports saved-service sync and duplicate prevention per user.';
comment on index notifications_user_reference_idx is
    'Supports in-app notification deduplication by reference.';
