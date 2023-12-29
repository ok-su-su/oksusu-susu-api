-- relationship
INSERT INTO susu.relationship (relation, description, is_active)
VALUES ('친구', NULL, 1);
INSERT INTO susu.relationship (relation, description, is_active)
VALUES ('가족', NULL, 1);
INSERT INTO susu.relationship (relation, description, is_active)
VALUES ('친척', NULL, 1);
INSERT INTO susu.relationship (relation, description, is_active)
VALUES ('동료', NULL, 1);
INSERT INTO susu.relationship (relation, description, is_active)
VALUES ('기타', NULL, 1);

-- category
INSERT INTO susu.category (seq, name, is_active)
VALUES (1, '결혼식', 1);
INSERT INTO susu.category (seq, name, is_active)
VALUES (2, '돌잔치', 1);
INSERT INTO susu.category (seq, name, is_active)
VALUES (3, '장례식', 1);
INSERT INTO susu.category (seq, name, is_active)
VALUES (4, '생일 기념일', 1);
INSERT INTO susu.category (seq, name, is_active)
VALUES (5, '기타', 1);

-- term
INSERT INTO susu.term (title, description, is_essential, is_active)
VALUES ('term1', 'des1', 1, 1);
INSERT INTO susu.term (title, description, is_essential, is_active)
VALUES ('term2', 'des2', 1, 1);
INSERT INTO susu.term (title, description, is_essential, is_active)
VALUES ('term3', 'des3', 1, 1);
INSERT INTO susu.term (title, description, is_essential, is_active)
VALUES ('term4', 'des4', 1, 1);
INSERT INTO susu.term (title, description, is_essential, is_active)
VALUES ('term5', 'des5', 1, 1);

-- friend
INSERT INTO suus.friend (uid, name)
VALUES ('200000', 'user1'),
       ('200000', 'user2'),
       ('200000', 'user3');

-- friendRelationship
INSERT INTO susu.friend_relationship (friend_id, relationship_id)
VALUES (1, 1),
       (2, 1),
       (3, 2);

-- ledger
INSERT INTO susu.ledger (uid, title, description, start_at, end_at)
VALUES ('200000', 'ledger_title1', 'ledger_description1', '2023-12-29T08:33:05', '2023-12-31T08:33:05'),
       ('200000', 'ledger_title2', 'ledger_description2', '2023-12-29T08:33:05', '2023-12-31T08:33:05');

-- envelope
INSERT INTO susu.envelope (uid, type, friend_id, ledger_id, amount, gift, memo, has_visited, handed_over_at)
VALUES ('200000', 'RECEIVED', 1, 200000, 100, null, null, 0, '2023-04-29T08:33:05'),
       ('200000', 'RECEIVED', 2, 200000, 200, null, null, 0, '2023-10-29T08:33:05'),
       ('200000', 'RECEIVED', 1, 200001, 300, null, null, 0, '2023-11-29T08:33:05'),
       ('200000', 'SENT', 1, null, 400, null, null, 0, '2023-04-29T08:33:05'),
       ('200000', 'SENT', 1, null, 500, null, null, 0, '2023-05-29T08:33:05'),
       ('200000', 'SENT', 3, null, 600, null, null, 0, '2023-11-29T08:33:05'),
       ('200000', 'SENT', 3, null, 700, null, null, 0, '2023-11-29T08:33:05'),
       ('200000', 'SENT', 3, null, 800, null, null, 0, '2023-12-29T08:33:05');

-- categoryAssignment
INSERT INTO susu.category_assignment (target_id, target_type, category_id)
VALUES ('200000', 'LEDGER', 1),
       ('200001', 'LEDGER', 2),
       ('200000', 'ENVELOPE', 1),
       ('200001', 'ENVELOPE', 2),
       ('200002', 'ENVELOPE', 2),
       ('200003', 'ENVELOPE', 3),
       ('200004', 'ENVELOPE', 3),
       ('200005', 'ENVELOPE', 3),
       ('200006', 'ENVELOPE', 4),
       ('200007', 'ENVELOPE', 5);