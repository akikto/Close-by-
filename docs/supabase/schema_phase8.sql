-- Close by — Supabase schema additions (Phase 8: Admin Dashboard helpers)
-- Apply after schema_phase7.sql.

-- Dashboard stats view (admin-only via RLS on underlying tables)
create or replace view public.admin_dashboard_stats as
select
    (select count(*) from auth.users) as total_users,
    (select count(*) from public.providers where is_active = true) as total_providers,
    (select count(*) from public.services where is_active = true and deleted_at is null) as active_services,
    (select count(*) from public.provider_verification_submissions where status = 'PENDING') as pending_verifications,
    (select count(*) from public.advertisements where status = 'PENDING') as pending_advertisements,
    (select count(*) from public.reports where status = 'OPEN') as open_reports;

-- Admin can update provider suspension
drop policy if exists providers_admin_update on public.providers;
create policy providers_admin_update on public.providers for update
    using (public.is_admin() or user_id = auth.uid())
    with check (public.is_admin() or user_id = auth.uid());

-- Admin can moderate services
drop policy if exists services_admin_update on public.services;
create policy services_admin_update on public.services for update
    using (
        public.is_admin()
        or exists (
            select 1 from public.providers p
            where p.id = services.provider_id and p.user_id = auth.uid()
        )
    );

-- Admin can suspend users via user_profiles
drop policy if exists user_profiles_admin_suspend on public.user_profiles;
create policy user_profiles_admin_suspend on public.user_profiles for update
    using (public.is_admin() or user_id = auth.uid());
