-- A director can write the same board at several meetings. Tournament entries
-- are located by meeting and board; ordinary entries still belong to one writer.
ALTER TABLE board_entries DROP CONSTRAINT IF EXISTS board_entries_board_id_writer_id_key;

CREATE UNIQUE INDEX uq_standalone_board_entry_writer_board
    ON board_entries (board_id, writer_id)
    WHERE ns IS NULL AND ew IS NULL;
