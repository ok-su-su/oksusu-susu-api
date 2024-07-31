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
ALTER TABLE report_result ADD (`target_type` varchar(128) NOT NULL COMMENT '신고 대상');
ALTER TABLE report_result CHANGE COLUMN `uid` `target_id` bigint NOT NULL COMMENT '신고 대상 id';
ALTER TABLE report_result CHANGE COLUMN `status` `status` varchar (128) NOT NULL COMMENT '신고 결과 상태';
DROP INDEX idx__uid ON report_result;
CREATE INDEX idx__target_id__target_type ON report_result (target_id, target_type);
