-- =============================================================================
-- Sample / development seed data — Doctor Assistant
--
-- Usage (dev only):
--   psql -U doctor_assistant -d doctor_assistant -f sample_data.sql
--
-- Note: knowledge_documents.embedding is NULL; populate via embedding worker.
-- Note: not executed by Flyway — run manually after migrations.
-- =============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- Specialties
-- ---------------------------------------------------------------------------
INSERT INTO specialties (id, code, name, description, created_by, updated_by) VALUES
    ('a1000000-0000-4000-8000-000000000001', 'CARDIOLOGY',    'Cardiology',       'Heart and cardiovascular system', 'seed', 'seed'),
    ('a1000000-0000-4000-8000-000000000002', 'DERMATOLOGY',   'Dermatology',      'Skin, hair, and nail conditions', 'seed', 'seed'),
    ('a1000000-0000-4000-8000-000000000003', 'GENERAL_MED',   'General Medicine', 'Primary care and general health', 'seed', 'seed'),
    ('a1000000-0000-4000-8000-000000000004', 'PEDIATRICS',    'Pediatrics',       'Medical care for infants and children', 'seed', 'seed'),
    ('a1000000-0000-4000-8000-000000000005', 'ORTHOPEDICS',   'Orthopedics',      'Bones, joints, and musculoskeletal system', 'seed', 'seed')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Patients
-- ---------------------------------------------------------------------------
INSERT INTO patients (id, external_subject_id, email, phone, full_name, date_of_birth, created_by, updated_by) VALUES
    ('b2000000-0000-4000-8000-000000000001', 'auth0|patient001', 'alice.johnson@email.com',  '+1-555-0101', 'Alice Johnson',  '1988-03-15', 'seed', 'seed'),
    ('b2000000-0000-4000-8000-000000000002', 'auth0|patient002', 'bob.smith@email.com',      '+1-555-0102', 'Bob Smith',      '1975-11-22', 'seed', 'seed'),
    ('b2000000-0000-4000-8000-000000000003', 'auth0|patient003', 'carol.williams@email.com', '+1-555-0103', 'Carol Williams', '1992-07-08', 'seed', 'seed')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Doctors
-- ---------------------------------------------------------------------------
INSERT INTO doctors (
    id, specialty_id, license_number, full_name, email, phone,
    bio, qualifications, languages, years_experience, rating_avg, consultation_fee,
    metadata, created_by, updated_by
) VALUES
(
    'c3000000-0000-4000-8000-000000000001',
    'a1000000-0000-4000-8000-000000000001',
    'MED-CA-10001', 'Dr. Rajesh Sharma', 'r.sharma@superclinic.com', '+1-555-1001',
    'Senior cardiologist specializing in preventive cardiology and heart failure management.',
    'MD (AIIMS), DM Cardiology, FACC',
    'English,Hindi', 18, 4.85, 150.00,
    '{"location": "Building A, Floor 3", "accepts_insurance": true}',
    'seed', 'seed'
),
(
    'c3000000-0000-4000-8000-000000000002',
    'a1000000-0000-4000-8000-000000000002',
    'MED-CA-10002', 'Dr. Priya Nair', 'p.nair@superclinic.com', '+1-555-1002',
    'Dermatologist with expertise in clinical dermatology and cosmetic procedures.',
    'MD, DDV (Dermatology)',
    'English,Malayalam', 12, 4.72, 120.00,
    '{"location": "Building B, Floor 2", "accepts_insurance": true}',
    'seed', 'seed'
),
(
    'c3000000-0000-4000-8000-000000000003',
    'a1000000-0000-4000-8000-000000000003',
    'MED-CA-10003', 'Dr. James O''Connor', 'j.oconnor@superclinic.com', '+1-555-1003',
    'General physician providing comprehensive primary care for adults.',
    'MBBS, MD (Internal Medicine)',
    'English', 15, 4.90, 100.00,
    '{"location": "Building A, Floor 1", "accepts_insurance": true}',
    'seed', 'seed'
),
(
    'c3000000-0000-4000-8000-000000000004',
    'a1000000-0000-4000-8000-000000000004',
    'MED-CA-10004', 'Dr. Meera Patel', 'm.patel@superclinic.com', '+1-555-1004',
    'Pediatrician focused on child development, vaccinations, and adolescent health.',
    'MD, DCH, MRCPCH',
    'English,Gujarati', 10, 4.88, 110.00,
    '{"location": "Building C, Floor 1", "accepts_insurance": true}',
    'seed', 'seed'
),
(
    'c3000000-0000-4000-8000-000000000005',
    'a1000000-0000-4000-8000-000000000005',
    'MED-CA-10005', 'Dr. David Kim', 'd.kim@superclinic.com', '+1-555-1005',
    'Orthopedic surgeon specializing in sports injuries and joint replacement.',
    'MD, MS (Orthopedics), Fellowship Sports Medicine',
    'English,Korean', 14, 4.79, 175.00,
    '{"location": "Building B, Floor 4", "accepts_insurance": false}',
    'seed', 'seed'
);

