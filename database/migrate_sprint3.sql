USE cinema_db_schema;

SET @sprint3_sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'seats'
          AND column_name = 'roomID'
    ),
    'SELECT ''seats.roomID already exists'' AS migration_note',
    'ALTER TABLE seats ADD COLUMN roomID BIGINT NULL'
);
PREPARE sprint3_statement FROM @sprint3_sql;
EXECUTE sprint3_statement;
DEALLOCATE PREPARE sprint3_statement;

INSERT INTO showrooms (name, capacity)
SELECT room.name, room.capacity
FROM (
    SELECT 'Auditorium 1' AS name, 48 AS capacity
    UNION ALL SELECT 'Auditorium 2', 48
    UNION ALL SELECT 'Auditorium 3', 48
) room
WHERE NOT EXISTS (
    SELECT 1 FROM showrooms existing WHERE existing.name = room.name
);

UPDATE seats
SET roomID = (
    SELECT roomID FROM showrooms WHERE name = 'Auditorium 1' ORDER BY roomID LIMIT 1
)
WHERE roomID IS NULL;

SET @sprint3_sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'seats'
          AND column_name = 'roomID'
          AND is_nullable = 'YES'
    ),
    'ALTER TABLE seats MODIFY roomID BIGINT NOT NULL',
    'SELECT ''seats.roomID is already NOT NULL'' AS migration_note'
);
PREPARE sprint3_statement FROM @sprint3_sql;
EXECUTE sprint3_statement;
DEALLOCATE PREPARE sprint3_statement;

SET @sprint3_sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.key_column_usage
        WHERE table_schema = DATABASE()
          AND table_name = 'seats'
          AND column_name = 'roomID'
          AND referenced_table_name = 'showrooms'
    ),
    'SELECT ''seats.roomID foreign key already exists'' AS migration_note',
    'ALTER TABLE seats ADD CONSTRAINT fk_seats_showroom FOREIGN KEY (roomID) REFERENCES showrooms(roomID) ON DELETE CASCADE'
);
PREPARE sprint3_statement FROM @sprint3_sql;
EXECUTE sprint3_statement;
DEALLOCATE PREPARE sprint3_statement;

SET @sprint3_sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'seats'
          AND index_name = 'unique_room_seat'
    ),
    'SELECT ''seats unique_room_seat index already exists'' AS migration_note',
    'ALTER TABLE seats ADD CONSTRAINT unique_room_seat UNIQUE (roomID, `row`, seatNumber)'
);
PREPARE sprint3_statement FROM @sprint3_sql;
EXECUTE sprint3_statement;
DEALLOCATE PREPARE sprint3_statement;

INSERT INTO seats (roomID, `row`, seatNumber)
SELECT showroom.roomID, seat_rows.seat_row, seat_numbers.seat_number
FROM showrooms showroom
CROSS JOIN (
    SELECT 'A' AS seat_row UNION ALL SELECT 'B' UNION ALL SELECT 'C'
    UNION ALL SELECT 'D' UNION ALL SELECT 'E' UNION ALL SELECT 'F'
) seat_rows
CROSS JOIN (
    SELECT 1 AS seat_number UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
) seat_numbers
WHERE showroom.name IN ('Auditorium 1', 'Auditorium 2', 'Auditorium 3')
  AND NOT EXISTS (
      SELECT 1
      FROM seats existing
      WHERE existing.roomID = showroom.roomID
        AND existing.`row` = seat_rows.seat_row
        AND existing.seatNumber = seat_numbers.seat_number
  );

INSERT INTO shows (dateTime, movieID, roomID)
SELECT DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY) + INTERVAL room_schedule.start_hour HOUR,
       (SELECT movie_id FROM movies WHERE title = 'The Batman' ORDER BY movie_id LIMIT 1),
       showroom.roomID
FROM (
    SELECT 'Auditorium 1' AS room_name, 14 AS start_hour
    UNION ALL SELECT 'Auditorium 2', 17
    UNION ALL SELECT 'Auditorium 3', 20
) room_schedule
JOIN showrooms showroom ON showroom.name = room_schedule.room_name
WHERE NOT EXISTS (
    SELECT 1 FROM shows existing
    WHERE existing.roomID = showroom.roomID
      AND existing.dateTime = DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY) + INTERVAL room_schedule.start_hour HOUR
);

INSERT INTO show_seats (showID, seatID, available)
SELECT showtime.showID,
       seat.seatID,
       NOT (
           (seat.`row` = 'B' AND seat.seatNumber IN (3, 4))
           OR (seat.`row` = 'C' AND seat.seatNumber IN (5, 6))
           OR (seat.`row` = 'D' AND seat.seatNumber IN (1, 2))
           OR (seat.`row` = 'E' AND seat.seatNumber IN (7, 8))
       )
FROM shows showtime
JOIN seats seat ON seat.roomID = showtime.roomID
WHERE showtime.movieID = (
    SELECT movie_id FROM movies WHERE title = 'The Batman' ORDER BY movie_id LIMIT 1
)
  AND DATE(showtime.dateTime) = DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY)
  AND NOT EXISTS (
      SELECT 1 FROM show_seats existing
      WHERE existing.showID = showtime.showID AND existing.seatID = seat.seatID
  );
