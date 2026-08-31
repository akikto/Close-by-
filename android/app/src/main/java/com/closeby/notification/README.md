# Phase 6 — Notifications (Android)

In-app notifications for request lifecycle events. Backend tables are defined in `docs/supabase/schema_phase6.sql`.

## Scope

- Persist notifications in Supabase `notifications` table (or in-memory mock without credentials).
- List, read/unread styling, mark one/all read, unread badge on the Notifications tab.
- `NotificationEventHandler` listens to `RequestNotificationBridge` and creates rows for request events (`reference_type = REQUEST`).

## Not configured

**FCM (Firebase Cloud Messaging) is not wired.** The `device_tokens` table exists for a future push layer; this phase only implements the in-app notification architecture. No `google-services.json`, no FCM service, no background push delivery.

## Wiring

- `NotificationDependenciesFactory.notificationRepository()`
- `NotificationDependenciesFactory.ensureEventHandlerStarted(context)` — call on app init (see `CloseByApp`).
- Signed-in users (`auth.users`) receive notifications; anonymous customers are skipped until they sign in.
