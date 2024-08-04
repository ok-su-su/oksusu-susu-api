-- 유저 정보
CREATE TABLE `user`
(
    `id`                bigint       NOT NULL AUTO_INCREMENT COMMENT 'user id',
    `oauth_provider`    int          NOT NULL COMMENT 'oauth 제공자, KAKAO: 0',
    `oauth_id`          varchar(256) NOT NULL COMMENT 'oauth id',
    `name`              varchar(256) NOT NULL COMMENT 'user 이름',
    `gender`            int          DEFAULT NULL COMMENT 'user 성별, 남성: 0, 여성: 1',
    `birth`             date         DEFAULT NULL COMMENT 'user 출생년도',
    `profile_image_url` varchar(512) DEFAULT NULL COMMENT '프로필 이미지',
    `role`              varchar(128) NOT NULL COMMENT '유저 권한',
    `created_at`        datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유저 정보';
CREATE UNIQUE INDEX uidx__oauth_id__oauth_provider ON user (oauth_id, oauth_provider);
CREATE INDEX idx__created_at ON user (created_at);

-- 유저 상태 정보 타입
CREATE TABLE `user_status_type`
(
    `id`               bigint  NOT NULL AUTO_INCREMENT COMMENT '유저 상태 정보 타입 id',
    `status_type_info` int     NOT NULL COMMENT '상태 정보 타입 정보 / 활동 : 1, 탈퇴 : 2,  일시 정지 7일 : 3, 영구 정지 : 4',
    `is_active`        tinyint NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`       datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`      datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유저 상태 정보 타입';

-- 유저 상태 정보
CREATE TABLE `user_status`
(
    `id`                  bigint NOT NULL AUTO_INCREMENT COMMENT '유저 상태 정보 id',
    `uid`                 bigint NOT NULL COMMENT '해당 유저 id',
    `account_status_id`   bigint NOT NULL COMMENT '계정 상태 id',
    `community_status_id` bigint NOT NULL COMMENT '커뮤니티 활동 상태 id',
    `created_at`          datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`         datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유저 상태 정보';
CREATE UNIQUE INDEX uidx__uid ON user_status (uid);

-- 유저 상태 변경 기록
CREATE TABLE `user_status_history`
(
    `id`                     bigint NOT NULL AUTO_INCREMENT COMMENT '유저 상태 변경 기록 id',
    `uid`                    bigint NOT NULL COMMENT '해당 유저 id',
    `status_assignment_type` int      DEFAULT NULL COMMENT '변경된 유저 상태 타입 / 계정 상태 : 1, 커뮤니티 활동 상태 : 2',
    `from_status_id`         int    NOT NULL COMMENT '변경 이전 상태 id',
    `to_status_id`           int    NOT NULL COMMENT '변경 후 상태 id',
    `created_at`             datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`            datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유저 상태 변경 기록';
CREATE INDEX idx__uid ON user_status_history (uid);
ALTER TABLE user_status_history ADD (`is_forced` tinyint DEFAULT 0 NOT NULL COMMENT '관리자 실행 여부, 1 : 관리자, 0 : 유저');
CREATE INDEX idx__created_at ON user_status_history (created_at);

-- 탈퇴 유저 기록
CREATE TABLE `user_withdraw`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '유저 상태 변경 기록 id',
    `uid`            bigint       NOT NULL COMMENT '해당 유저 id',
    `oauth_provider` int          NOT NULL COMMENT 'oauth 제공자, KAKAO: 0',
    `oauth_id`       varchar(256) NOT NULL COMMENT 'oauth id',
    `role`           varchar(128) NOT NULL COMMENT '유저 권한',
    `created_at`     datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`    datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유저 상태 변경 기록';
CREATE INDEX idx__uid ON user_withdraw (uid);
CREATE INDEX idx__created_at ON user_withdraw (created_at);

-- 카테고리
CREATE TABLE `category`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '관계 정보 id',
    `seq`         int          NOT NULL COMMENT '노출 순서',
    `name`        varchar(256) NOT NULL COMMENT '카테고리 명',
    `style`       varchar(128) NOT NULL COMMENT '스타일',
    `is_active`   tinyint      NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='카테고리';

-- 차단
CREATE TABLE `user_block`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '약관 정보 id',
    `uid`         bigint       NOT NULL COMMENT '유저 id',
    `target_id`   int          NOT NULL COMMENT '대상 id',
    `target_type` varchar(256) NOT NULL COMMENT '대상 type (USER, POST)',
    `reason`      varchar(256) DEFAULT NULL COMMENT '차단 사유,',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = '차단';
CREATE UNIQUE INDEX idx__uid__target_id ON user_block (uid, target_id);

-- 유저 디바이스 정보
CREATE TABLE `user_device`
(
    `id`                      bigint NOT NULL AUTO_INCREMENT COMMENT '유저 디바이스 정보 id',
    `uid`                     bigint NOT NULL COMMENT '유저 id',
    `application_version`     varchar(256) DEFAULT NULL COMMENT '어플리케이션 버전',
    `device_id`               varchar(256) DEFAULT NULL COMMENT 'IMEI',
    `device_software_version` varchar(256) DEFAULT NULL COMMENT 'SW버전',
    `line_number`             varchar(256) DEFAULT NULL COMMENT '전화번호',
    `network_country_iso`     varchar(256) DEFAULT NULL COMMENT '국가코드',
    `network_operator`        varchar(256) DEFAULT NULL COMMENT '망 사업자코드',
    `network_operator_name`   varchar(256) DEFAULT NULL COMMENT '망 사업자명 ',
    `network_type`            varchar(256) DEFAULT NULL COMMENT '망 시스템 방식',
    `phone_type`              varchar(256) DEFAULT NULL COMMENT '단말기 종류',
    `sim_serial_number`       varchar(256) DEFAULT NULL COMMENT 'SIM카드 Serial Number',
    `sim_state`               varchar(256) DEFAULT NULL COMMENT '가입자 ID',
    `created_at`              datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`             datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_general_ci COMMENT '유저 디바이스 정보';
CREATE INDEX idx__uid ON user_device (uid);
