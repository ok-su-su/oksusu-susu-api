-- scheme
CREATE DATABASE susu CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 유저 정보
CREATE TABLE `user`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT 'user id',
    `oauth_provider`    varchar(32)  NOT NULL COMMENT 'oauth 제공자',
    `oauth_id`          varchar(256) NOT NULL COMMENT 'oauth id',
    `name`              varchar(256) NOT NULL COMMENT 'user 이름',
    `age`               int COMMENT 'user 나이',
    `birth`             date COMMENT 'user 생년',
    `profile_image_url` varchar(512) DEFAULT NULL COMMENT '프로필 이미지',
    `created_at`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유저 정보';
CREATE UNIQUE INDEX uidx__oauth_id ON user (oauth_id);

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
