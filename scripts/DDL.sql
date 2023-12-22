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
    `start_at`    datetime     NOT NULL COMMENT '시작일',
    `end_at`      datetime     NOT NULL COMMENT '종료일',
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

-- 지인
CREATE TABLE `friend`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '지인 정보 id',
    `uid`         int          NOT NULL COMMENT 'user id',
    `name`        varchar(512) NOT NULL COMMENT '지인 이름',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='지인 정보';
CREATE INDEX idx__uid ON friend (uid);

-- 지인 관계
CREATE TABLE `friend_relationship`
(
    `id`              bigint NOT NULL AUTO_INCREMENT COMMENT 'friend_relationship id',
    `friend_id`       int    NOT NULL COMMENT '지인 id',
    `relationship_id` int    NOT NULL COMMENT '관계 id',
    `custom_relation` varchar(512) DEFAULT NULL COMMENT '기타 항목인 경우, 별도 입력을 위한 컬럼',
    `created_at`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='지인 관계 매핑 테이블';
CREATE UNIQUE INDEX uidx__friend_id__relationship_id ON friend_relationship (friend_id, relationship_id);

-- 카테고리 매핑
CREATE TABLE `category_assignment`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT 'category_assignment id',
    `target_id`       int          NOT NULL COMMENT '대상 id',
    `target_type`     varchar(256) NOT NULL COMMENT '대상 type (LEDGER, ENVELOPE)',
    `category_id`     int          NOT NULL COMMENT '카테고리 id',
    `custom_category` varchar(256) NOT NULL COMMENT '기타 항목인 경우, 별도 입력을 위한 컬럼',
    `created_at`      datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`     datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='카테고리 매핑 테이블';
CREATE UNIQUE INDEX uidx__target_id__target_type ON category_assignment (target_id, target_type);
