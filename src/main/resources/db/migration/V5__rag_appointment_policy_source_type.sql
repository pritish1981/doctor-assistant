-- Allow APPOINTMENT_POLICY in RAG source catalog (classpath PDF ingestion)
ALTER TABLE rag_source_catalog
    DROP CONSTRAINT IF EXISTS chk_rag_source_catalog_source_type;

ALTER TABLE rag_source_catalog
    ADD CONSTRAINT chk_rag_source_catalog_source_type CHECK (
        source_type IN ('DOCTOR_PROFILE', 'FAQ', 'INSURANCE_POLICY', 'APPOINTMENT_POLICY')
    );

COMMENT ON COLUMN rag_source_catalog.source_type IS
    'Knowledge source type: DOCTOR_PROFILE, FAQ, INSURANCE_POLICY, APPOINTMENT_POLICY';
