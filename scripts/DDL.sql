-- scheme
CREATE DATABASE susu CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 유저 정보
CREATE TABLE `user`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT 'user id',
    `oauth_provider`    int          NOT NULL COMMENT 'oauth 제공자, KAKAO: 0',
    `oauth_id`          varchar(256) NOT NULL COMMENT 'oauth id',
    `user_state`        int          NOT NULL COMMENT '유저 계정 상태, 활동 유저: 0, 탈퇴한 유저: 1, 정지 유저: 2, 영구 정지: 3',
    `name`              varchar(256) NOT NULL COMMENT 'user 이름',
    `gender`            int          DEFAULT NULL COMMENT 'user 성별, 남성: 0, 여성: 1',
    `birth`             date         DEFAULT NULL COMMENT 'user 출생년도',
    `profile_image_url` varchar(512) DEFAULT NULL COMMENT '프로필 이미지',
    `created_at`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유저 정보';

CREATE UNIQUE INDEX uidx__oauth_id__oauth_provider ON user (oauth_id, oauth_provider);

-- 장부
CREATE TABLE `ledger`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '장부 id',
    `uid`         int          NOT NULL COMMENT 'user id',
    `title`       varchar(512) NOT NULL COMMENT '제목',
    `description` varchar(512) DEFAULT NULL COMMENT '상세 설명',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 200000 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='장부';
CREATE INDEX idx__uid ON ledger (uid);

-- 관계
CREATE TABLE `relationship`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '관계 정보 id',
    `title`       varchar(512) NOT NULL COMMENT '관계',
    `description` varchar(512) DEFAULT NULL COMMENT '상세 설명',
    `is_active`   tinyint      NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='관계 정보';

-- 카테고리
CREATE TABLE `category`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '관계 정보 id',
    `seq`         int          NOT NULL COMMENT '노출 순서',
    `name`        varchar(256) NOT NULL COMMENT '카테고리 명',
    `is_active`   tinyint      NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='카테고리';
