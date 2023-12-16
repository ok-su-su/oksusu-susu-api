CREATE
DATABASE susu CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 유저 정보
CREATE TABLE `user`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT 'user id',
    `oauth_provider`    varchar(30)  NOT NULL COMMENT 'oauth 제공자',
    `oauth_id`          varchar(255) NOT NULL COMMENT 'oauth id',
    `name`              varchar(255) NOT NULL COMMENT 'user 이름',
    `age`               int COMMENT 'user 나이',
    `birth`             date COMMENT 'user 생년',
    `profile_image_url` varchar(512) DEFAULT NULL COMMENT '프로필 이미지',
    `created_at`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유저 정보';
CREATE UNIQUE INDEX uidx__oauth_id ON user (oauth_id);

-- 유저 디바이스 정보
CREATE TABLE `user_device`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `uid`         int          NOT NULL COMMENT '유저 id',
    `device`      varchar(512) NOT NULL COMMENT '디바이스명',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE INDEX idx__user_id ON user_device (user_id);

-- 유저 이용약관 정보
CREATE TABLE `user_term`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `uid`         int    NOT NULL,
    `term_id`     int    NOT NULL,
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE INDEX idx__user_id ON user_term (user_id);

-- 약관 정보
CREATE TABLE `term`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `title`       varchar(512) NOT NULL COMMENT '약관명',
    `description` text         NOT NULL COMMENT '약관 상세',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 장부
CREATE TABLE `ledger`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `title`       varchar(512) NOT NULL COMMENT '제목',
    `description` text         NOT NULL COMMENT '상세 설명',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='장부';

-- 봉투 받은 돈, 보낸 돈
CREATE TABLE `envelope`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `ledger_id`  int DEFAULT NULL COMMENT '장부 id',
    `type`       int    NOT NULL COMMENT '경조사비를 낸다. ', -- SENT, RECEIVED
    `uid`        int    NOT NULL COMMENT '',
    `target_uid` int    NOT NULL COMMENT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='장부';
CREATE INDEX idx__ledger_id ON pay_info (ledger_id);
