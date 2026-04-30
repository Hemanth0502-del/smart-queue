INSERT IGNORE INTO categories (name, description, active, created_at) VALUES
('Hospital', 'Patient registration, consultation, pharmacy, and billing queues.', true, CURRENT_TIMESTAMP),
('Bank', 'Account, cash, loan, and customer service queues.', true, CURRENT_TIMESTAMP),
('College', 'Admissions, fee payment, examination, and student support queues.', true, CURRENT_TIMESTAMP),
('Government Office', 'Citizen services, documentation, verification, and help desk queues.', true, CURRENT_TIMESTAMP),
('Restaurant', 'Dine-in, takeaway, billing, and reservation queues.', true, CURRENT_TIMESTAMP),
('Service Center', 'Repair intake, diagnostics, billing, and delivery queues.', true, CURRENT_TIMESTAMP);

INSERT IGNORE INTO counters (name, code, category_id, status, average_service_time_in_minutes, active) VALUES
('Registration Desk', 'HOS-C01', (SELECT id FROM categories WHERE name = 'Hospital'), 'OPEN', 8, true),
('Consultation Desk', 'HOS-C02', (SELECT id FROM categories WHERE name = 'Hospital'), 'OPEN', 12, true),
('Pharmacy Counter', 'HOS-C03', (SELECT id FROM categories WHERE name = 'Hospital'), 'CLOSED', 6, true),
('Cash Deposit Counter', 'BNK-C01', (SELECT id FROM categories WHERE name = 'Bank'), 'OPEN', 7, true),
('Account Services', 'BNK-C02', (SELECT id FROM categories WHERE name = 'Bank'), 'OPEN', 10, true),
('Loan Enquiry Desk', 'BNK-C03', (SELECT id FROM categories WHERE name = 'Bank'), 'PAUSED', 15, true),
('Admission Desk', 'COL-C01', (SELECT id FROM categories WHERE name = 'College'), 'OPEN', 9, true),
('Fee Payment Counter', 'COL-C02', (SELECT id FROM categories WHERE name = 'College'), 'OPEN', 6, true),
('Examination Cell', 'COL-C03', (SELECT id FROM categories WHERE name = 'College'), 'CLOSED', 11, true),
('Document Verification', 'GOV-C01', (SELECT id FROM categories WHERE name = 'Government Office'), 'OPEN', 13, true),
('Citizen Help Desk', 'GOV-C02', (SELECT id FROM categories WHERE name = 'Government Office'), 'OPEN', 8, true),
('Certificate Counter', 'GOV-C03', (SELECT id FROM categories WHERE name = 'Government Office'), 'PAUSED', 12, true),
('Dine-in Queue Desk', 'RES-C01', (SELECT id FROM categories WHERE name = 'Restaurant'), 'OPEN', 5, true),
('Takeaway Counter', 'RES-C02', (SELECT id FROM categories WHERE name = 'Restaurant'), 'OPEN', 4, true),
('Billing Counter', 'RES-C03', (SELECT id FROM categories WHERE name = 'Restaurant'), 'OPEN', 3, true),
('Repair Intake Desk', 'SVC-C01', (SELECT id FROM categories WHERE name = 'Service Center'), 'OPEN', 10, true),
('Diagnostics Counter', 'SVC-C02', (SELECT id FROM categories WHERE name = 'Service Center'), 'OPEN', 14, true),
('Delivery Counter', 'SVC-C03', (SELECT id FROM categories WHERE name = 'Service Center'), 'CLOSED', 7, true);
