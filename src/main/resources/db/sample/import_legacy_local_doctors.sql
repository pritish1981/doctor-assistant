-- Import doctors and availability from legacy local PostgreSQL (doctorassistant DB)
-- Maps legacy columns: doctor_name, speciality, department -> app doctors + specialties

BEGIN;

-- Ensure specialty rows exist (legacy names may differ slightly)
INSERT INTO specialties (id, code, name, description, created_by, updated_by) VALUES
    ('a1000000-0000-4000-8000-000000000001', 'CARDIOLOGY',    'Cardiology',    'Heart and cardiovascular system', 'legacy-import', 'legacy-import'),
    ('a1000000-0000-4000-8000-000000000002', 'DERMATOLOGY',   'Dermatology',   'Skin conditions', 'legacy-import', 'legacy-import'),
    ('a1000000-0000-4000-8000-000000000005', 'ORTHOPEDICS',   'Orthopedics',   'Bones, joints, and musculoskeletal system', 'legacy-import', 'legacy-import')
ON CONFLICT (id) DO NOTHING;

INSERT INTO doctors (
    id, specialty_id, license_number, full_name, email, phone,
    bio, qualifications, languages, years_experience, rating_avg, consultation_fee,
    active, metadata, created_by, updated_by
) VALUES
(
    'c4000000-0000-4000-8000-000000000001',
    'a1000000-0000-4000-8000-000000000005',
    'MED-LEGACY-10001', 'Dr Ravi Kumar', NULL, NULL,
    'Orthopedic specialist imported from legacy clinic database.',
    NULL, 'English', 10, 4.50, 150.00, TRUE,
    '{"legacy_doctor_id": 1, "department": "Orthopedics", "source": "local-postgres"}',
    'legacy-import', 'legacy-import'
),
(
    'c4000000-0000-4000-8000-000000000002',
    'a1000000-0000-4000-8000-000000000005',
    'MED-LEGACY-10002', 'Dr John Mathew', NULL, NULL,
    'Orthopedic specialist imported from legacy clinic database.',
    NULL, 'English', 12, 4.60, 150.00, TRUE,
    '{"legacy_doctor_id": 2, "department": "Orthopedics", "source": "local-postgres"}',
    'legacy-import', 'legacy-import'
),
(
    'c4000000-0000-4000-8000-000000000003',
    'a1000000-0000-4000-8000-000000000002',
    'MED-LEGACY-10003', 'Dr Sarah Wilson', NULL, NULL,
    'Dermatology specialist imported from legacy clinic database.',
    NULL, 'English', 8, 4.40, 120.00, TRUE,
    '{"legacy_doctor_id": 3, "department": "Skin Care", "source": "local-postgres"}',
    'legacy-import', 'legacy-import'
),
(
    'c4000000-0000-4000-8000-000000000004',
    'a1000000-0000-4000-8000-000000000001',
    'MED-LEGACY-10004', 'Dr Michael Brown', NULL, NULL,
    'Cardiology specialist imported from legacy clinic database.',
    NULL, 'English', 15, 4.70, 160.00, TRUE,
    '{"legacy_doctor_id": 4, "department": "Heart Care", "source": "local-postgres"}',
    'legacy-import', 'legacy-import'
)
ON CONFLICT (id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    specialty_id = EXCLUDED.specialty_id,
    active = EXCLUDED.active,
    metadata = EXCLUDED.metadata,
    updated_by = EXCLUDED.updated_by,
    updated_at = NOW();

-- Legacy availability (relative dates — spread across today + next 6 days)
INSERT INTO availability (id, doctor_id, slot_start, slot_end, status, created_by, updated_by) VALUES
-- Dr Ravi Kumar (Orthopedics): today + upcoming days
('d5000000-0000-4000-8000-000000000001', 'c4000000-0000-4000-8000-000000000001', (CURRENT_DATE + TIME '11:00') AT TIME ZONE 'UTC', (CURRENT_DATE + TIME '11:30') AT TIME ZONE 'UTC', 'OPEN', 'legacy-import', 'legacy-import'),
('d5000000-0000-4000-8000-000000000002', 'c4000000-0000-4000-8000-000000000001', (CURRENT_DATE + TIME '11:30') AT TIME ZONE 'UTC', (CURRENT_DATE + TIME '12:00') AT TIME ZONE 'UTC', 'OPEN', 'legacy-import', 'legacy-import'),
('d5000000-0000-4000-8000-000000000003', 'c4000000-0000-4000-8000-000000000001', (CURRENT_DATE + INTERVAL '2 days' + TIME '12:00') AT TIME ZONE 'UTC', (CURRENT_DATE + INTERVAL '2 days' + TIME '12:30') AT TIME ZONE 'UTC', 'OPEN', 'legacy-import', 'legacy-import'),
('d5000000-0000-4000-8000-000000000004', 'c4000000-0000-4000-8000-000000000002', (CURRENT_DATE + TIME '10:00') AT TIME ZONE 'UTC', (CURRENT_DATE + TIME '10:30') AT TIME ZONE 'UTC', 'OPEN', 'legacy-import', 'legacy-import'),
('d5000000-0000-4000-8000-000000000005', 'c4000000-0000-4000-8000-000000000002', (CURRENT_DATE + INTERVAL '1 day' + TIME '10:30') AT TIME ZONE 'UTC', (CURRENT_DATE + INTERVAL '1 day' + TIME '11:00') AT TIME ZONE 'UTC', 'OPEN', 'legacy-import', 'legacy-import'),
('d5000000-0000-4000-8000-000000000006', 'c4000000-0000-4000-8000-000000000003', (CURRENT_DATE + INTERVAL '1 day' + TIME '15:00') AT TIME ZONE 'UTC', (CURRENT_DATE + INTERVAL '1 day' + TIME '15:30') AT TIME ZONE 'UTC', 'OPEN', 'legacy-import', 'legacy-import'),
('d5000000-0000-4000-8000-000000000007', 'c4000000-0000-4000-8000-000000000003', (CURRENT_DATE + INTERVAL '3 days' + TIME '15:30') AT TIME ZONE 'UTC', (CURRENT_DATE + INTERVAL '3 days' + TIME '16:00') AT TIME ZONE 'UTC', 'OPEN', 'legacy-import', 'legacy-import'),
('d5000000-0000-4000-8000-000000000008', 'c4000000-0000-4000-8000-000000000004', (CURRENT_DATE + INTERVAL '2 days' + TIME '16:00') AT TIME ZONE 'UTC', (CURRENT_DATE + INTERVAL '2 days' + TIME '16:30') AT TIME ZONE 'UTC', 'OPEN', 'legacy-import', 'legacy-import')
ON CONFLICT (id) DO UPDATE SET
    slot_start = EXCLUDED.slot_start,
    slot_end = EXCLUDED.slot_end,
    status = EXCLUDED.status,
    updated_by = EXCLUDED.updated_by,
    updated_at = NOW();

COMMIT;

SELECT s.code, d.full_name
FROM doctors d
JOIN specialties s ON d.specialty_id = s.id
WHERE d.metadata->>'source' = 'local-postgres'
ORDER BY d.full_name;