-- ---------------------------------------------------------------------------
-- Availability (next-week slots — adjust timestamps for your environment)
-- Uses relative dates from current date at seed time
-- ---------------------------------------------------------------------------
INSERT INTO availability (id, doctor_id, slot_start, slot_end, status, created_by, updated_by)
SELECT
    gen_random_uuid(),
    d.id,
    (CURRENT_DATE + INTERVAL '1 day' + TIME '09:00') AT TIME ZONE 'UTC',
    (CURRENT_DATE + INTERVAL '1 day' + TIME '09:30') AT TIME ZONE 'UTC',
    'OPEN',
    'seed', 'seed'
FROM doctors d WHERE d.license_number = 'MED-CA-10001';

INSERT INTO availability (id, doctor_id, slot_start, slot_end, status, created_by, updated_by)
SELECT
    gen_random_uuid(),
    d.id,
    (CURRENT_DATE + INTERVAL '1 day' + TIME '11:00') AT TIME ZONE 'UTC',
    (CURRENT_DATE + INTERVAL '1 day' + TIME '11:30') AT TIME ZONE 'UTC',
    'OPEN',
    'seed', 'seed'
FROM doctors d WHERE d.license_number = 'MED-CA-10001';

INSERT INTO availability (id, doctor_id, slot_start, slot_end, status, created_by, updated_by)
SELECT
    'd4000000-0000-4000-8000-000000000001',
    d.id,
    (CURRENT_DATE + INTERVAL '2 days' + TIME '10:00') AT TIME ZONE 'UTC',
    (CURRENT_DATE + INTERVAL '2 days' + TIME '10:30') AT TIME ZONE 'UTC',
    'BOOKED',
    'seed', 'seed'
FROM doctors d WHERE d.license_number = 'MED-CA-10003';

INSERT INTO availability (id, doctor_id, slot_start, slot_end, status, created_by, updated_by)
SELECT
    gen_random_uuid(),
    d.id,
    (CURRENT_DATE + INTERVAL '2 days' + TIME '14:00') AT TIME ZONE 'UTC',
    (CURRENT_DATE + INTERVAL '2 days' + TIME '14:30') AT TIME ZONE 'UTC',
    'OPEN',
    'seed', 'seed'
FROM doctors d WHERE d.license_number = 'MED-CA-10002';

INSERT INTO availability (id, doctor_id, slot_start, slot_end, status, created_by, updated_by)
SELECT
    gen_random_uuid(),
    d.id,
    (CURRENT_DATE + INTERVAL '3 days' + TIME '09:30') AT TIME ZONE 'UTC',
    (CURRENT_DATE + INTERVAL '3 days' + TIME '10:00') AT TIME ZONE 'UTC',
    'OPEN',
    'seed', 'seed'
