-- database
CREATE DATABASE susu CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- user
CREATE TABLE `user`
(
    `id`         bigint NOT NULL AUTO_INCREMENT COMMENT 'user id',
    `createdAt`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    `modifiedAt` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='유저 정보'
