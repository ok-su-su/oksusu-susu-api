-- 게시글
CREATE TABLE `post`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '커뮤니티 id',
    `uid`         bigint       NOT NULL COMMENT 'user id',
    `board_id`    bigint       NOT NULL COMMENT 'board id',
    `type`        int          NOT NULL COMMENT '커뮤니티 타입, 0: 투표',
    `title`       varchar(256) DEFAULT NULL COMMENT '제목',
    `content`     varchar(512) NOT NULL COMMENT '내용',
    `is_active`   tinyint      NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='게시글';

-- 보드
CREATE TABLE `board`
(
    `id`          bigint  NOT NULL AUTO_INCREMENT COMMENT '보드 id',
    `name`        varchar(256) DEFAULT NULL COMMENT '보드 명',
    `seq`         int     NOT NULL COMMENT '노출 순서',
    `is_active`   tinyint NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='보드';

-- 투표 선택지
CREATE TABLE `vote_option`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '선택지 id',
    `post_id`     bigint       NOT NULL COMMENT '게시글 id',
    `content`     varchar(256) NOT NULL COMMENT '선택지 명',
    `seq`         int          NOT NULL COMMENT '노출 순서',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='투표 선택지';
CREATE INDEX idx__post_id ON vote_option (post_id);

-- 투표 유저 매핑
CREATE TABLE `vote_history`
(
    `id`             bigint NOT NULL AUTO_INCREMENT COMMENT '투표 유저 매핑 id',
    `uid`            bigint NOT NULL COMMENT '유저 id',
    `post_id`        bigint NOT NULL COMMENT '투표 id',
    `vote_option_id` bigint NOT NULL COMMENT '투표 옵션 id',
    `created_at`     datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`    datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='투표 유저 매핑';
CREATE UNIQUE INDEX idx__uid__post_id ON vote_history (uid, post_id);
