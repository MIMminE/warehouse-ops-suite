alter table picking_tasks add column outbound_order_line_allocation_id bigint;

alter table picking_tasks
    add constraint fk_picking_tasks_outbound_allocation
    foreign key (outbound_order_line_allocation_id) references outbound_order_line_allocations (id);

create index idx_picking_tasks_outbound_allocation on picking_tasks (outbound_order_line_allocation_id);
