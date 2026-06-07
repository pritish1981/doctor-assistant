-- Minimal dev bootstrap: default frontend patient for New chat
INSERT INTO patients (
    id, external_subject_id, email, phone, full_name, date_of_birth, created_by, updated_by
) VALUES (
    'b2000000-0000-4000-8000-000000000001',
    'auth0|patient001',
    'alice.johnson@email.com',
    '+1-555-0101',
    'Alice Johnson',
    '1988-03-15',
    'seed',
    'seed'
)
ON CONFLICT (id) DO NOTHING;
