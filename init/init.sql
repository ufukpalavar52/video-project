CREATE TABLE `video` (
                         `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                         `transaction_id` varchar(128) DEFAULT NULL,
                         `path` varchar(255) NOT NULL,
                         `output_path` varchar(255) DEFAULT NULL,
                         `path_type` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
                         `resolution` varchar(20) DEFAULT NULL,
                         `process_type` varchar(20) DEFAULT NULL,
                         `is_url` tinyint(1) DEFAULT '0',
                         `start_time` int DEFAULT NULL,
                         `end_time` int DEFAULT NULL,
                         `status` varchar(20) DEFAULT NULL,
                         `created_at` timestamp NULL DEFAULT NULL,
                         `updated_at` timestamp NULL DEFAULT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE IF NOT EXISTS `video_error_log` (
                           `id` bigint unsigned NOT NULL AUTO_INCREMENT,
                           `transaction_id` varchar(128) DEFAULT NULL,
                           `message` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci,
                           `created_at` timestamp NULL DEFAULT NULL,
                           `updated_at` timestamp NULL DEFAULT NULL,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;