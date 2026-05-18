create table app_schema_version_marker (
    id bigint primary key,
    description varchar(255) not null
);

insert into app_schema_version_marker (id, description)
values (1, 'initial migration placeholder');

