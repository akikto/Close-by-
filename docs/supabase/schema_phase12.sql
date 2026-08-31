-- Close by — Phase 12: production hardening notes
-- Apply after schema_phase11.sql.
-- No destructive schema changes; documents audit expectations.

comment on table public.saved_services is 'User favorites — RLS enforced per user_id';
comment on table public.user_blocks is 'User blocks — RLS enforced per blocker_id';
comment on table public.account_deletion_requests is 'Secure account deletion requests — user or admin only';
