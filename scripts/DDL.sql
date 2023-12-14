CREATE
DATABASE susu CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE `user`
(
    `id`                bigint NOT NULL AUTO_INCREMENT,
    `createdAt`         datetime DEFAULT CURRENT_TIMESTAMP,
    `modifiedAt`        datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `oauth_provider`    varchar(30) NOT NULL,
    `oauth_id`          varchar(255) NOT NULL,
    `name`              varchar(255) NOT NULL,
    `age`               int,
    `birth`             date,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE INDEX oauth_idx ON user(oauth_id, oauth_provider);