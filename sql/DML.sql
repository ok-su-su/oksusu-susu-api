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
VALUES (1, '결혼식', 1, '#FFA500');
INSERT INTO susu.category (seq, name, is_active, style)
VALUES (2, '돌잔치', 1, '#FFA500');
INSERT INTO susu.category (seq, name, is_active, style)
VALUES (3, '장례식', 1, '#242424');
INSERT INTO susu.category (seq, name, is_active, style)
VALUES (4, '생일 기념일', 1, '#007BFF');
INSERT INTO susu.category (seq, name, is_active, style)
VALUES (5, '기타', 1, '#D0D0D0');

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

-- post
INSERT INTO susu.post (id, uid, board_id, type, title, content, is_active)
VALUES (1, 1, 1, 0, null, '친구의 결혼식, 축의금은 얼마가 적당하다고 생각하시나요', 1);

-- vote_option
INSERT INTO susu.vote_option (id, post_id, content, seq)
VALUES (1, 1, '3만원', 1);
INSERT INTO susu.vote_option (id, post_id, content, seq)
VALUES (2, 1, '5만원', 2);
INSERT INTO susu.vote_option (id, post_id, content, seq)
VALUES (3, 1, '10만원', 3);
INSERT INTO susu.vote_option (id, post_id, content, seq)
VALUES (4, 1, '20만원', 4);

-- count
INSERT INTO susu.count (id, target_id, target_type, count_type, count)
VALUES (1, 1, 0, 0, 0);
INSERT INTO susu.count (id, target_id, target_type, count_type, count)
VALUES (2, 1, 1, 0, 0);
INSERT INTO susu.count (id, target_id, target_type, count_type, count)
VALUES (3, 2, 1, 0, 0);
INSERT INTO susu.count (id, target_id, target_type, count_type, count)
VALUES (4, 3, 1, 0, 0);
INSERT INTO susu.count (id, target_id, target_type, count_type, count)
VALUES (5, 4, 1, 0, 0);

-- application metadata
INSERT INTO susu.application_metadata (id, application_version, forced_update_date, is_active)
VALUES (1, "1.0.0", "2024-02-18 20:14:25", 1)