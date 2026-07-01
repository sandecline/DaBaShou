-- Seed available time slots for demo users.
-- Time slots are user-level in the current schema and are read by shelf owner.

INSERT INTO `dbs_time_slot` (`user_id`, `date`, `start_time`, `end_time`, `status`)
VALUES
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '10:00:00', 1),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '15:00:00', 1),
(2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '19:00:00', '20:00:00', 1),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '11:00:00', 1),
(3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:00:00', '16:00:00', 1),
(3, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '20:00:00', '21:00:00', 1),
(4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '09:00:00', 1),
(4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00:00', '19:00:00', 1);

-- ROLLBACK:
-- DELETE FROM `dbs_time_slot`
-- WHERE `user_id` IN (2, 3, 4)
--   AND `date` BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY);
