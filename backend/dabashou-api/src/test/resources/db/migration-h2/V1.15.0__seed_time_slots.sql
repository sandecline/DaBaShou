-- Seed available time slots for H2 integration tests.
-- Time slots are user-level in the current schema and are read by shelf owner.

INSERT INTO dbs_time_slot (user_id, date, start_time, end_time, status)
VALUES
(2, DATEADD('DAY', 1, CURRENT_DATE), '09:00:00', '10:00:00', 1),
(2, DATEADD('DAY', 1, CURRENT_DATE), '14:00:00', '15:00:00', 1),
(2, DATEADD('DAY', 2, CURRENT_DATE), '19:00:00', '20:00:00', 1),
(3, DATEADD('DAY', 1, CURRENT_DATE), '10:00:00', '11:00:00', 1),
(3, DATEADD('DAY', 2, CURRENT_DATE), '15:00:00', '16:00:00', 1),
(3, DATEADD('DAY', 3, CURRENT_DATE), '20:00:00', '21:00:00', 1),
(4, DATEADD('DAY', 1, CURRENT_DATE), '08:00:00', '09:00:00', 1),
(4, DATEADD('DAY', 2, CURRENT_DATE), '18:00:00', '19:00:00', 1);

-- ROLLBACK:
-- DELETE FROM dbs_time_slot
-- WHERE user_id IN (2, 3, 4)
--   AND date BETWEEN CURRENT_DATE AND DATEADD('DAY', 7, CURRENT_DATE);
