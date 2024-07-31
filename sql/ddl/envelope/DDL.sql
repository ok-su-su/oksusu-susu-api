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
CREATE INDEX idx__friend_id__uid ON envelope (friend_id, uid);
CREATE INDEX idx__ledger_id__uid ON envelope (ledger_id, uid);
CREATE INDEX idx__handed_over_at ON envelope (handed_over_at);

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
