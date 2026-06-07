-- Integration test seed data (fixed UUIDs, relative availability dates)
BEGIN;

INSERT INTO specialties (id, code, name, description, created_by, updated_by) VALUES
    ('a1000000-0000-4000-8000-000000000001', 'CARDIOLOGY',  'Cardiology',  'Heart and cardiovascular system', 'test', 'test'),
    ('a1000000-0000-4000-8000-000000000002', 'DERMATOLOGY', 'Dermatology', 'Skin conditions', 'test', 'test')
ON CONFLICT (id) DO NOTHING;

INSERT INTO patients (id, external_subject_id, email, phone, full_name, date_of_birth, created_by, updated_by) VALUES
    ('b2000000-0000-4000-8000-000000000001', 'test|patient001', 'alice@test.com', '+1-555-0101', 'Alice Johnson', '1988-03-15', 'test', 'test')
ON CONFLICT (id) DO NOTHING;

INSERT INTO doctors (
    id, specialty_id, license_number, full_name, email, phone,
    bio, qualifications, languages, years_experience, rating_avg, consultation_fee,
    active, metadata, created_by, updated_by
) VALUES
(
    'c3000000-0000-4000-8000-000000000001',
    'a1000000-0000-4000-8000-000000000001',
    'MED-TEST-10001', 'Dr. Rajesh Sharma', 'r.sharma@test.com', '+1-555-1001',
    'Senior cardiologist specializing in preventive cardiology and hypertension.',
    'MD, DM Cardiology', 'English,Hindi', 18, 4.85, 150.00, TRUE,
    '{"location": "Building A, Floor 3"}', 'test', 'test'
),
(
    'c3000000-0000-4000-8000-000000000002',
    'a1000000-0000-4000-8000-000000000002',
    'MED-TEST-10002', 'Dr. Priya Nair', 'p.nair@test.com', '+1-555-1002',
    'Dermatologist specializing in clinical dermatology and eczema.',
    'MD, DDV', 'English,Malayalam', 12, 4.72, 120.00, TRUE,
    '{"location": "Building B, Floor 2"}', 'test', 'test'
)
ON CONFLICT (id) DO NOTHING;

-- Dr Sharma: open slot tomorrow
INSERT INTO availability (id, doctor_id, slot_start, slot_end, status, created_by, updated_by) VALUES
(
    'd4000000-0000-4000-8000-000000000010',
    'c3000000-0000-4000-8000-000000000001',
    (CURRENT_DATE + INTERVAL '1 day' + TIME '09:00') AT TIME ZONE 'UTC',
    (CURRENT_DATE + INTERVAL '1 day' + TIME '09:30') AT TIME ZONE 'UTC',
    'OPEN', 'test', 'test'
),
-- Dr Nair: open slot in 2 days (alternative doctor scenario)
(
    'd4000000-0000-4000-8000-000000000011',
    'c3000000-0000-4000-8000-000000000002',
    (CURRENT_DATE + INTERVAL '2 days' + TIME '14:00') AT TIME ZONE 'UTC',
    (CURRENT_DATE + INTERVAL '2 days' + TIME '14:30') AT TIME ZONE 'UTC',
    'OPEN', 'test', 'test'
),
-- Dr Sharma: alternative slot in 3 days (for suggestAlternativeSlots)
(
    'd4000000-0000-4000-8000-000000000012',
    'c3000000-0000-4000-8000-000000000001',
    (CURRENT_DATE + INTERVAL '3 days' + TIME '11:00') AT TIME ZONE 'UTC',
    (CURRENT_DATE + INTERVAL '3 days' + TIME '11:30') AT TIME ZONE 'UTC',
    'OPEN', 'test', 'test'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO knowledge_documents (
    id, source_type, source_id, title, content, chunk_index, metadata, published, created_by, updated_by
) VALUES
(
    'k7000000-0000-4000-8000-000000000001',
    'DOCTOR_PROFILE',
    'c3000000-0000-4000-8000-000000000001',
    'Dr. Rajesh Sharma — Cardiologist',
    'Dr. Rajesh Sharma is a senior cardiologist at Super Clinic with 18 years of experience. Specializations include preventive cardiology, heart failure, and hypertension management.',
    0,
    '{"category": "doctor", "specialty_code": "CARDIOLOGY"}',
    TRUE, 'test', 'test'
),
(
    'k7000000-0000-4000-8000-000000000002',
    'FAQ',
    NULL,
    'How do I book an appointment?',
    'You can book an appointment through the AI assistant by describing your symptoms or preferred doctor. The assistant will show available slots and confirm your booking.',
    0,
    '{"category": "faq", "tags": ["booking"]}',
    TRUE, 'test', 'test'
),
(
    'k7000000-0000-4000-8000-000000000003',
    'FAQ',
    NULL,
    'What are the clinic operating hours?',
    'Super Clinic is open Monday to Saturday, 8:00 AM to 8:00 PM. Sunday outpatient hours are 9:00 AM to 1:00 PM.',
    0,
    '{"category": "faq", "tags": ["hours"]}',
    TRUE, 'test', 'test'
)
ON CONFLICT (id) DO NOTHING;

COMMIT;
