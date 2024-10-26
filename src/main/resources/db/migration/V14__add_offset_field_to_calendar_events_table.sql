alter table calendar.calendar_events
add column offset_in_seconds integer not null default 0;
