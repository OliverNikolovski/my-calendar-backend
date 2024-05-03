create schema calendar;

create table calendar.repeating_patterns
(
    id               bigserial primary key,
    frequency        smallint not null,
    week_days        integer[],
    set_pos          integer,
    interval         integer,
    occurrence_count integer,
    rrule_text       text,
    rrule_string     text,
    start            timestamptz,
    until            timestamptz,
    created_at       timestamptz,
    updated_at       timestamptz
);

create table calendar.calendar_events
(
    id                   bigserial primary key,
    title                text,
    description          text,
    start_date           timestamptz not null,
    end_date             timestamptz,
    duration             integer,
    repeating_pattern_id bigint references calendar.repeating_patterns (id),
    parent_id            bigint references calendar.calendar_events (id),
    created_at           timestamptz,
    updated_at           timestamptz
);
