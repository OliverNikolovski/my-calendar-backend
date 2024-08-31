create type calendar.scheduled_notification_status as enum('PENDING', 'PROCESSED');

create table calendar.scheduled_notifications(
    id bigserial primary key,
    scheduled_time timestamptz not null,
    event_id bigint not null references calendar.calendar_events(id),
    status calendar.scheduled_notification_status not null default 'PENDING'::calendar.scheduled_notification_status,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
