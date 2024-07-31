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
ALTER TABLE term ADD (`seq` int NOT NULL COMMENT '노출 순서');
ALTER TABLE term MODIFY `description` text NULL;

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
