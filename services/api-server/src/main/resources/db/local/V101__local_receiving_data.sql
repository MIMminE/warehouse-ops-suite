insert into receiving_orders (
    id,
    receiving_no,
    client_company_id,
    warehouse_id,
    status,
    supplier_name,
    requested_by,
    memo,
    created_at
)
values
    (1, 'RCV-20260518-001', 1, 1, 'RECEIVING', '남양주 공급처', '한지훈', 'PDA 검수 진행 중', timestamp '2026-05-18 10:12:00'),
    (2, 'RCV-20260518-002', 2, 1, 'COMPLETED', '인천 공급처', '오세린', '전량 적치 완료', timestamp '2026-05-18 11:08:00'),
    (3, 'RCV-20260518-003', 1, 2, 'PUTAWAY', '김해 공급처', '김도현', '부산 센터 분할 적치', timestamp '2026-05-19 13:24:00'),
    (4, 'RCV-20260519-004', 3, 1, 'REQUESTED', '시흥 공급처', '문하늘', '입고 예정', timestamp '2026-05-19 09:30:00')
on conflict (receiving_no) do nothing;

insert into receiving_order_lines (
    id,
    receiving_order_id,
    line_no,
    sku_id,
    requested_quantity,
    received_quantity,
    created_at
)
values
    (1, 1, 1, 1, 120, 96, timestamp '2026-05-18 10:12:00'),
    (2, 2, 1, 3, 80, 80, timestamp '2026-05-18 11:08:00'),
    (3, 3, 1, 2, 240, 210, timestamp '2026-05-19 13:24:00'),
    (4, 4, 1, 4, 160, 0, timestamp '2026-05-19 09:30:00')
on conflict (receiving_order_id, line_no) do nothing;

insert into putaway_tasks (
    id,
    task_no,
    receiving_order_line_id,
    warehouse_id,
    target_location_id,
    sku_id,
    status,
    putaway_quantity,
    assigned_worker,
    created_at
)
values
    (1, 'PTW-20260518-001-01', 1, 1, 1, 1, 'COMPLETED', 72, '한지훈', timestamp '2026-05-18 10:42:00'),
    (2, 'PTW-20260518-002-01', 2, 1, 2, 3, 'COMPLETED', 80, '오세린', timestamp '2026-05-18 11:08:00'),
    (3, 'PTW-20260518-003-01', 3, 2, 5, 2, 'PUTTING_AWAY', 120, '김도현', timestamp '2026-05-19 13:24:00')
on conflict (task_no) do nothing;

select setval(pg_get_serial_sequence('receiving_orders', 'id'), greatest((select max(id) from receiving_orders), 1));
select setval(pg_get_serial_sequence('receiving_order_lines', 'id'), greatest((select max(id) from receiving_order_lines), 1));
select setval(pg_get_serial_sequence('putaway_tasks', 'id'), greatest((select max(id) from putaway_tasks), 1));
