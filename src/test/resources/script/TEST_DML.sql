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
INSERT INTO susu.category (seq, name, is_active, style)
VALUES (1, '결혼식', 1, );
INSERT INTO susu.category (seq, name, is_active, style)
VALUES (2, '돌잔치', 1);
INSERT INTO susu.category (seq, name, is_active, style)
VALUES (3, '장례식', 1);
INSERT INTO susu.category (seq, name, is_active, style)
VALUES (4, '생일 기념일', 1);
INSERT INTO susu.category (seq, name, is_active, style)
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


-- post_category
INSERT INTO susu.post_category (seq, name, is_active)
VALUES (1, '결혼식', 1);
INSERT INTO susu.post_category (seq, name, is_active)
VALUES (2, '장례식', 1);
INSERT INTO susu.post_category (seq, name, is_active)
VALUES (3, '돌잔치', 1);
INSERT INTO susu.post_category (seq, name, is_active)
VALUES (4, '생일 기념일', 1);
INSERT INTO susu.post_category (seq, name, is_active)
VALUES (5, '자유', 1);
INSERT INTO susu.post_category (seq, name, is_active)
VALUES (6, '명예의 전당', 1);

-- friend
INSERT INTO susu.friend (uid, name)
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
VALUES ('200000', 'RECEIVED', 1, 200000, 100, NULL, NULL, 0, '2023-04-29T08:33:05'),
       ('200000', 'RECEIVED', 2, 200000, 200, NULL, NULL, 0, '2023-10-29T08:33:05'),
       ('200000', 'RECEIVED', 1, 200001, 300, NULL, NULL, 0, '2023-11-29T08:33:05'),
       ('200000', 'SENT', 1, NULL, 400, NULL, NULL, 0, '2023-04-29T08:33:05'),
       ('200000', 'SENT', 1, NULL, 500, NULL, NULL, 0, '2023-05-29T08:33:05'),
       ('200000', 'SENT', 3, NULL, 600, NULL, NULL, 0, '2023-11-29T08:33:05'),
       ('200000', 'SENT', 3, NULL, 700, NULL, NULL, 0, '2023-11-29T08:33:05'),
       ('200000', 'SENT', 3, NULL, 800, NULL, NULL, 0, '2023-12-29T08:33:05');

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

-- user
INSERT INTO susu.user (id, oauth_provider, oauth_id, user_state, name)
VALUES ('1', 0, "0000000001", 1, '1번 유저'),
       ('2', 0, "0000000002", 1, '2번 유저'),
       ('3', 0, "0000000003", 1, '3번 유저'),
       ('4', 0, "0000000004", 1, '4번 유저'),
       ('5', 0, "0000000005", 1, '5번 유저');