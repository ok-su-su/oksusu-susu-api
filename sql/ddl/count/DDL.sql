-- 카운트
CREATE TABLE `count`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '카운트 id',
    `target_id`   bigint NOT NULL COMMENT '대상 id',
    `target_type` int    NOT NULL COMMENT '대상 type / 0:POST, 1:VOTE_OPTION',
    `count_type`  int    NOT NULL COMMENT '카운트 type / 0:VOTE',
    `count`       bigint NOT NULL COMMENT '카운트',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_general_ci COMMENT '카운트';
CREATE INDEX idx__target_type__target_id ON count (target_type, target_id);
