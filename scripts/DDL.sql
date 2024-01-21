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
    `id`                     bigint                 NOT NULL AUTO_INCREMENT COMMENT '장부 id',
    `uid`                    int                    NOT NULL COMMENT 'user id',
    `title`                  varchar(512)           NOT NULL COMMENT '제목',
    `description`            varchar(512) DEFAULT NULL COMMENT '상세 설명',
    `total_sent_amounts`     int          DEFAULT 0 NOT NULL COMMENT '보낸 봉투 총합',
    `total_received_amounts` int          DEFAULT 0 NOT NULL COMMENT '받은 봉투 총합',
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
CREATE INDEX idx__uid ON friend (uid);
ALTER TABLE friend
    ADD CONSTRAINT unique__phone_number UNIQUE (phone_number);

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
    `amount`         int          NOT NULL COMMENT '금액',
    `gift`           varchar(512) DEFAULT NULL COMMENT '선물',
    `memo`           varchar(512) DEFAULT NULL COMMENT '메모',
    `has_visited`    tinyint      NOT NULL COMMENT '방문 : 1, 미방문 : 0',
    `handed_over_at` datetime     NOT NULL COMMENT '전달일',
    `created_at`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 200000 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='장부';
CREATE INDEX idx__uid ON envelope (uid);

-- 게시글
CREATE TABLE `post`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '커뮤니티 id',
    `uid`              bigint       NOT NULL COMMENT 'user id',
    `post_category_id` bigint       NOT NULL COMMENT 'post category id',
    `type`             int          NOT NULL COMMENT '커뮤니티 타입, 0: 투표',
    `title`            varchar(256) DEFAULT NULL COMMENT '제목',
    `content`          varchar(512) NOT NULL COMMENT '내용',
    `is_active`        tinyint      NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`       datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='게시글';

-- 게시글
CREATE TABLE `post_category`
(
    `id`          bigint  NOT NULL AUTO_INCREMENT COMMENT '게시글 id',
    `name`        varchar(256) DEFAULT NULL COMMENT '카테고리 명',
    `seq`         int     NOT NULL COMMENT '노출 순서',
    `is_active`   tinyint NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='게시글 카테고리';

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
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='투표 선택지';

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
CREATE TABLE `block`
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
CREATE UNIQUE INDEX idx__uid__target_id ON block (uid, target_id);

CREATE TABLE `report_history`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `uid`         bigint       NOT NULL COMMENT '신고를 보낸 사용자',
    `target_id`   bigint       NOT NULL COMMENT '신고 대상',
    `target_type` varchar(128) NOT NULL COMMENT '신고 대상 유형',
    `metadata_id` bigint       NOT NULL COMMENT '메타데이터 id',
    `description` varchar(255) DEFAULT NULL COMMENT '신고 상세 설명',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT '신고 기록';
CREATE INDEX idx__uid__target_id ON report_history (target_id, target_type);

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
