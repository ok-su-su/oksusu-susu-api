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

-- board
INSERT INTO susu.board (seq, name, is_active)
VALUES (1, '결혼식', 1);
INSERT INTO susu.board (seq, name, is_active)
VALUES (2, '돌잔치', 1);
INSERT INTO susu.board (seq, name, is_active)
VALUES (3, '장례식', 1);
INSERT INTO susu.board (seq, name, is_active)
VALUES (4, '생일 기념일', 1);
INSERT INTO susu.board (seq, name, is_active)
VALUES (5, '자유', 1);

-- user_status_type
INSERT INTO susu.user_status_type (status_type_info, is_active)
VALUES (0, 1);
INSERT INTO susu.user_status_type (status_type_info, is_active)
VALUES (1, 1);
INSERT INTO susu.user_status_type (status_type_info, is_active)
VALUES (2, 1);
INSERT INTO susu.user_status_type (status_type_info, is_active)
VALUES (3, 1);