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
CREATE INDEX idx__created_at ON friend (created_at);

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

-- 관계
CREATE TABLE `relationship`
(
    `id`          bigint                 NOT NULL AUTO_INCREMENT COMMENT '관계 정보 id',
    `relation`    varchar(512)           NOT NULL COMMENT '관계',
    `description` varchar(512) DEFAULT NULL COMMENT '상세 설명',
    `is_active`   tinyint                NOT NULL COMMENT '활성화 : 1, 비활성화 : 0',
    `is_custom`   tinyint      DEFAULT 0 NOT NULL COMMENT '커스텀 여부',
    `created_at`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modified_at` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='관계 정보';
