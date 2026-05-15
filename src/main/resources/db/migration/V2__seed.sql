-- Seed data: 3 sites, 1 admin, 1 auditor, 3 team leads, 9 workers, 20 equipment, 10 licenses
-- Passwords: admin123! / Lead123! / Worker1! / Audit1!  (BCrypt cost=10)

-- ─── Sites ────────────────────────────────────────────────────────────────────
INSERT INTO sites (id, name, address, city, created_at)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'Київ — Головний офіс', 'вул. Хрещатик, 1',   'Київ',   now()),
  ('22222222-2222-2222-2222-222222222222', 'Львів — Філія',        'вул. Свободи, 10',   'Львів',  now()),
  ('33333333-3333-3333-3333-333333333333', 'Харків — Центр',       'вул. Сумська, 55',   'Харків', now())
ON CONFLICT (id) DO NOTHING;

-- ─── Users ────────────────────────────────────────────────────────────────────
-- Global roles (no site)
INSERT INTO users (id, username, email, full_name, password_hash, role, site_id, active, created_at, updated_at)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'admin',   'admin@inventory.local',   'Адмін Системи',
   '$2y$10$ES.78j2brzSrHZR2HW9xtOdOud.wshkEyanr6pesgHtjjN1fYKDnu', 'ADMIN',   NULL, true, now(), now()),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'auditor', 'auditor@inventory.local', 'Аудитор Системи',
   '$2y$10$1e7tcufbVtUl3GJ2Fc2PU.TRCs0TMIxUI827OZgxM.NXdM.EwVLcu', 'AUDITOR', NULL, true, now(), now())
ON CONFLICT (id) DO NOTHING;

-- Team Leads (one per site)
INSERT INTO users (id, username, email, full_name, password_hash, role, site_id, active, created_at, updated_at)
VALUES
  ('cc111111-1111-1111-1111-111111111111', 'teamlead1', 'teamlead1@inventory.local', 'Тімлід Київ',
   '$2y$10$gtcne9qJFqN3X.cdf3FMB.9ycLWWkvCPCf3m2aU2VnvMHhy8bLMca', 'TEAM_LEAD',
   '11111111-1111-1111-1111-111111111111', true, now(), now()),
  ('cc222222-2222-2222-2222-222222222222', 'teamlead2', 'teamlead2@inventory.local', 'Тімлід Львів',
   '$2y$10$gtcne9qJFqN3X.cdf3FMB.9ycLWWkvCPCf3m2aU2VnvMHhy8bLMca', 'TEAM_LEAD',
   '22222222-2222-2222-2222-222222222222', true, now(), now()),
  ('cc333333-3333-3333-3333-333333333333', 'teamlead3', 'teamlead3@inventory.local', 'Тімлід Харків',
   '$2y$10$gtcne9qJFqN3X.cdf3FMB.9ycLWWkvCPCf3m2aU2VnvMHhy8bLMca', 'TEAM_LEAD',
   '33333333-3333-3333-3333-333333333333', true, now(), now())
ON CONFLICT (id) DO NOTHING;

-- Update site team_lead_id
UPDATE sites SET team_lead_id = 'cc111111-1111-1111-1111-111111111111'
  WHERE id = '11111111-1111-1111-1111-111111111111';
UPDATE sites SET team_lead_id = 'cc222222-2222-2222-2222-222222222222'
  WHERE id = '22222222-2222-2222-2222-222222222222';
UPDATE sites SET team_lead_id = 'cc333333-3333-3333-3333-333333333333'
  WHERE id = '33333333-3333-3333-3333-333333333333';

