-- 카테고리
CREATE TABLE `category`
(
    `id`          bigint             NOT NULL AUTO_INCREMENT COMMENT '관계 정보 id',
    `seq`         int                NOT NULL COMMENT '노출 순서',
    `name`        varchar(256)       NOT NULL COMMENT '카테고리 명',
    `style`       varchar(128)       NOT NULL COMMENT '스타일',
    `is_active`   tinyint            NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `is_custom`   tinyint  DEFAULT 0 NOT NULL COMMENT '커스텀 여부',
    `created_at`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='카테고리';

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
