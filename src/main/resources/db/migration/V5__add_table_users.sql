create table calendar.calendar_users
(
    id        bigserial primary key,
    username  text not null unique,
    password  text not null,
    first_name      text,
    last_name text,
    created_at       timestamptz,
    updated_at       timestamptz
);

create table calendar.user_roles(
    user_id bigint references calendar.calendar_users(id),
    role text not null
);