-- Workers — 3 per site
INSERT INTO users (id, username, email, full_name, password_hash, role, site_id, active, created_at, updated_at)
VALUES
  ('dd111111-1111-1111-1111-111111111111', 'worker1', 'worker1@inventory.local', 'Іваненко Іван',
   '$2y$10$9lMX6keXi5UMRQlAO.A4nO3fMIUXhLcmQfZcWsMYURMasZXqAnH5.', 'WORKER',
   '11111111-1111-1111-1111-111111111111', true, now(), now()),
  ('dd111111-1111-1111-1111-222222222222', 'worker2', 'worker2@inventory.local', 'Петренко Петро',
   '$2y$10$9lMX6keXi5UMRQlAO.A4nO3fMIUXhLcmQfZcWsMYURMasZXqAnH5.', 'WORKER',
   '11111111-1111-1111-1111-111111111111', true, now(), now()),
  ('dd111111-1111-1111-1111-333333333333', 'worker3', 'worker3@inventory.local', 'Сидоренко Сергій',
   '$2y$10$9lMX6keXi5UMRQlAO.A4nO3fMIUXhLcmQfZcWsMYURMasZXqAnH5.', 'WORKER',
   '11111111-1111-1111-1111-111111111111', true, now(), now()),
  ('dd222222-2222-2222-2222-111111111111', 'worker4', 'worker4@inventory.local', 'Коваленко Олена',
   '$2y$10$9lMX6keXi5UMRQlAO.A4nO3fMIUXhLcmQfZcWsMYURMasZXqAnH5.', 'WORKER',
   '22222222-2222-2222-2222-222222222222', true, now(), now()),
  ('dd222222-2222-2222-2222-222222222222', 'worker5', 'worker5@inventory.local', 'Мельник Микола',
   '$2y$10$9lMX6keXi5UMRQlAO.A4nO3fMIUXhLcmQfZcWsMYURMasZXqAnH5.', 'WORKER',
   '22222222-2222-2222-2222-222222222222', true, now(), now()),
  ('dd222222-2222-2222-2222-333333333333', 'worker6', 'worker6@inventory.local', 'Шевченко Тарас',
   '$2y$10$9lMX6keXi5UMRQlAO.A4nO3fMIUXhLcmQfZcWsMYURMasZXqAnH5.', 'WORKER',
   '22222222-2222-2222-2222-222222222222', true, now(), now()),
  ('dd333333-3333-3333-3333-111111111111', 'worker7', 'worker7@inventory.local', 'Бондаренко Ірина',
   '$2y$10$9lMX6keXi5UMRQlAO.A4nO3fMIUXhLcmQfZcWsMYURMasZXqAnH5.', 'WORKER',
   '33333333-3333-3333-3333-333333333333', true, now(), now()),
  ('dd333333-3333-3333-3333-222222222222', 'worker8', 'worker8@inventory.local', 'Кравченко Дмитро',
   '$2y$10$9lMX6keXi5UMRQlAO.A4nO3fMIUXhLcmQfZcWsMYURMasZXqAnH5.', 'WORKER',
   '33333333-3333-3333-3333-333333333333', true, now(), now()),
  ('dd333333-3333-3333-3333-333333333333', 'worker9', 'worker9@inventory.local', 'Лисенко Ганна',
   '$2y$10$9lMX6keXi5UMRQlAO.A4nO3fMIUXhLcmQfZcWsMYURMasZXqAnH5.', 'WORKER',
   '33333333-3333-3333-3333-333333333333', true, now(), now())
ON CONFLICT (id) DO NOTHING;