FROM doctors d WHERE d.license_number = 'MED-CA-10004';

-- ---------------------------------------------------------------------------
-- Appointments
-- ---------------------------------------------------------------------------
INSERT INTO appointments (
    id, reference_code, patient_id, doctor_id, availability_id,
    scheduled_at, duration_minutes, status, reason,
    idempotency_key, created_by, updated_by
)
SELECT
    'e5000000-0000-4000-8000-000000000001',
    'APT-2026-00001',
    'b2000000-0000-4000-8000-000000000001',
    d.id,
    'd4000000-0000-4000-8000-000000000001',
    a.slot_start,
    30,
    'CONFIRMED',
    'Annual health check-up',
    'idem-alice-20260607-1000',
    'seed', 'seed'
FROM doctors d
JOIN availability a ON a.id = 'd4000000-0000-4000-8000-000000000001'
WHERE d.license_number = 'MED-CA-10003';

-- ---------------------------------------------------------------------------
-- Conversation sessions & history
-- ---------------------------------------------------------------------------
INSERT INTO conversation_sessions (
    id, patient_id, status, title, context,
    started_at, last_active_at, created_by, updated_by
) VALUES (
    'f6000000-0000-4000-8000-000000000001',
    'b2000000-0000-4000-8000-000000000002',
    'ACTIVE',
    'Chest discomfort inquiry',
    '{"channel": "web", "locale": "en-US"}',
    NOW() - INTERVAL '15 minutes',
    NOW() - INTERVAL '2 minutes',
    'seed', 'seed'
);

INSERT INTO conversation_history (
    id, session_id, role, content, sequence_number, token_count, created_by
) VALUES
(
    'f6100000-0000-4000-8000-000000000001',
    'f6000000-0000-4000-8000-000000000001',
    'USER',
    'I have been experiencing chest tightness and shortness of breath since yesterday. Which doctor should I see?',
    1, 28, 'seed'
),
(
    'f6100000-0000-4000-8000-000000000002',
    'f6000000-0000-4000-8000-000000000001',
    'ASSISTANT',
    'Based on your symptoms, I recommend consulting a Cardiologist. Dr. Rajesh Sharma specializes in cardiovascular care. This is not a diagnosis — please seek emergency care if symptoms are severe. Would you like to check his availability?',
    2, 52,
    'seed'
),
(
    'f6100000-0000-4000-8000-000000000003',
    'f6000000-0000-4000-8000-000000000001',
    'USER',
    'Yes, please show available slots tomorrow.',
    3, 10, 'seed'
);

