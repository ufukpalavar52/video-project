-- changeset video_process:gif_video

CREATE TABLE IF NOT EXISTS `gif_video` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `transaction_id` varchar(128) DEFAULT NULL,
    `path` varchar(255) NOT NULL,
    `gif_path` varchar(255) DEFAULT NULL,
    `path_type` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
    `is_url` tinyint(1) DEFAULT '0',
    `start_time` int DEFAULT NULL,
    `end_time` int DEFAULT NULL,
    `status` varchar(20) DEFAULT NULL,
    `created_at` timestamp NULL DEFAULT NULL,
    `updated_at` timestamp NULL DEFAULT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb3;

