alter table calendar.scheduled_notifications
add column receiver_id bigint not null references calendar.users(id);
