-- =============================================================================
-- V2: Core schema — Doctor Assistant
-- Entities: Patient, Doctor, Availability, Appointment,
--           ConversationHistory, KnowledgeDocument
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Shared: auto-update updated_at on row modification
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------------
-- Lookup: medical specialties
-- ---------------------------------------------------------------------------
CREATE TABLE specialties (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50)     NOT NULL,
    name            VARCHAR(150)    NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'system',
    updated_by      VARCHAR(100)    NOT NULL DEFAULT 'system',

    CONSTRAINT uq_specialties_code UNIQUE (code),
    CONSTRAINT chk_specialties_code_format CHECK (code ~ '^[A-Z][A-Z0-9_]{1,49}$')
);

CREATE TRIGGER trg_specialties_updated_at
    BEFORE UPDATE ON specialties
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- Patient
-- ---------------------------------------------------------------------------
CREATE TABLE patients (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    external_subject_id VARCHAR(255),
    email               VARCHAR(255)    NOT NULL,
    phone               VARCHAR(30),
    full_name           VARCHAR(200)    NOT NULL,
    date_of_birth       DATE,
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100)    NOT NULL DEFAULT 'system',
    updated_by          VARCHAR(100)    NOT NULL DEFAULT 'system',

    CONSTRAINT uq_patients_email UNIQUE (email),
    CONSTRAINT uq_patients_external_subject_id UNIQUE (external_subject_id),
    CONSTRAINT chk_patients_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_patients_active ON patients (active) WHERE active = TRUE;
CREATE INDEX idx_patients_full_name ON patients (full_name);

CREATE TRIGGER trg_patients_updated_at
    BEFORE UPDATE ON patients
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- Doctor
-- ---------------------------------------------------------------------------
CREATE TABLE doctors (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    specialty_id        UUID            NOT NULL,
    license_number      VARCHAR(50)     NOT NULL,
    full_name           VARCHAR(200)    NOT NULL,
    email               VARCHAR(255),
    phone               VARCHAR(30),
    bio                 TEXT,
    qualifications      TEXT,
    languages           VARCHAR(255)    NOT NULL DEFAULT 'English',
    years_experience    SMALLINT,
    rating_avg          NUMERIC(3, 2)   NOT NULL DEFAULT 0.00,
    consultation_fee    NUMERIC(10, 2),
    active              BOOLEAN         NOT NULL DEFAULT TRUE,
    metadata            JSONB           NOT NULL DEFAULT '{}',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100)    NOT NULL DEFAULT 'system',
    updated_by          VARCHAR(100)    NOT NULL DEFAULT 'system',

    CONSTRAINT fk_doctors_specialty
        FOREIGN KEY (specialty_id) REFERENCES specialties (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_doctors_license_number UNIQUE (license_number),
    CONSTRAINT chk_doctors_rating_avg CHECK (rating_avg >= 0 AND rating_avg <= 5),
    CONSTRAINT chk_doctors_years_experience CHECK (years_experience IS NULL OR years_experience >= 0),
    CONSTRAINT chk_doctors_consultation_fee CHECK (consultation_fee IS NULL OR consultation_fee >= 0)
);

CREATE INDEX idx_doctors_specialty_id ON doctors (specialty_id);
CREATE INDEX idx_doctors_active ON doctors (active) WHERE active = TRUE;
CREATE INDEX idx_doctors_full_name ON doctors (full_name);
CREATE INDEX idx_doctors_metadata ON doctors USING GIN (metadata);

CREATE TRIGGER trg_doctors_updated_at
    BEFORE UPDATE ON doctors
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- Availability — bookable time slots per doctor
-- ---------------------------------------------------------------------------
CREATE TABLE availability (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id       UUID            NOT NULL,
    slot_start      TIMESTAMPTZ     NOT NULL,
    slot_end        TIMESTAMPTZ     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
    hold_expires_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'system',
    updated_by      VARCHAR(100)    NOT NULL DEFAULT 'system',

    CONSTRAINT fk_availability_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_availability_slot_range CHECK (slot_end > slot_start),
    CONSTRAINT chk_availability_status CHECK (
        status IN ('OPEN', 'HELD', 'BOOKED', 'BLOCKED', 'CANCELLED')
    ),
    CONSTRAINT chk_availability_hold_expires CHECK (
        (status = 'HELD' AND hold_expires_at IS NOT NULL)
        OR (status <> 'HELD' AND hold_expires_at IS NULL)
    ),
    CONSTRAINT uq_availability_doctor_slot UNIQUE (doctor_id, slot_start)
);

