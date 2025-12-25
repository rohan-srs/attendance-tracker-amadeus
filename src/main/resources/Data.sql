-- USERS (with WFO goal percentage)
INSERT INTO "user" (id, name, email, wfo_goal_percentage)
VALUES (1, 'Rohan', 'rohan@amadeus.com', 60);

INSERT INTO "user" (id, name, email, wfo_goal_percentage)
VALUES (2, 'John', 'john@amadeus.com', 50);

INSERT INTO "user" (id, name, email, wfo_goal_percentage)
VALUES (3, 'Adam', 'adam@amadeus.com', 70);


-- CATEGORIES (Pre-defined)
INSERT INTO category (id, name)
VALUES (1, 'WFH');

INSERT INTO category (id, name)
VALUES (2, 'WFO');

INSERT INTO category (id, name)
VALUES (3, 'Absence');


-- SAMPLE ATTENDANCE DATA (December 2025)
-- Rohan's attendance
INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (100, '2025-12-01', 1, 2); -- WFO

INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (101, '2025-12-02', 1, 2); -- WFO

INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (102, '2025-12-03', 1, 1); -- WFH

INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (103, '2025-12-04', 1, 2); -- WFO

INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (104, '2025-12-05', 1, 1); -- WFH

INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (105, '2025-12-09', 1, 2); -- WFO

INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (106, '2025-12-10', 1, 3); -- Absence


-- John's attendance
INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (200, '2025-12-01', 2, 1); -- WFH

INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (201, '2025-12-02', 2, 2); -- WFO

INSERT INTO attendance (id, attendance_date, user_id, category_id)
VALUES (202, '2025-12-03', 2, 2); -- WFO