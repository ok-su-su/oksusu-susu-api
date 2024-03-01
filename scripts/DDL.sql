-- scheme
CREATE
DATABASE susu CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

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

-- 유저 상태 정보 타입
CREATE TABLE `user_sratus_type`
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

-- 장부
CREATE TABLE `ledger`
(
    `id`                     bigint                 NOT NULL AUTO_INCREMENT COMMENT '장부 id',
    `uid`                    bigint                 NOT NULL COMMENT 'user id',
    `title`                  varchar(512)           NOT NULL COMMENT '제목',
    `description`            varchar(512) DEFAULT NULL COMMENT '상세 설명',
    `total_sent_amounts`     bigint       DEFAULT 0 NOT NULL COMMENT '보낸 봉투 총합',
    `total_received_amounts` bigint       DEFAULT 0 NOT NULL COMMENT '받은 봉투 총합',
    `start_at`               datetime               NOT NULL COMMENT '시작일',
    `end_at`                 datetime               NOT NULL COMMENT '종료일',
    `created_at`             datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`            datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 200000 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='장부';
CREATE INDEX idx__uid ON ledger (uid);

-- 관계
CREATE TABLE `relationship`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '관계 정보 id',
    `relation`    varchar(512) NOT NULL COMMENT '관계',
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
    `style`       varchar(128) NOT NULL COMMENT '스타일',
    `is_active`   tinyint      NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='카테고리';

-- 지인
CREATE TABLE `friend`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '지인 정보 id',
    `uid`          int          NOT NULL COMMENT 'user id',
    `name`         varchar(512) NOT NULL COMMENT '지인 이름',
    `phone_number` varchar(512) DEFAULT NULL COMMENT '전화번호',
    `created_at`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='지인 정보';
CREATE UNIQUE INDEX idx__uid__phone_number ON friend (uid, phone_number);

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
    `custom_category` varchar(256) NULL COMMENT '기타 항목인 경우, 별도 입력을 위한 컬럼',
    `created_at`      datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`     datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='카테고리 매핑 테이블';
CREATE UNIQUE INDEX uidx__target_id__target_type ON category_assignment (target_id, target_type);

-- 봉투
CREATE TABLE `envelope`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '장부 id',
    `uid`            int          NOT NULL COMMENT 'user id',
    `type`           varchar(128) NOT NULL COMMENT 'type: SENT, RECEIVED',
    `friend_id`      int          NOT NULL COMMENT '친구 id',
    `ledger_id`      int          DEFAULT NULL COMMENT '장부 id',
    `amount`         bigint       NOT NULL COMMENT '금액',
    `gift`           varchar(512) DEFAULT NULL COMMENT '선물',
    `memo`           varchar(512) DEFAULT NULL COMMENT '메모',
    `has_visited`    tinyint      DEFAULT NULL COMMENT '방문 : 1, 미방문 : 0, null인 경우 미선택',
    `handed_over_at` datetime     NOT NULL COMMENT '전달일',
    `created_at`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 200000 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='장부';
CREATE INDEX idx__uid ON envelope (uid);

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

-- 약관 정보
CREATE TABLE `term`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '약관 정보 id',
    `title`        varchar(512) NOT NULL COMMENT '약관명',
    `description`  text         NOT NULL COMMENT '약관 상세',
    `is_essential` tinyint      NOT NULL COMMENT '필수 여부, 필수 : 1, 선택 : 0',
    `is_active`    tinyint      NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`  datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT ='약관 정보';

-- 약관 정보 동의
CREATE TABLE `term_agreement`
(
    `id`          bigint  NOT NULL AUTO_INCREMENT COMMENT '약관 정보 id',
    `uid`         bigint  NOT NULL COMMENT '유저 id',
    `term_id`     bigint  NOT NULL COMMENT '약관 정보 id',
    `is_active`   tinyint NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT ='약관 정보 동의';
CREATE UNIQUE INDEX idx__uid__term_id ON term_agreement (uid, term_id);

-- 약관 정보 동의 기록
CREATE TABLE `term_agreement_history`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '약관 정보 id',
    `uid`         bigint NOT NULL COMMENT '유저 id',
    `term_id`     bigint NOT NULL COMMENT '약관 정보 id',
    `change_type` int    NOT NULL COMMENT '변경 유형, 동의 : 0, 취소 : 1',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT ='약관 정보 동의 기록';

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

-- 신고 기록
CREATE TABLE `report_history`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `uid`         bigint       NOT NULL COMMENT '신고를 보낸 사용자',
    `target_id`   bigint       NOT NULL COMMENT '신고 대상',
    `target_type` varchar(128) NOT NULL COMMENT '신고 대상 유형',
    `metadata_id` bigint       NOT NULL COMMENT '메타데이터 id',
    `description` varchar(256) DEFAULT NULL COMMENT '신고 상세 설명',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT '신고 기록';
CREATE INDEX idx__uid__target_id ON report_history (target_id, target_type);

-- 신고 메타데이터
CREATE TABLE `report_metadata`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `seq`         int          NOT NULL COMMENT '노출 순서',
    `metadata`    varchar(512) NOT NULL COMMENT '신고 메타데이터',
    `target_type` varchar(128) NOT NULL COMMENT '신고 대상 유형',
    `is_active`   bit(1)       NOT NULL COMMENT '활성화 상태 1: true, 0: false',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT '신고 메타데이터';

-- 신고 결과
CREATE TABLE `report_result`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `uid`         bigint NOT NULL COMMENT '신고 대상 uid',
    `status`      int    NOT NULL COMMENT '신고 결과 상태',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_general_ci COMMENT '신고 결과';
CREATE INDEX idx__uid ON report_result (uid);

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

-- system log
CREATE TABLE `system_action_log`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `host`        varchar(255)                    DEFAULT NULL,
    `http_method` varchar(255)                    DEFAULT NULL,
    `ip_address`  varchar(255)                    DEFAULT NULL,
    `path`        varchar(255)                    DEFAULT NULL,
    `referer`     varchar(255)                    DEFAULT NULL,
    `user_agent`  varchar(255)                    DEFAULT NULL,
    `extra`       text COLLATE utf8mb4_general_ci DEFAULT NULL,
    `created_at`  datetime                        DEFAULT CURRENT_TIMESTAMP,
    `modified_at` datetime                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT 'system log';

-- 어플리케이션 설정 정보
CREATE TABLE `application_metadata`
(
    `id`                  bigint  NOT NULL AUTO_INCREMENT COMMENT '어플리케이션 설정 정보 id',
    `application_version` varchar(255) DEFAULT NULL COMMENT '최신 어플리케이션 버전',
    `forced_update_date`  datetime     DEFAULT NULL COMMENT '강제 업데이트 날짜',
    `is_active`           tinyint NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`          datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`         datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT '어플리케이션 설정 정보';
