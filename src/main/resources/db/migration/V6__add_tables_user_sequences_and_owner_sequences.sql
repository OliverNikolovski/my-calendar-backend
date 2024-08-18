create table calendar.owner_sequences
(
    owner_id    bigint not null references calendar.calendar_users (id),
    sequence_id text   not null,
    primary key (owner_id, sequence_id)
);

create table calendar.user_sequences
(
    id          bigserial primary key,
    user_id     bigint not null references calendar.calendar_users (id),
    sequence_id text   not null,
    created_at  timestamptz,
    updated_at  timestamptz
);