-- ─── Equipment (20 items) ─────────────────────────────────────────────────────
INSERT INTO equipment (id, inventory_number, name, type, manufacturer, model, serial_number,
                       status, purchase_date, warranty_until, price, site_id, assigned_user_id,
                       notes, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'EQ-2024-00001', 'Ноутбук Dell XPS 15',    'LAPTOP',  'Dell',    'XPS 15',       'SN-DELL-001', 'ASSIGNED', '2024-01-15', '2027-01-15', 65000.00, '11111111-1111-1111-1111-111111111111', 'dd111111-1111-1111-1111-111111111111', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00002', 'Ноутбук Lenovo ThinkPad', 'LAPTOP',  'Lenovo',  'ThinkPad X1',  'SN-LEN-002',  'ASSIGNED', '2024-02-01', '2027-02-01', 55000.00, '11111111-1111-1111-1111-111111111111', 'dd111111-1111-1111-1111-222222222222', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00003', 'Монітор LG 27"',          'MONITOR', 'LG',      '27UK850',      'SN-LG-003',   'ASSIGNED', '2024-01-20', '2026-01-20', 18000.00, '11111111-1111-1111-1111-111111111111', 'dd111111-1111-1111-1111-111111111111', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00004', 'Монітор Samsung 24"',     'MONITOR', 'Samsung', 'S24F350',      'SN-SAM-004',  'IN_STOCK', '2024-03-10', '2026-03-10', 9500.00,  '11111111-1111-1111-1111-111111111111', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00005', 'Принтер HP LaserJet',     'PRINTER', 'HP',      'LaserJet Pro', 'SN-HP-005',   'ASSIGNED', '2023-11-05', '2025-11-05', 12000.00, '11111111-1111-1111-1111-111111111111', 'dd111111-1111-1111-1111-333333333333', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00006', 'Комутатор Cisco',         'NETWORK', 'Cisco',   'Catalyst 2960','SN-CIS-006',  'IN_STOCK', '2022-06-15', '2027-06-15', 45000.00, '11111111-1111-1111-1111-111111111111', NULL, 'Серверна кімната', now(), now()),
  (gen_random_uuid(), 'EQ-2024-00007', 'MacBook Pro 14"',         'LAPTOP',  'Apple',   'MacBook Pro',  'SN-APL-007',  'ASSIGNED', '2024-04-01', '2027-04-01', 85000.00, '22222222-2222-2222-2222-222222222222', 'dd222222-2222-2222-2222-111111111111', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00008', 'Ноутбук HP EliteBook',    'LAPTOP',  'HP',      'EliteBook 840','SN-HP-008',   'ASSIGNED', '2024-01-30', '2027-01-30', 48000.00, '22222222-2222-2222-2222-222222222222', 'dd222222-2222-2222-2222-222222222222', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00009', 'Монітор Dell 32"',        'MONITOR', 'Dell',    'U3223QE',      'SN-DEL-009',  'IN_STOCK', '2024-02-15', '2027-02-15', 28000.00, '22222222-2222-2222-2222-222222222222', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00010', 'Телефон Cisco IP',        'PHONE',   'Cisco',   'CP-8841',      'SN-CIS-010',  'ASSIGNED', '2023-09-01', '2025-09-01',  5500.00, '22222222-2222-2222-2222-222222222222', 'dd222222-2222-2222-2222-333333333333', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00011', 'Роутер MikroTik',         'NETWORK', 'MikroTik','RB4011',       'SN-MIK-011',  'IN_STOCK', '2023-07-10', '2026-07-10', 15000.00, '22222222-2222-2222-2222-222222222222', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00012', 'Ноутбук ASUS ProArt',     'LAPTOP',  'ASUS',    'ProArt 16',    'SN-ASU-012',  'ASSIGNED', '2024-03-20', '2027-03-20', 72000.00, '33333333-3333-3333-3333-333333333333', 'dd333333-3333-3333-3333-111111111111', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00013', 'Монітор ASUS 27"',        'MONITOR', 'ASUS',    'ProArt PA278', 'SN-ASU-013',  'ASSIGNED', '2024-01-05', '2027-01-05', 22000.00, '33333333-3333-3333-3333-333333333333', 'dd333333-3333-3333-3333-222222222222', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00014', 'Принтер Epson L6190',     'PRINTER', 'Epson',   'L6190',        'SN-EPS-014',  'IN_STOCK', '2023-12-01', '2025-12-01',  9800.00, '33333333-3333-3333-3333-333333333333', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00015', 'Комутатор HP Aruba',      'NETWORK', 'HP',      'Aruba 2930F',  'SN-HP-015',   'IN_STOCK', '2023-05-20', '2026-05-20', 35000.00, '33333333-3333-3333-3333-333333333333', NULL, 'Серверна', now(), now()),
  (gen_random_uuid(), 'EQ-2024-00016', 'Робоча станція Dell',     'DESKTOP', 'Dell',    'Precision 5860','SN-DEL-016', 'ASSIGNED', '2024-02-28', '2027-02-28',120000.00, '11111111-1111-1111-1111-111111111111', 'dd111111-1111-1111-1111-222222222222', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00017', 'Телефон Polycom',         'PHONE',   'Polycom', 'VVX 450',      'SN-POL-017',  'ASSIGNED', '2023-10-15', '2025-10-15',  4200.00, '22222222-2222-2222-2222-222222222222', 'dd222222-2222-2222-2222-111111111111', NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00018', 'Ноутбук MSI Creator',     'LAPTOP',  'MSI',     'Creator 17',   'SN-MSI-018',  'IN_STOCK', '2024-04-10', '2027-04-10', 68000.00, '33333333-3333-3333-3333-333333333333', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'EQ-2024-00019', 'Сервер HPE ProLiant',     'DESKTOP', 'HPE',     'DL380 Gen10',  'SN-HPE-019',  'IN_STOCK', '2023-01-01', '2028-01-01',250000.00, '11111111-1111-1111-1111-111111111111', NULL, 'ЦОД', now(), now()),
  (gen_random_uuid(), 'EQ-2023-00020', 'Принтер Canon MF',        'PRINTER', 'Canon',   'MF655Cdw',     'SN-CAN-020',  'DECOMMISSIONED','2021-03-01','2023-03-01',7000.00, '33333333-3333-3333-3333-333333333333', NULL, 'Знято з обліку', now(), now())
ON CONFLICT DO NOTHING;

-- ─── Licenses (10 items) ──────────────────────────────────────────────────────
INSERT INTO licenses (id, software_name, vendor, license_key, license_type, seats_total, seats_used,
                      purchase_date, expiration_date, cost, site_id, assigned_user_id, notes,
                      created_at, updated_at)
VALUES
  (gen_random_uuid(), 'Microsoft 365 Business', 'Microsoft', 'M365-KYIV-0001', 'SUBSCRIPTION', 25, 15,
   '2024-01-01', '2025-01-01', 45000.00, '11111111-1111-1111-1111-111111111111', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'Windows 11 Pro',         'Microsoft', 'WIN11-LIC-0002', 'OEM',          50, 20,
   '2023-06-01', NULL,          NULL,    '11111111-1111-1111-1111-111111111111', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'JetBrains All Products', 'JetBrains', 'JB-ALL-KYIV-003','SUBSCRIPTION', 10, 8,
   '2024-03-01', '2025-03-01', 35000.00, '11111111-1111-1111-1111-111111111111', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'Adobe Creative Cloud',   'Adobe',     'ADCC-KYIV-00004','SUBSCRIPTION',  5,  3,
   '2024-02-01', '2025-02-01', 60000.00, '11111111-1111-1111-1111-111111111111', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'Microsoft 365 Business', 'Microsoft', 'M365-LVIV-0005', 'SUBSCRIPTION', 20, 12,
   '2024-01-01', '2025-01-01', 36000.00, '22222222-2222-2222-2222-222222222222', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'Windows 11 Pro',         'Microsoft', 'WIN11-LVIV-0006','OEM',          30, 15,
   '2023-09-01', NULL,          NULL,    '22222222-2222-2222-2222-222222222222', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'Slack Business+',        'Slack',     'SLACK-LVIV-007', 'SUBSCRIPTION', 30, 18,
   '2024-04-01', '2025-04-01', 28000.00, '22222222-2222-2222-2222-222222222222', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'Zoom Business',          'Zoom',      'ZOOM-KHR-00008', 'SUBSCRIPTION', 50, 25,
   '2024-01-15', '2025-01-15', 22000.00, '33333333-3333-3333-3333-333333333333', NULL, NULL, now(), now()),
  (gen_random_uuid(), 'AutoCAD 2024',           'Autodesk',  'ACAD-KHR-00009', 'SUBSCRIPTION',  5,  5,
   '2024-02-01', '2025-02-01', 75000.00, '33333333-3333-3333-3333-333333333333', NULL, 'Вичерпані місця', now(), now()),
  (gen_random_uuid(), 'GitLab Ultimate',        'GitLab',    'GL-CORP-000010',  'SUBSCRIPTION',100, 42,
   '2024-03-01', '2025-03-01', 95000.00, '11111111-1111-1111-1111-111111111111', NULL, 'Корпоративна', now(), now())
ON CONFLICT DO NOTHING;
