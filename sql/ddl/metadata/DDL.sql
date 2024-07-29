-- 어플리케이션 설정 정보
CREATE TABLE `application_metadata`
(
    `id`                  bigint       NOT NULL AUTO_INCREMENT COMMENT '어플리케이션 설정 정보 id',
    `application_version` varchar(255) NOT NULL COMMENT '최신 어플리케이션 버전',
    `forced_update_date`  datetime     NOT NULL COMMENT '강제 업데이트 날짜',
    `description`         text COMMENT '해당 버전의 주요 기능 설명',
    `is_active`           tinyint      NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `created_at`          datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at`         datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='어플리케이션 설정 정보';
