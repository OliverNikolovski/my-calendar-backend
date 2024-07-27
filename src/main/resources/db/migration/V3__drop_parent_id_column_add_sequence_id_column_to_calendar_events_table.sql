alter table calendar.calendar_events drop column parent_id;
alter table calendar.calendar_events add column sequence_id text not null default '';
