alter table calendar.user_sequences
add constraint unique_user_id_sequence_id unique (user_id, sequence_id);
