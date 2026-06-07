-- =============================================================================
-- V1: PostgreSQL extensions required by Doctor Assistant
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "vector";     -- pgvector for RAG embeddings
CREATE EXTENSION IF NOT EXISTS "btree_gist"; -- exclusion constraints on time ranges
