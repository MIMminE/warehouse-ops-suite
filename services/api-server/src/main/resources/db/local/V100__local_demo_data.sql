insert into client_companies (id, code, name, business_registration_no, contact_name, contact_email)
values
    (1, 'CLIENT-A', 'A 고객사', '100-00-00001', '김운영', 'ops-a@example.com'),
    (2, 'CLIENT-B', 'B 고객사', '100-00-00002', '박운영', 'ops-b@example.com'),
    (3, 'CLIENT-C', 'C 고객사', '100-00-00003', '이운영', 'ops-c@example.com')
on conflict (code) do nothing;

insert into warehouses (id, code, name)
values
    (1, 'WH-SEOUl-01', '수도권 1센터'),
    (2, 'WH-BUSAN-02', '부산 2센터')
on conflict (code) do nothing;

insert into locations (id, warehouse_id, code, name, zone)
values
    (1, 1, 'A-01-03', 'A동 1열 3단', 'A'),
    (2, 1, 'B-02-01', 'B동 2열 1단', 'B'),
    (3, 1, 'C-04-05', 'C동 4열 5단', 'C'),
    (4, 1, 'D-01-02', 'DPS 1열 2셀', 'DPS'),
    (5, 2, 'B2-C-01', '부산 C구역 1단', 'C')
on conflict (warehouse_id, code) do nothing;

insert into skus (id, client_company_id, code, name, barcode, unit)
values
    (1, 1, 'SKU-4012', 'Basic Tee / Black', '8800000004012', 'EA'),
    (2, 1, 'SKU-1024', 'Slim Bottle / Clear', '8800000001024', 'EA'),
    (3, 2, 'SKU-8801', 'Daily Cap / Navy', '8800000008801', 'EA'),
    (4, 3, 'SKU-7780', 'Pouch Set / Gray', '8800000007780', 'EA')
on conflict (client_company_id, code) do nothing;

insert into inventories (id, client_company_id, warehouse_id, location_id, sku_id, available_quantity, allocated_quantity)
values
    (1, 1, 1, 1, 1, 180, 24),
    (2, 1, 1, 3, 2, 96, 12),
    (3, 2, 1, 2, 3, 32, 8),
    (4, 3, 1, 4, 4, 64, 6)
on conflict (client_company_id, location_id, sku_id) do nothing;

insert into outbound_orders (
    id,
    client_company_id,
    warehouse_id,
    outbound_order_no,
    external_reference_no,
    intake_source,
    status,
    receiver_name,
    receiver_phone,
    zip_code,
    address1,
    address2,
    delivery_memo,
    requested_ship_date,
    ordered_at
)
values
    (1, 1, 1, 'OUT-20260518-0801', 'OMS-A-8001', 'API', 'ALLOCATED', '김서연', '010-1000-0001', '06234', '서울특별시 강남구 테헤란로 123', '101동 1201호', '문 앞 배송', date '2026-05-18', timestamp '2026-05-18 08:01:00'),
    (2, 2, 1, 'OUT-20260518-0802', 'CSV-B-8002', 'CSV_UPLOAD', 'RECEIVED', '박민준', '010-1000-0002', '10401', '경기도 고양시 일산동구 중앙로 456', null, null, date '2026-05-18', timestamp '2026-05-18 08:12:00'),
    (3, 3, 1, 'OUT-20260518-0803', 'EDI-C-8003', 'EDI_FILE', 'PICKING', '이하은', '010-1000-0003', '48058', '부산광역시 해운대구 센텀중앙로 78', '18층', '경비실 호출', date '2026-05-19', timestamp '2026-05-18 16:40:00'),
    (4, 1, 1, 'OUT-20260519-0804', 'MANUAL-A-8004', 'MANUAL', 'READY_TO_SHIP', '최도윤', '010-1000-0004', '04783', '서울특별시 성동구 왕십리로 22', null, '부재 시 문 앞', date '2026-05-19', timestamp '2026-05-19 09:05:00')
on conflict (client_company_id, outbound_order_no) do nothing;

insert into outbound_order_lines (
    id,
    outbound_order_id,
    line_no,
    sku_id,
    ordered_quantity,
    allocated_quantity,
    picked_quantity,
    packed_quantity
)
values
    (1, 1, 1, 1, 2, 2, 2, 0),
    (2, 1, 2, 2, 1, 1, 1, 0),
    (3, 2, 1, 3, 1, 0, 0, 0),
    (4, 2, 2, 1, 4, 2, 0, 0),
    (5, 3, 1, 4, 6, 6, 0, 0),
    (6, 4, 1, 2, 2, 2, 2, 2)
on conflict (outbound_order_id, line_no) do nothing;

select setval(pg_get_serial_sequence('client_companies', 'id'), greatest((select max(id) from client_companies), 1));
select setval(pg_get_serial_sequence('warehouses', 'id'), greatest((select max(id) from warehouses), 1));
select setval(pg_get_serial_sequence('locations', 'id'), greatest((select max(id) from locations), 1));
select setval(pg_get_serial_sequence('skus', 'id'), greatest((select max(id) from skus), 1));
select setval(pg_get_serial_sequence('inventories', 'id'), greatest((select max(id) from inventories), 1));
select setval(pg_get_serial_sequence('outbound_orders', 'id'), greatest((select max(id) from outbound_orders), 1));
select setval(pg_get_serial_sequence('outbound_order_lines', 'id'), greatest((select max(id) from outbound_order_lines), 1));
