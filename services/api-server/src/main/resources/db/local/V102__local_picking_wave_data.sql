insert into outbound_waves (
    id,
    wave_no,
    client_company_id,
    warehouse_id,
    status,
    requested_by,
    memo,
    created_at
)
values
    (1, 'WAVE-0518-AM-01', 1, 1, 'COMPLETED', '최유진', 'A 고객사 오전 피킹', timestamp '2026-05-18 09:10:00'),
    (2, 'WAVE-0518-AM-02', 2, 1, 'PICKING', '오세린', 'B 고객사 오전 피킹', timestamp '2026-05-18 10:30:00'),
    (3, 'WAVE-0518-PM-01', 3, 1, 'READY', 'DPS', 'DPS 구역 오후 피킹', timestamp '2026-05-19 14:30:00')
on conflict (wave_no) do nothing;

insert into picking_tasks (
    id,
    task_no,
    outbound_wave_id,
    outbound_order_line_id,
    warehouse_id,
    source_location_id,
    sku_id,
    status,
    requested_quantity,
    picked_quantity,
    assigned_worker,
    created_at
)
values
    (1, 'PICK-260518-0001', 1, 1, 1, 1, 1, 'COMPLETED', 2, 2, '최유진', timestamp '2026-05-18 09:15:00'),
    (2, 'PICK-260518-0002', 1, 2, 1, 3, 2, 'COMPLETED', 1, 1, '최유진', timestamp '2026-05-18 09:16:00'),
    (3, 'PICK-260518-0003', 2, 3, 1, 2, 3, 'PICKING', 1, 0, '오세린', timestamp '2026-05-18 10:35:00'),
    (4, 'PICK-260518-0004', 2, 4, 1, 1, 1, 'PICKING', 2, 0, '오세린', timestamp '2026-05-18 10:36:00'),
    (5, 'PICK-260519-0005', 3, 5, 1, 4, 4, 'READY', 6, 0, 'DPS', timestamp '2026-05-19 14:35:00'),
    (6, 'PICK-260519-0006', 3, 6, 1, 3, 2, 'READY', 2, 0, 'DPS', timestamp '2026-05-19 14:36:00')
on conflict (task_no) do nothing;

select setval(pg_get_serial_sequence('outbound_waves', 'id'), greatest((select max(id) from outbound_waves), 1));
select setval(pg_get_serial_sequence('picking_tasks', 'id'), greatest((select max(id) from picking_tasks), 1));
