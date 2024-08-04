-- system log
CREATE TABLE `system_action_log`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `host`        varchar(255)                    DEFAULT NULL,
    `http_method` varchar(255)                    DEFAULT NULL,
    `ip_address`  varchar(255)                    DEFAULT NULL,
    `path`        varchar(255)                    DEFAULT NULL,
    `referer`     varchar(512)                    DEFAULT NULL,
    `user_agent`  varchar(255)                    DEFAULT NULL,
    `extra`       text COLLATE utf8mb4_general_ci DEFAULT NULL,
    `created_at`  datetime                        DEFAULT CURRENT_TIMESTAMP,
    `modified_at` datetime                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT 'system log';
CREATE INDEX idx__created_at ON system_action_log (created_at);