-- Prevent overlapping slots for the same doctor
ALTER TABLE availability
    ADD CONSTRAINT ex_availability_no_overlap
    EXCLUDE USING gist (
        doctor_id WITH =,
        tstzrange(slot_start, slot_end, '[)') WITH &&
    );

CREATE INDEX idx_availability_doctor_slot_start ON availability (doctor_id, slot_start);
CREATE INDEX idx_availability_open_slots
    ON availability (doctor_id, slot_start)
    WHERE status = 'OPEN';
CREATE INDEX idx_availability_status ON availability (status);

CREATE TRIGGER trg_availability_updated_at
    BEFORE UPDATE ON availability
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- Appointment
-- ---------------------------------------------------------------------------
CREATE TABLE appointments (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_code      VARCHAR(20)     NOT NULL,
    patient_id          UUID            NOT NULL,
    doctor_id           UUID            NOT NULL,
    availability_id     UUID            NOT NULL,
    scheduled_at        TIMESTAMPTZ     NOT NULL,
    duration_minutes    SMALLINT        NOT NULL DEFAULT 30,
    status              VARCHAR(20)     NOT NULL DEFAULT 'CONFIRMED',
    reason              TEXT,
    notes               TEXT,
    idempotency_key     VARCHAR(100),
    cancelled_at        TIMESTAMPTZ,
    cancellation_reason TEXT,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(100)    NOT NULL DEFAULT 'system',
    updated_by          VARCHAR(100)    NOT NULL DEFAULT 'system',

    CONSTRAINT fk_appointments_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_appointments_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_appointments_availability
        FOREIGN KEY (availability_id) REFERENCES availability (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_appointments_reference_code UNIQUE (reference_code),
    CONSTRAINT uq_appointments_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT uq_appointments_availability_id UNIQUE (availability_id),
    CONSTRAINT chk_appointments_status CHECK (
        status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')
    ),
    CONSTRAINT chk_appointments_duration CHECK (duration_minutes > 0 AND duration_minutes <= 480),
    CONSTRAINT chk_appointments_cancelled CHECK (
        (status = 'CANCELLED' AND cancelled_at IS NOT NULL)
        OR (status <> 'CANCELLED' AND cancelled_at IS NULL)
    )
);

CREATE INDEX idx_appointments_patient_scheduled ON appointments (patient_id, scheduled_at DESC);
CREATE INDEX idx_appointments_doctor_scheduled ON appointments (doctor_id, scheduled_at DESC);
CREATE INDEX idx_appointments_status ON appointments (status);
CREATE INDEX idx_appointments_scheduled_at ON appointments (scheduled_at);

CREATE TRIGGER trg_appointments_updated_at
    BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- ConversationHistory — session header + message rows
-- ---------------------------------------------------------------------------
CREATE TABLE conversation_sessions (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id      UUID            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    title           VARCHAR(255),
    context         JSONB           NOT NULL DEFAULT '{}',
    started_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    last_active_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    ended_at        TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'system',
    updated_by      VARCHAR(100)    NOT NULL DEFAULT 'system',

    CONSTRAINT fk_conversation_sessions_patient
        FOREIGN KEY (patient_id) REFERENCES patients (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_conversation_sessions_status CHECK (
        status IN ('ACTIVE', 'CLOSED', 'ARCHIVED')
    ),
    CONSTRAINT chk_conversation_sessions_ended CHECK (
        (status IN ('CLOSED', 'ARCHIVED') AND ended_at IS NOT NULL)
        OR (status = 'ACTIVE' AND ended_at IS NULL)
    )
);

CREATE INDEX idx_conversation_sessions_patient ON conversation_sessions (patient_id, last_active_at DESC);
CREATE INDEX idx_conversation_sessions_status ON conversation_sessions (status);

CREATE TRIGGER trg_conversation_sessions_updated_at
    BEFORE UPDATE ON conversation_sessions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE conversation_history (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id      UUID            NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    content         TEXT            NOT NULL,
    tool_calls      JSONB,
    rag_sources     JSONB,
    token_count     INTEGER,
    sequence_number INTEGER         NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'system',

    CONSTRAINT fk_conversation_history_session
        FOREIGN KEY (session_id) REFERENCES conversation_sessions (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_conversation_history_role CHECK (
        role IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')
    ),
    CONSTRAINT chk_conversation_history_token_count CHECK (
        token_count IS NULL OR token_count >= 0
    ),
    CONSTRAINT uq_conversation_history_session_sequence UNIQUE (session_id, sequence_number)
);

CREATE INDEX idx_conversation_history_session ON conversation_history (session_id, sequence_number);
CREATE INDEX idx_conversation_history_created_at ON conversation_history (created_at);

-- ---------------------------------------------------------------------------
-- KnowledgeDocument — RAG corpus with vector embeddings
-- ---------------------------------------------------------------------------
CREATE TABLE knowledge_documents (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    source_type     VARCHAR(30)     NOT NULL,
    source_id       UUID,
    title           VARCHAR(500)    NOT NULL,
    content         TEXT            NOT NULL,
    chunk_index     INTEGER         NOT NULL DEFAULT 0,
    metadata        JSONB           NOT NULL DEFAULT '{}',
    embedding       vector(1536),
    published       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(100)    NOT NULL DEFAULT 'system',
    updated_by      VARCHAR(100)    NOT NULL DEFAULT 'system',

    CONSTRAINT chk_knowledge_documents_source_type CHECK (
        source_type IN ('DOCTOR_PROFILE', 'FAQ', 'TRIAGE_GUIDE', 'CLINIC_POLICY')
    ),
    CONSTRAINT chk_knowledge_documents_chunk_index CHECK (chunk_index >= 0),
    CONSTRAINT uq_knowledge_documents_source_chunk UNIQUE (source_type, source_id, chunk_index)
);

CREATE INDEX idx_knowledge_documents_source ON knowledge_documents (source_type, source_id);
CREATE INDEX idx_knowledge_documents_published ON knowledge_documents (published) WHERE published = TRUE;
CREATE INDEX idx_knowledge_documents_metadata ON knowledge_documents USING GIN (metadata);

-- HNSW index for cosine similarity search (create after seeding in prod for faster bulk load)
CREATE INDEX idx_knowledge_documents_embedding
    ON knowledge_documents
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

CREATE TRIGGER trg_knowledge_documents_updated_at
    BEFORE UPDATE ON knowledge_documents
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- Comments for documentation
-- ---------------------------------------------------------------------------
COMMENT ON TABLE patients IS 'Registered clinic patients who book appointments and use the AI assistant.';
COMMENT ON TABLE doctors IS 'Clinic doctors available for appointment booking and search.';
COMMENT ON TABLE availability IS 'Bookable time slots per doctor; status drives booking lifecycle.';
COMMENT ON TABLE appointments IS 'Confirmed bookings linking a patient, doctor, and availability slot.';
COMMENT ON TABLE conversation_sessions IS 'Chat session header for AI assistant conversations.';
COMMENT ON TABLE conversation_history IS 'Individual messages within a conversation session.';
COMMENT ON TABLE knowledge_documents IS 'RAG knowledge base chunks with pgvector embeddings.';

COMMENT ON COLUMN availability.hold_expires_at IS 'Temporary hold expiry when status = HELD during checkout.';
COMMENT ON COLUMN appointments.idempotency_key IS 'Client-supplied key to prevent duplicate bookings on retry.';
COMMENT ON COLUMN knowledge_documents.embedding IS 'OpenAI text-embedding-3-small vector (1536 dimensions).';