-- ---------------------------------------------------------------------------
-- Knowledge documents (embeddings populated by worker)
-- ---------------------------------------------------------------------------
INSERT INTO knowledge_documents (
    id, source_type, source_id, title, content, chunk_index,
    metadata, published, created_by, updated_by
) VALUES
(
    'e7000000-0000-4000-8000-000000000001',
    'DOCTOR_PROFILE',
    'c3000000-0000-4000-8000-000000000001',
    'Dr. Rajesh Sharma — Cardiologist',
    'Dr. Rajesh Sharma is a senior cardiologist at Super Clinic with 18 years of experience. Specializations include preventive cardiology, heart failure, and hypertension management. Languages: English, Hindi. Location: Building A, Floor 3.',
    0,
    '{"specialty_code": "CARDIOLOGY", "doctor_name": "Dr. Rajesh Sharma", "languages": ["English", "Hindi"]}',
    TRUE, 'seed', 'seed'
),
(
    'e7000000-0000-4000-8000-000000000002',
    'DOCTOR_PROFILE',
    'c3000000-0000-4000-8000-000000000002',
    'Dr. Priya Nair — Dermatologist',
    'Dr. Priya Nair is a dermatologist specializing in clinical dermatology, acne treatment, and eczema management. Languages: English, Malayalam. Location: Building B, Floor 2.',
    0,
    '{"specialty_code": "DERMATOLOGY", "doctor_name": "Dr. Priya Nair", "languages": ["English", "Malayalam"]}',
    TRUE, 'seed', 'seed'
),
(
    'e7000000-0000-4000-8000-000000000003',
    'FAQ',
    NULL,
    'How do I book an appointment?',
    'You can book an appointment through the AI assistant by describing your symptoms or preferred doctor. The assistant will show available slots and confirm your booking. You will receive an email confirmation with reference code.',
    0,
    '{"category": "appointments", "tags": ["booking", "how-to"]}',
    TRUE, 'seed', 'seed'
),
(
    'e7000000-0000-4000-8000-000000000004',
    'FAQ',
    NULL,
    'What are the clinic operating hours?',
    'Super Clinic is open Monday to Saturday, 8:00 AM to 8:00 PM. Emergency services are available 24/7 at the main campus. Sunday outpatient hours are 9:00 AM to 1:00 PM.',
    0,
    '{"category": "general", "tags": ["hours", "location"]}',
    TRUE, 'seed', 'seed'
),
(
    'e7000000-0000-4000-8000-000000000005',
    'TRIAGE_GUIDE',
    NULL,
    'Chest pain symptom guidance',
    'Chest tightness with shortness of breath may warrant evaluation by a Cardiologist. If symptoms include severe pain, radiating pain to arm or jaw, or fainting, seek emergency care immediately. This guide is informational only and not a medical diagnosis.',
    0,
    '{"symptoms": ["chest pain", "shortness of breath"], "recommended_specialty": "CARDIOLOGY"}',
    TRUE, 'seed', 'seed'
),
(
    'e7000000-0000-4000-8000-000000000006',
    'CLINIC_POLICY',
    NULL,
    'Cancellation policy',
    'Appointments may be cancelled up to 4 hours before the scheduled time without penalty. Late cancellations or no-shows may incur a fee of $25. Reschedule through the assistant or patient portal.',
    0,
    '{"category": "policy", "tags": ["cancellation", "fees"]}',
    TRUE, 'seed', 'seed'
),
(
    'e7000000-0000-4000-8000-000000000007',
    'INSURANCE_POLICY',
    NULL,
    'Super Clinic Health Plus — Coverage Summary',
    'Super Clinic Health Plus covers outpatient consultations, diagnostic imaging (X-ray, ultrasound), and laboratory tests at in-network facilities. Cardiology and dermatology specialist visits require a primary-care referral unless marked as direct-access in the member portal. Coverage limits: up to 12 specialist visits per calendar year; emergency room visits covered after a $150 copay; prescription drugs Tier 1 generic $10, Tier 2 preferred brand $35, Tier 3 specialty 20% coinsurance up to $250 per fill. Pre-authorization is required for MRI, CT scans, elective procedures, and hospital admissions. Submit requests at least 5 business days before the scheduled service. Exclusions: cosmetic procedures, experimental treatments, and services received outside the approved provider network unless authorized for emergency care. For claims and eligibility verification, members may contact insurance support at insurance@superclinic.example or call 1-800-555-0199.',
    0,
    '{"category": "insurance", "plan_code": "SC-HP-2026", "tags": ["coverage", "copay", "pre-auth"]}',
    TRUE, 'seed', 'seed'
);

COMMIT;

-- Verify row counts
SELECT 'specialties'           AS table_name, COUNT(*) AS rows FROM specialties
UNION ALL SELECT 'patients',              COUNT(*) FROM patients
UNION ALL SELECT 'doctors',               COUNT(*) FROM doctors
UNION ALL SELECT 'availability',          COUNT(*) FROM availability
UNION ALL SELECT 'appointments',          COUNT(*) FROM appointments
UNION ALL SELECT 'conversation_sessions', COUNT(*) FROM conversation_sessions
UNION ALL SELECT 'conversation_history',  COUNT(*) FROM conversation_history
UNION ALL SELECT 'knowledge_documents',   COUNT(*) FROM knowledge_documents;
