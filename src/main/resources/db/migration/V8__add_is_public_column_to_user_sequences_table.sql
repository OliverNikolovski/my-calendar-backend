alter table calendar.user_sequences
add column is_public boolean not null default false;
