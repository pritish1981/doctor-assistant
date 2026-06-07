-- =============================================================================
-- V4: pgvector RAG schema — Spring AI vector_store + knowledge source types
-- Stores: Doctor profiles, Clinic FAQ, Insurance policies
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Spring AI PgVectorStore table (Flyway-managed; initialize-schema=false)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vector_store (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content   TEXT,
    metadata  JSONB,
    embedding vector(1536)
);

COMMENT ON TABLE vector_store IS 'Spring AI pgvector store for RAG retrieval (doctor profiles, FAQs, insurance policies).';
COMMENT ON COLUMN vector_store.metadata IS 'JSON metadata: source_type, source_id, title, category, published, chunk_index.';

CREATE INDEX IF NOT EXISTS idx_vector_store_metadata
    ON vector_store USING GIN (metadata);

CREATE INDEX IF NOT EXISTS idx_vector_store_source_type
    ON vector_store ((metadata ->> 'source_type'));

CREATE INDEX IF NOT EXISTS idx_vector_store_source_id
    ON vector_store ((metadata ->> 'source_id'))
    WHERE metadata ->> 'source_id' IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_vector_store_embedding
    ON vector_store
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- ---------------------------------------------------------------------------
-- Extend knowledge_documents source types (domain audit / ingestion source)
-- ---------------------------------------------------------------------------
ALTER TABLE knowledge_documents
    DROP CONSTRAINT IF EXISTS chk_knowledge_documents_source_type;

ALTER TABLE knowledge_documents
    ADD CONSTRAINT chk_knowledge_documents_source_type CHECK (
        source_type IN (
            'DOCTOR_PROFILE',
            'FAQ',
            'INSURANCE_POLICY',
            'TRIAGE_GUIDE',
            'CLINIC_POLICY'
        )
    );

COMMENT ON COLUMN knowledge_documents.source_type IS
    'DOCTOR_PROFILE | FAQ | INSURANCE_POLICY | TRIAGE_GUIDE | CLINIC_POLICY';

-- ---------------------------------------------------------------------------
-- RAG source catalog — tracks ingested documents per source type
-- ---------------------------------------------------------------------------
CREATE TABLE rag_source_catalog (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    source_type     VARCHAR(30)     NOT NULL,
    source_id       UUID,
    title           VARCHAR(500)    NOT NULL,
    chunk_count     INTEGER         NOT NULL DEFAULT 0,
    published       BOOLEAN         NOT NULL DEFAULT TRUE,
    last_ingested_at TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_rag_source_catalog_source_type CHECK (
        source_type IN ('DOCTOR_PROFILE', 'FAQ', 'INSURANCE_POLICY')
    ),
    CONSTRAINT chk_rag_source_catalog_chunk_count CHECK (chunk_count >= 0),
    CONSTRAINT uq_rag_source_catalog_source UNIQUE (source_type, source_id, title)
);

CREATE INDEX idx_rag_source_catalog_type ON rag_source_catalog (source_type);
CREATE INDEX idx_rag_source_catalog_published ON rag_source_catalog (published) WHERE published = TRUE;

CREATE TRIGGER trg_rag_source_catalog_updated_at
    BEFORE UPDATE ON rag_source_catalog
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE rag_source_catalog IS 'Catalog of RAG corpus entries synced into vector_store.';
