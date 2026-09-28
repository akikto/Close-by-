---
name: Supabase view migration drift
description: Preserve existing dashboard view columns when replaying earlier Supabase migrations against an advanced project.
---

## Rule

PostgreSQL `CREATE OR REPLACE VIEW` cannot remove or reorder existing columns. When an existing Supabase project already has a later-added trailing column, make earlier view migrations preserve the existing shape or conditionally skip the replacement. Keep the fresh-install path valid and avoid referencing tables introduced by later migrations.

**Why:** A project can have an advanced schema despite an empty migration ledger; replaying an older view definition then fails with a cannot-drop-columns error.

**How to apply:** Inspect the live view's column order before applying migrations. Prefer forward-compatible guards over dropping and recreating views, then verify the final definition and dependent policies.