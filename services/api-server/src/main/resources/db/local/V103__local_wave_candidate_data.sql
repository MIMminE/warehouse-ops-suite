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
    (5, 1, 1, 'OUT-20260519-0810', 'OMS-A-8010', 'API', 'ALLOCATED', '정하린', '010-1000-0010', '06235', '서울특별시 강남구 논현로 321', null, '오후 출고', date '2026-05-19', timestamp '2026-05-19 10:20:00')
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
    (7, 5, 1, 1, 3, 3, 0, 0),
    (8, 5, 2, 2, 2, 2, 0, 0)
on conflict (outbound_order_id, line_no) do nothing;

insert into outbound_order_line_allocations (
    id,
    outbound_order_line_id,
    inventory_id,
    location_id,
    sku_id,
    allocated_quantity,
    picked_quantity
)
values
    (1, 7, 1, 1, 1, 3, 0),
    (2, 8, 2, 3, 2, 2, 0)
on conflict (id) do nothing;

select setval(pg_get_serial_sequence('outbound_orders', 'id'), greatest((select max(id) from outbound_orders), 1));
select setval(pg_get_serial_sequence('outbound_order_lines', 'id'), greatest((select max(id) from outbound_order_lines), 1));
select setval(pg_get_serial_sequence('outbound_order_line_allocations', 'id'), greatest((select max(id) from outbound_order_line_allocations), 1));
