-- MySQL dump 10.13  Distrib 9.7.1, for Win64 (x86_64)
--
-- Host: localhost    Database: dabashou
-- ------------------------------------------------------
-- Server version	9.7.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '3f504b8a-729f-11f1-ab7e-745d2271cd5b:1-210';

--
-- Current Database: `dabashou`
--

/*!40000 DROP DATABASE IF EXISTS `dabashou`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `dabashou` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `dabashou`;

--
-- Table structure for table `credit_appeal`
--

DROP TABLE IF EXISTS `credit_appeal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_appeal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申诉ID',
  `violation_id` bigint NOT NULL COMMENT '违规记录ID',
  `appellant_id` bigint NOT NULL COMMENT '申诉人ID',
  `reason` varchar(500) NOT NULL COMMENT '申诉理由',
  `evidence_file_id` bigint DEFAULT NULL COMMENT '证据文件ID',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-待审核 1-已通过 2-已驳回',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `review_remark` varchar(200) DEFAULT NULL COMMENT '审核备注',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_violation` (`violation_id`),
  KEY `idx_appellant` (`appellant_id`),
  KEY `evidence_file_id` (`evidence_file_id`),
  CONSTRAINT `credit_appeal_ibfk_1` FOREIGN KEY (`violation_id`) REFERENCES `credit_violation` (`id`),
  CONSTRAINT `credit_appeal_ibfk_2` FOREIGN KEY (`appellant_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `credit_appeal_ibfk_3` FOREIGN KEY (`evidence_file_id`) REFERENCES `sys_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='申诉表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_appeal`
--

LOCK TABLES `credit_appeal` WRITE;
/*!40000 ALTER TABLE `credit_appeal` DISABLE KEYS */;
/*!40000 ALTER TABLE `credit_appeal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_violation`
--

DROP TABLE IF EXISTS `credit_violation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_violation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '违规ID',
  `user_id` bigint NOT NULL COMMENT '违规用户ID',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `type` varchar(30) NOT NULL COMMENT '违规类型: cheat/spam/fraud/other',
  `description` varchar(500) DEFAULT NULL COMMENT '违规描述',
  `penalty_score` decimal(3,1) DEFAULT NULL COMMENT '扣分',
  `reporter_id` bigint DEFAULT NULL COMMENT '举报人ID',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-已撤销 1-有效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_type` (`type`),
  KEY `reporter_id` (`reporter_id`),
  CONSTRAINT `credit_violation_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `credit_violation_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `dbs_order` (`id`),
  CONSTRAINT `credit_violation_ibfk_3` FOREIGN KEY (`reporter_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='违规记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_violation`
--

LOCK TABLES `credit_violation` WRITE;
/*!40000 ALTER TABLE `credit_violation` DISABLE KEYS */;
/*!40000 ALTER TABLE `credit_violation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_chat_message`
--

DROP TABLE IF EXISTS `dbs_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `msg_type` tinyint DEFAULT '1' COMMENT '消息类型: 1-文字 2-图片',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读: 0-未读 1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `dbs_chat_message_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `dbs_chat_session` (`id`),
  CONSTRAINT `dbs_chat_message_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_chat_message`
--

LOCK TABLES `dbs_chat_message` WRITE;
/*!40000 ALTER TABLE `dbs_chat_message` DISABLE KEYS */;
INSERT INTO `dbs_chat_message` VALUES (1,1,1,'你好',1,0,'2026-07-01 14:05:25');
/*!40000 ALTER TABLE `dbs_chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_chat_session`
--

DROP TABLE IF EXISTS `dbs_chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user1_id` bigint NOT NULL COMMENT '用户1ID',
  `user2_id` bigint NOT NULL COMMENT '用户2ID',
  `last_message` varchar(500) DEFAULT NULL COMMENT '最后一条消息',
  `last_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` int DEFAULT '0' COMMENT '未读消息数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user1` (`user1_id`),
  KEY `idx_user2` (`user2_id`),
  CONSTRAINT `dbs_chat_session_ibfk_1` FOREIGN KEY (`user1_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `dbs_chat_session_ibfk_2` FOREIGN KEY (`user2_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_chat_session`
--

LOCK TABLES `dbs_chat_session` WRITE;
/*!40000 ALTER TABLE `dbs_chat_session` DISABLE KEYS */;
INSERT INTO `dbs_chat_session` VALUES (1,1,2,'你好','2026-07-01 14:05:25',1,'2026-07-01 09:23:28','2026-07-01 14:05:25');
/*!40000 ALTER TABLE `dbs_chat_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_demand`
--

DROP TABLE IF EXISTS `dbs_demand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_demand` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '闇?眰ID',
  `user_id` bigint NOT NULL COMMENT '鍙戝竷鑰匢D',
  `skill_tag_id` bigint DEFAULT NULL COMMENT '鎵?渶鎶?兘鏍囩?ID',
  `title` varchar(100) NOT NULL COMMENT '闇?眰鏍囬?',
  `description` text COMMENT '闇?眰鎻忚堪',
  `point_reward` int NOT NULL COMMENT '鎮?祻绉?垎',
  `deadline` datetime DEFAULT NULL COMMENT '鎴??鏃堕棿',
  `location_type` tinyint DEFAULT '1' COMMENT '鍦扮偣绫诲瀷: 1-绾夸笂 2-绾夸笅 3-鍧囧彲',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '缁忓害',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '绾?害',
  `campus` varchar(50) DEFAULT NULL COMMENT '鏍″尯',
  `building` varchar(50) DEFAULT NULL COMMENT '妤兼爧',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?: 0-宸插叧闂?1-寰呮帴鍗?2-杩涜?涓?3-宸插畬鎴',
  `demand_type` tinyint NOT NULL DEFAULT '1' COMMENT '需求类型: 1-求助悬赏 2-技能置换',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_skill` (`skill_tag_id`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`longitude`,`latitude`),
  CONSTRAINT `dbs_demand_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `dbs_demand_ibfk_2` FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='闇?眰琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_demand`
--

LOCK TABLES `dbs_demand` WRITE;
/*!40000 ALTER TABLE `dbs_demand` DISABLE KEYS */;
INSERT INTO `dbs_demand` VALUES (1,2,6,'急需PPT美化','下周一就要答辩了，PPT还做得不够好，求帮忙美化',300,'2026-06-29 15:36:05',1,NULL,NULL,'仙林校区',NULL,2,1,'2026-06-29 09:36:05','2026-06-29 16:01:36'),(2,1,11,'求帮忙做个简单App','课程项目需要，求帮忙做一个简单的Todo App',500,'2026-07-02 09:36:05',3,NULL,NULL,'仙林校区',NULL,1,1,'2026-06-29 09:36:05','2026-06-29 14:59:10'),(3,3,1,'求高数辅导','期末考试快到了，求高数辅导，极限和微积分部分',250,'2026-07-04 09:36:05',2,NULL,NULL,'仙林校区',NULL,1,1,'2026-06-29 09:36:05','2026-06-29 14:59:10');
/*!40000 ALTER TABLE `dbs_demand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_notification`
--

DROP TABLE IF EXISTS `dbs_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'notification id',
  `user_id` bigint NOT NULL COMMENT 'receiver user id',
  `type` varchar(30) NOT NULL COMMENT 'notification type: system/order/credit/point/chat',
  `title` varchar(100) NOT NULL COMMENT 'notification title',
  `content` varchar(500) DEFAULT NULL COMMENT 'notification content',
  `related_type` varchar(30) DEFAULT NULL COMMENT 'related type: order/review/violation',
  `related_id` bigint DEFAULT NULL COMMENT 'related business id',
  `is_read` tinyint DEFAULT '0' COMMENT 'read flag: 0 unread 1 read',
  `read_time` datetime DEFAULT NULL COMMENT 'read time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`),
  KEY `idx_user_time` (`user_id`,`create_time`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='system notification table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_notification`
--

LOCK TABLES `dbs_notification` WRITE;
/*!40000 ALTER TABLE `dbs_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `dbs_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_order`
--

DROP TABLE IF EXISTS `dbs_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁㈠崟ID',
  `order_no` varchar(32) NOT NULL COMMENT '璁㈠崟鍙',
  `buyer_id` bigint NOT NULL COMMENT '涔板?ID',
  `seller_id` bigint NOT NULL COMMENT '鍗栧?ID',
  `demand_id` bigint DEFAULT NULL COMMENT '鍏宠仈闇?眰ID',
  `skill_shelf_id` bigint DEFAULT NULL COMMENT '鍏宠仈璐ф灦ID',
  `skill_tag_id` bigint NOT NULL COMMENT '鎶?兘鏍囩?ID',
  `title` varchar(100) NOT NULL COMMENT '鏈嶅姟鏍囬?',
  `point_amount` int NOT NULL COMMENT '绉?垎閲戦?',
  `status` tinyint NOT NULL COMMENT '鐘舵?: 0-宸插彇娑?1-寰呮敮浠?2-宸叉敮浠?鎷呬繚涓? 3-鏈嶅姟涓?4-寰呯‘璁?5-宸插畬鎴?6-宸查?娆?7-浜夎?涓',
  `verify_code` varchar(10) DEFAULT NULL COMMENT '鏍搁攢鐮',
  `verify_code_expire` datetime DEFAULT NULL COMMENT '鏍搁攢鐮佽繃鏈熸椂闂',
  `time_slot_id` bigint DEFAULT NULL COMMENT '棰勭害鐨勬椂闂存牸瀛怚D',
  `service_start_time` datetime DEFAULT NULL COMMENT '鏈嶅姟寮??鏃堕棿',
  `service_end_time` datetime DEFAULT NULL COMMENT '鏈嶅姟缁撴潫鏃堕棿',
  `complete_time` datetime DEFAULT NULL COMMENT '瀹屾垚鏃堕棿',
  `cancel_time` datetime DEFAULT NULL COMMENT '鍙栨秷鏃堕棿',
  `cancel_reason` varchar(200) DEFAULT NULL COMMENT '鍙栨秷鍘熷洜',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT 'logic delete: 0 active 1 deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  UNIQUE KEY `uk_verify_code` (`verify_code`),
  KEY `idx_buyer` (`buyer_id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_status` (`status`),
  KEY `idx_order_no` (`order_no`),
  KEY `demand_id` (`demand_id`),
  KEY `skill_shelf_id` (`skill_shelf_id`),
  KEY `skill_tag_id` (`skill_tag_id`),
  KEY `idx_buyer_status` (`buyer_id`,`status`),
  KEY `idx_seller_status` (`seller_id`,`status`),
  CONSTRAINT `dbs_order_ibfk_1` FOREIGN KEY (`buyer_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `dbs_order_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `dbs_order_ibfk_3` FOREIGN KEY (`demand_id`) REFERENCES `dbs_demand` (`id`),
  CONSTRAINT `dbs_order_ibfk_4` FOREIGN KEY (`skill_shelf_id`) REFERENCES `dbs_skill_shelf` (`id`),
  CONSTRAINT `dbs_order_ibfk_5` FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璁㈠崟琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_order`
--

LOCK TABLES `dbs_order` WRITE;
/*!40000 ALTER TABLE `dbs_order` DISABLE KEYS */;
INSERT INTO `dbs_order` VALUES (1,'DB1782703014622',2,1,1,NULL,6,'急需PPT美化',300,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 14:32:59','test',NULL,'2026-06-29 11:16:54','2026-06-29 14:32:59',0),(2,'DB1782703179165',1,1,2,NULL,11,'求帮忙做个简单App',500,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 14:39:16','用户取消',NULL,'2026-06-29 11:19:39','2026-06-29 14:39:16',0),(3,'DB1782703188811',1,3,NULL,4,16,'吉他入门教学',120,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 14:39:28','用户取消',NULL,'2026-06-29 11:19:48','2026-06-29 14:39:28',0),(4,'DB1782714487430',1,1,NULL,5,1,'Test Skill',99,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 14:39:35','用户取消',NULL,'2026-06-29 14:28:07','2026-06-29 14:39:35',0),(5,'DB1782715143470',1,1,NULL,5,1,'Test Skill',99,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 14:39:39','用户取消',NULL,'2026-06-29 14:39:03','2026-06-29 14:39:39',0),(6,'DB1782715197116',3,1,3,NULL,1,'求高数辅导',250,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 14:48:35','用户取消',NULL,'2026-06-29 14:39:57','2026-06-29 14:48:35',0),(7,'DB1782716341807',2,1,1,NULL,6,'急需PPT美化',300,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 15:03:36','用户取消',NULL,'2026-06-29 14:59:01','2026-06-29 15:03:36',0),(8,'DB1782716519389',1,1,NULL,5,1,'Test Skill',99,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 15:03:42','用户取消',NULL,'2026-06-29 15:01:59','2026-06-29 15:03:42',0),(9,'DB1782716528316',2,1,1,NULL,6,'急需PPT美化',300,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 15:03:31','用户取消',NULL,'2026-06-29 15:02:08','2026-06-29 15:03:31',0),(10,'DB1782716553458',1,1,NULL,2,7,'海报设计服务',200,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 15:03:27','用户取消',NULL,'2026-06-29 15:02:33','2026-06-29 15:03:27',0),(11,'DB1782716960483',2,1,1,NULL,6,'急需PPT美化',300,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 15:09:52','用户取消',NULL,'2026-06-29 15:09:20','2026-06-29 15:09:52',0),(12,'DB1782717038067',2,1,1,NULL,6,'急需PPT美化',300,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 15:10:41','用户取消',NULL,'2026-06-29 15:10:38','2026-06-29 15:10:41',0),(13,'DB1782720096118',2,1,1,NULL,6,'急需PPT美化',300,3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-06-29 16:01:36','2026-06-29 16:01:41',0),(14,'DBS17828437295043843',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 02:22:10','2026-07-01 02:22:10',0),(15,'DBS178284373766635D3',1,2,NULL,6,1,'高数期末冲刺辅导',160,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 14:33:49','da',NULL,'2026-07-01 02:22:18','2026-07-01 14:33:49',0),(16,'DBS17828681500743DCC',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 09:09:10','2026-07-01 09:09:10',0),(17,'DBS17828684271769C6F',1,3,NULL,13,19,'代取快递服务',30,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 09:13:47','2026-07-01 09:13:47',0),(18,'DBS17828686491284737',1,2,NULL,14,20,'宿舍大扫除',200,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 09:17:29','2026-07-01 09:17:29',0),(19,'DBS17828690145442CEE',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 09:23:35','2026-07-01 09:23:35',0),(20,'DBS17828696183523790',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 09:33:38','2026-07-01 09:33:38',0),(21,'DBS17828710171712E25',1,2,NULL,6,1,'高数期末冲刺辅导',160,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 10:02:17','用户取消',NULL,'2026-07-01 09:56:57','2026-07-01 10:02:42',0),(22,'DBS17828714887313EA2',1,3,NULL,7,2,'英语四六级写作指导',120,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 14:33:41','cds',NULL,'2026-07-01 10:04:49','2026-07-01 14:33:41',0),(23,'DBS17828876045791DF5',1,2,NULL,6,1,'高数期末冲刺辅导',160,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 14:33:32','dsd',NULL,'2026-07-01 14:33:25','2026-07-01 14:33:32',0),(24,'DBS178288763726799D9',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 14:33:57','2026-07-01 14:33:57',0),(25,'DBS1782887642464D124',1,2,NULL,6,1,'高数期末冲刺辅导',160,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 14:42:05','cds',NULL,'2026-07-01 14:34:02','2026-07-01 14:42:05',0),(26,'DBS178288783291674D4',1,2,NULL,6,1,'高数期末冲刺辅导',160,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 14:37:18','gvfdfv',NULL,'2026-07-01 14:37:13','2026-07-01 14:37:18',0),(27,'DBS178288807580736FC',1,3,NULL,9,4,'笔记本电脑清灰换硅脂',80,0,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 14:41:23','dxvdf',NULL,'2026-07-01 14:41:16','2026-07-01 14:41:23',0),(28,'DBS17828894621412846',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:04:22','2026-07-01 15:04:22',0),(29,'DBS1782889467855529B',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:04:28','2026-07-01 15:04:28',0),(30,'DBS17828894784307785',1,3,NULL,7,2,'英语四六级写作指导',120,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:04:38','2026-07-01 15:04:38',0),(31,'DBS1782889485560D1AC',1,2,NULL,11,13,'篮球一对一陪练',150,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:04:46','2026-07-01 15:04:46',0),(32,'DBS1782890534326BA9A',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:22:14','2026-07-01 15:22:14',0),(33,'DBS1782890540319219A',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:22:20','2026-07-01 15:22:20',0),(34,'DBS1782890554761F114',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:22:35','2026-07-01 15:22:35',0),(35,'DBS1782890624968D9E2',1,3,NULL,7,2,'英语四六级写作指导',120,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:23:45','2026-07-01 15:23:45',0),(36,'DBS178289066461471EF',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:24:25','2026-07-01 15:24:25',0),(37,'DBS1782890668325B7AF',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:24:28','2026-07-01 15:24:28',0),(38,'DBS17828910613151F45',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:31:01','2026-07-01 15:31:01',0),(39,'DBS178289106968671BD',1,2,NULL,6,1,'高数期末冲刺辅导',160,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:31:10','2026-07-01 15:31:10',0),(40,'DBS17828916013949568',1,3,NULL,13,19,'代取快递服务',30,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 15:40:01','2026-07-01 15:40:01',0);
/*!40000 ALTER TABLE `dbs_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_point_account`
--

DROP TABLE IF EXISTS `dbs_point_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_point_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'point account id',
  `user_id` bigint NOT NULL COMMENT 'user id',
  `available` int NOT NULL DEFAULT '0' COMMENT 'available points',
  `frozen` int NOT NULL DEFAULT '0' COMMENT 'frozen points',
  `total_earned` int NOT NULL DEFAULT '0' COMMENT 'total earned points',
  `total_spent` int NOT NULL DEFAULT '0' COMMENT 'total spent points',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `fk_point_account_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='point account table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_point_account`
--

LOCK TABLES `dbs_point_account` WRITE;
/*!40000 ALTER TABLE `dbs_point_account` DISABLE KEYS */;
INSERT INTO `dbs_point_account` VALUES (1,1,500,0,500,0,'2026-07-01 09:52:00','2026-07-01 09:52:00'),(2,2,300,0,300,0,'2026-07-01 09:52:00','2026-07-01 09:52:00'),(3,3,200,0,200,0,'2026-07-01 09:52:00','2026-07-01 09:52:00');
/*!40000 ALTER TABLE `dbs_point_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_point_transaction`
--

DROP TABLE IF EXISTS `dbs_point_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_point_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '娴佹按ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `order_id` bigint DEFAULT NULL COMMENT '鍏宠仈璁㈠崟ID',
  `type` tinyint NOT NULL COMMENT '绫诲瀷: 1-鏀跺叆 2-鏀?嚭 3-鍐荤粨 4-瑙ｅ喕 5-绯荤粺濂栧姳 6-绯荤粺鎵ｉ櫎',
  `amount` int NOT NULL COMMENT '绉?垎鏁伴噺',
  `balance_after` int NOT NULL COMMENT '鍙樺姩鍚庝綑棰',
  `description` varchar(200) DEFAULT NULL COMMENT '鎻忚堪',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_type` (`type`),
  CONSTRAINT `dbs_point_transaction_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `dbs_point_transaction_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `dbs_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='绉?垎娴佹按琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_point_transaction`
--

LOCK TABLES `dbs_point_transaction` WRITE;
/*!40000 ALTER TABLE `dbs_point_transaction` DISABLE KEYS */;
/*!40000 ALTER TABLE `dbs_point_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_review`
--

DROP TABLE IF EXISTS `dbs_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璇勪环ID',
  `order_id` bigint NOT NULL COMMENT '璁㈠崟ID',
  `reviewer_id` bigint NOT NULL COMMENT '璇勪环鑰匢D',
  `reviewee_id` bigint NOT NULL COMMENT '琚?瘎浠疯?ID',
  `rating` tinyint NOT NULL COMMENT '璇勫垎: 1-5',
  `content` varchar(500) DEFAULT NULL COMMENT '璇勪环鍐呭?',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_reviewer` (`order_id`,`reviewer_id`),
  KEY `reviewer_id` (`reviewer_id`),
  KEY `reviewee_id` (`reviewee_id`),
  CONSTRAINT `dbs_review_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `dbs_order` (`id`),
  CONSTRAINT `dbs_review_ibfk_2` FOREIGN KEY (`reviewer_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `dbs_review_ibfk_3` FOREIGN KEY (`reviewee_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璇勪环琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_review`
--

LOCK TABLES `dbs_review` WRITE;
/*!40000 ALTER TABLE `dbs_review` DISABLE KEYS */;
/*!40000 ALTER TABLE `dbs_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_skill_category`
--

DROP TABLE IF EXISTS `dbs_skill_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_skill_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鍒嗙被ID',
  `name` varchar(50) NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `icon` varchar(100) DEFAULT NULL COMMENT '鍥炬爣',
  `sort_order` int DEFAULT '0' COMMENT '鎺掑簭',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?: 0-绂佺敤 1-姝ｅ父',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鎶?兘鍒嗙被琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_skill_category`
--

LOCK TABLES `dbs_skill_category` WRITE;
/*!40000 ALTER TABLE `dbs_skill_category` DISABLE KEYS */;
INSERT INTO `dbs_skill_category` VALUES (1,'学业辅导','?',1,1,'2026-06-29 09:36:05'),(2,'维修帮忙','?',2,1,'2026-06-29 09:36:05'),(3,'设计美工','?',3,1,'2026-06-29 09:36:05'),(4,'技术支持','?',4,1,'2026-06-29 09:36:05'),(5,'运动陪练','?',5,1,'2026-06-29 09:36:05'),(6,'音乐艺术','?',6,1,'2026-06-29 09:36:05'),(7,'生活服务','?',7,1,'2026-06-29 09:36:05');
/*!40000 ALTER TABLE `dbs_skill_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_skill_shelf`
--

DROP TABLE IF EXISTS `dbs_skill_shelf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_skill_shelf` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璐ф灦ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `skill_tag_id` bigint NOT NULL COMMENT '鎶?兘鏍囩?ID',
  `title` varchar(100) NOT NULL COMMENT '鏈嶅姟鏍囬?',
  `description` text COMMENT '鏈嶅姟鎻忚堪',
  `point_price` int NOT NULL COMMENT '绉?垎浠锋牸',
  `duration_minutes` int DEFAULT NULL COMMENT '棰勮?鏃堕暱(鍒嗛挓)',
  `location_type` tinyint DEFAULT '1' COMMENT '鍦扮偣绫诲瀷: 1-绾夸笂 2-绾夸笅 3-鍧囧彲',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?: 0-涓嬫灦 1-涓婃灦 2-瀹℃牳涓',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_skill` (`skill_tag_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `dbs_skill_shelf_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `dbs_skill_shelf_ibfk_2` FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鎶?兘璐ф灦琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_skill_shelf`
--

LOCK TABLES `dbs_skill_shelf` WRITE;
/*!40000 ALTER TABLE `dbs_skill_shelf` DISABLE KEYS */;
INSERT INTO `dbs_skill_shelf` VALUES (1,1,6,'PPT排版美化','帮你把PPT做得更加专业、美观，适用于课程展示、毕业答辩等场景',150,60,3,1,'2026-06-29 09:36:05','2026-06-29 09:36:05'),(2,1,7,'海报设计服务','电子海报、活动海报设计，提供源文件',200,90,1,1,'2026-06-29 09:36:05','2026-06-29 09:36:05'),(3,2,10,'Python编程辅导','Python入门到进阶，数据分析方向，一对一辅导',180,120,3,1,'2026-06-29 09:36:05','2026-06-29 09:36:05'),(4,3,16,'吉他入门教学','零基础吉他教学，提供练习谱和指导',120,60,2,1,'2026-06-29 09:36:05','2026-06-29 09:36:05'),(5,1,1,'Test Skill','',99,60,1,1,'2026-06-29 14:25:58','2026-06-29 14:25:58'),(6,2,1,'高数期末冲刺辅导','一对一高数辅导，重点突破极限、微积分、级数，帮你轻松通过考试',160,90,2,1,'2026-06-29 15:17:52','2026-06-29 15:17:52'),(7,3,2,'英语四六级写作指导','四六级作文模板+实战训练，提升写作分数',120,60,1,1,'2026-06-29 15:17:52','2026-06-29 15:17:52'),(8,1,3,'毕业论文格式排版','Word排版、目录生成、页眉页脚设置，符合学校规范',100,45,3,1,'2026-06-29 15:17:52','2026-06-29 15:17:52'),(9,3,4,'笔记本电脑清灰换硅脂','专业工具，现场清灰+更换散热硅脂，让电脑不再发烫',80,45,2,1,'2026-06-29 15:17:52','2026-06-29 15:17:52'),(10,1,5,'自行车补胎维修','提供内胎修补、链条上油、刹车调整等服务',50,30,2,1,'2026-06-29 15:17:52','2026-06-29 15:17:52'),(11,2,13,'篮球一对一陪练','校队成员，可指导投篮、运球、战术配合',150,120,2,1,'2026-06-29 15:17:52','2026-06-29 15:17:52'),(12,1,15,'健身房入门指导','器械使用教学+训练计划制定，新手友好',130,90,2,1,'2026-06-29 15:17:52','2026-06-29 15:17:52'),(13,3,19,'代取快递服务','校园内代取快递，送货到宿舍楼下',30,15,2,1,'2026-06-29 15:17:52','2026-06-29 15:17:52'),(14,2,20,'宿舍大扫除','深度清洁，包括地面、桌面、卫生间，自带清洁工具',200,180,2,1,'2026-06-29 15:17:52','2026-06-29 15:17:52');
/*!40000 ALTER TABLE `dbs_skill_shelf` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_skill_tag`
--

DROP TABLE IF EXISTS `dbs_skill_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_skill_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鏍囩?ID',
  `category_id` bigint NOT NULL COMMENT '鍒嗙被ID',
  `name` varchar(50) NOT NULL COMMENT '鏍囩?鍚嶇О',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?: 0-绂佺敤 1-姝ｅ父',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `dbs_skill_tag_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `dbs_skill_category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鎶?兘鏍囩?琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_skill_tag`
--

LOCK TABLES `dbs_skill_tag` WRITE;
/*!40000 ALTER TABLE `dbs_skill_tag` DISABLE KEYS */;
INSERT INTO `dbs_skill_tag` VALUES (1,1,'数理化辅导',1,'2026-06-29 09:36:05'),(2,1,'英语辅导',1,'2026-06-29 09:36:05'),(3,1,'论文指导',1,'2026-06-29 09:36:05'),(4,2,'电器维修',1,'2026-06-29 09:36:05'),(5,2,'自行车修理',1,'2026-06-29 09:36:05'),(6,3,'PPT设计',1,'2026-06-29 09:36:05'),(7,3,'海报制作',1,'2026-06-29 09:36:05'),(8,3,'Logo设计',1,'2026-06-29 09:36:05'),(9,4,'编程辅导',1,'2026-06-29 09:36:05'),(10,4,'网站开发',1,'2026-06-29 09:36:05'),(11,4,'App开发',1,'2026-06-29 09:36:05'),(12,5,'篮球陪练',1,'2026-06-29 09:36:05'),(13,5,'跑步陪练',1,'2026-06-29 09:36:05'),(14,5,'健身指导',1,'2026-06-29 09:36:05'),(15,6,'吉他教学',1,'2026-06-29 09:36:05'),(16,6,'钢琴教学',1,'2026-06-29 09:36:05'),(17,6,'声乐指导',1,'2026-06-29 09:36:05'),(18,7,'代取快递',1,'2026-06-29 09:36:05'),(19,7,'打扫卫生',1,'2026-06-29 09:36:05'),(20,7,'搬家帮忙',1,'2026-06-29 09:36:05');
/*!40000 ALTER TABLE `dbs_skill_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_time_slot`
--

DROP TABLE IF EXISTS `dbs_time_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_time_slot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `date` date NOT NULL COMMENT '鏃ユ湡',
  `start_time` time NOT NULL COMMENT '寮??鏃堕棿',
  `end_time` time NOT NULL COMMENT '缁撴潫鏃堕棿',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?: 0-涓嶅彲鐢?1-鍙??绾?2-宸查?绾',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`,`date`),
  CONSTRAINT `dbs_time_slot_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鏃堕棿鏍煎瓙琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_time_slot`
--

LOCK TABLES `dbs_time_slot` WRITE;
/*!40000 ALTER TABLE `dbs_time_slot` DISABLE KEYS */;
/*!40000 ALTER TABLE `dbs_time_slot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_user`
--

DROP TABLE IF EXISTS `dbs_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `username` varchar(50) NOT NULL COMMENT '鐢ㄦ埛鍚',
  `nickname` varchar(50) DEFAULT NULL COMMENT '鏄电О',
  `avatar` varchar(255) DEFAULT NULL COMMENT '澶村儚URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '鎵嬫満鍙',
  `email` varchar(100) DEFAULT NULL COMMENT '閭??',
  `password_hash` varchar(255) NOT NULL COMMENT '瀵嗙爜鍝堝笇',
  `point_balance` int DEFAULT '0' COMMENT '绉?垎浣欓?',
  `trust_score` decimal(3,1) DEFAULT '5.0' COMMENT '淇′换鍒?1.0-5.0)',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '缁忓害',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '绾?害',
  `campus` varchar(50) DEFAULT NULL COMMENT '鏍″尯',
  `building` varchar(50) DEFAULT NULL COMMENT '妤兼爧',
  `bio` varchar(500) DEFAULT NULL COMMENT '涓?汉绠?粙',
  `status` tinyint DEFAULT '1' COMMENT '鐘舵?: 0-绂佺敤 1-姝ｅ父',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_location` (`longitude`,`latitude`),
  KEY `idx_campus` (`campus`),
  KEY `idx_status` (`status`),
  KEY `dbs_user_idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_user`
--

LOCK TABLES `dbs_user` WRITE;
/*!40000 ALTER TABLE `dbs_user` DISABLE KEYS */;
INSERT INTO `dbs_user` VALUES (1,'zhangsan','张三',NULL,'13800001001',NULL,'$2b$12$VipmeVvp066DBkrRvDNIgerpnmKqvo.lLzB84IwiIBlEahgG/0sjW',500,4.5,NULL,NULL,'仙林校区','2号楼','擅长PPT设计，欢迎交流',1,'2026-06-29 09:36:05','2026-06-30 22:02:26',0),(2,'lisi','李四',NULL,'13800001002',NULL,'$2b$12$QVF7Ab0Isg0OsT1HNbCEOOiaNiC7q50MLXOwNXNgG3Ojs3JNDKd6G',300,3.8,NULL,NULL,'鼓楼校区','5号楼','计算机专业，可辅导编程',1,'2026-06-29 09:36:05','2026-06-30 22:02:26',0),(3,'wangwu','王五',NULL,'13800001003',NULL,'$2b$12$BrHm3.KTC8iyQ9PFmz6WOubq8QT47ofPcCIsFP6Dc7q1TyFAZqARO',200,4.2,NULL,NULL,'仙林校区','1号楼','音乐爱好者，可教吉他',1,'2026-06-29 09:36:05','2026-06-30 22:02:26',0);
/*!40000 ALTER TABLE `dbs_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_user_campus_auth`
--

DROP TABLE IF EXISTS `dbs_user_campus_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_user_campus_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'auth id',
  `user_id` bigint NOT NULL COMMENT 'user id',
  `auth_type` varchar(20) NOT NULL COMMENT 'auth type: student/teacher',
  `student_no` varchar(30) DEFAULT NULL COMMENT 'student or staff number',
  `real_name` varchar(50) DEFAULT NULL COMMENT 'real name',
  `campus` varchar(50) DEFAULT NULL COMMENT 'campus',
  `college` varchar(100) DEFAULT NULL COMMENT 'college',
  `id_card_hash` varchar(64) DEFAULT NULL COMMENT 'id card hash',
  `credential_file_id` bigint DEFAULT NULL COMMENT 'credential file id',
  `status` tinyint DEFAULT '0' COMMENT 'status: 0 pending 1 approved 2 rejected',
  `review_remark` varchar(200) DEFAULT NULL COMMENT 'review remark',
  `reviewer_id` bigint DEFAULT NULL COMMENT 'reviewer id',
  `review_time` datetime DEFAULT NULL COMMENT 'review time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `fk_campus_auth_file` (`credential_file_id`),
  CONSTRAINT `fk_campus_auth_file` FOREIGN KEY (`credential_file_id`) REFERENCES `sys_file` (`id`),
  CONSTRAINT `fk_campus_auth_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='user campus auth table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_user_campus_auth`
--

LOCK TABLES `dbs_user_campus_auth` WRITE;
/*!40000 ALTER TABLE `dbs_user_campus_auth` DISABLE KEYS */;
/*!40000 ALTER TABLE `dbs_user_campus_auth` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_user_skill`
--

DROP TABLE IF EXISTS `dbs_user_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_user_skill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '鐢ㄦ埛ID',
  `skill_tag_id` bigint NOT NULL COMMENT '鎶?兘鏍囩?ID',
  `proficiency` tinyint DEFAULT '1' COMMENT '鐔熺粌搴? 1-浜嗚В 2-鐔熸倝 3-绮鹃? 4-涓撳?',
  `description` varchar(500) DEFAULT NULL COMMENT '鎶?兘鎻忚堪',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_skill` (`user_id`,`skill_tag_id`),
  KEY `skill_tag_id` (`skill_tag_id`),
  CONSTRAINT `dbs_user_skill_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `dbs_user_skill_ibfk_2` FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='鐢ㄦ埛鎶?兘琛';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_user_skill`
--

LOCK TABLES `dbs_user_skill` WRITE;
/*!40000 ALTER TABLE `dbs_user_skill` DISABLE KEYS */;
/*!40000 ALTER TABLE `dbs_user_skill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dbs_user_trust_score_log`
--

DROP TABLE IF EXISTS `dbs_user_trust_score_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dbs_user_trust_score_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'log id',
  `user_id` bigint NOT NULL COMMENT 'user id',
  `order_id` bigint DEFAULT NULL COMMENT 'related order id',
  `type` varchar(30) NOT NULL COMMENT 'change type',
  `score_change` decimal(3,1) NOT NULL COMMENT 'score change',
  `score_before` decimal(3,1) NOT NULL COMMENT 'score before',
  `score_after` decimal(3,1) NOT NULL COMMENT 'score after',
  `reason` varchar(200) DEFAULT NULL COMMENT 'change reason',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_type` (`type`),
  CONSTRAINT `fk_trust_score_log_order` FOREIGN KEY (`order_id`) REFERENCES `dbs_order` (`id`),
  CONSTRAINT `fk_trust_score_log_user` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='user trust score log';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dbs_user_trust_score_log`
--

LOCK TABLES `dbs_user_trust_score_log` WRITE;
/*!40000 ALTER TABLE `dbs_user_trust_score_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `dbs_user_trust_score_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'0','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',NULL,'root','2026-06-29 10:39:58',0,1),(2,'1.0.0','init core tables','SQL','V1.0.0__init_core_tables.sql',-470030475,'root','2026-06-29 10:39:58',46,1),(3,'1.1.0','init system tables','SQL','V1.1.0__init_system_tables.sql',1282383243,'root','2026-06-29 10:39:58',306,1),(4,'1.2.0','init credit tables','SQL','V1.2.0__init_credit_tables.sql',-1308604354,'root','2026-06-29 10:39:58',191,1),(5,'1.3.0','init stat tables','SQL','V1.3.0__init_stat_tables.sql',-1915156510,'root','2026-06-29 10:39:58',83,1),(6,'1.4.0','add chat and demand type','SQL','V1.4.0__add_chat_and_demand_type.sql',370366045,'root','2026-06-29 10:39:59',155,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stat_daily_summary`
--

DROP TABLE IF EXISTS `stat_daily_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stat_daily_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `new_user_count` int DEFAULT '0' COMMENT '新增用户数',
  `active_user_count` int DEFAULT '0' COMMENT '活跃用户数',
  `new_demand_count` int DEFAULT '0' COMMENT '新增需求数',
  `new_order_count` int DEFAULT '0' COMMENT '新增订单数',
  `completed_order_count` int DEFAULT '0' COMMENT '完成订单数',
  `cancelled_order_count` int DEFAULT '0' COMMENT '取消订单数',
  `point_inflow` bigint DEFAULT '0' COMMENT '积分流入量',
  `point_outflow` bigint DEFAULT '0' COMMENT '积分流出量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日汇总统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stat_daily_summary`
--

LOCK TABLES `stat_daily_summary` WRITE;
/*!40000 ALTER TABLE `stat_daily_summary` DISABLE KEYS */;
/*!40000 ALTER TABLE `stat_daily_summary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stat_skill_heat`
--

DROP TABLE IF EXISTS `stat_skill_heat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stat_skill_heat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `skill_tag_id` bigint NOT NULL COMMENT '技能标签ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `shelf_count` int DEFAULT '0' COMMENT '上架服务数',
  `demand_count` int DEFAULT '0' COMMENT '需求数',
  `order_count` int DEFAULT '0' COMMENT '订单数',
  `heat_score` decimal(10,2) DEFAULT '0.00' COMMENT '热度分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_skill` (`stat_date`,`skill_tag_id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_heat_score` (`heat_score`),
  KEY `skill_tag_id` (`skill_tag_id`),
  CONSTRAINT `stat_skill_heat_ibfk_1` FOREIGN KEY (`skill_tag_id`) REFERENCES `dbs_skill_tag` (`id`),
  CONSTRAINT `stat_skill_heat_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `dbs_skill_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='技能热度统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stat_skill_heat`
--

LOCK TABLES `stat_skill_heat` WRITE;
/*!40000 ALTER TABLE `stat_skill_heat` DISABLE KEYS */;
/*!40000 ALTER TABLE `stat_skill_heat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_type` varchar(50) DEFAULT NULL COMMENT '配置类型',
  `description` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_file`
--

DROP TABLE IF EXISTS `sys_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_url` varchar(500) DEFAULT NULL COMMENT '访问URL',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `upload_user_id` bigint DEFAULT NULL COMMENT '上传用户ID',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-删除 1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_upload_user` (`upload_user_id`),
  KEY `idx_file_type` (`file_type`),
  CONSTRAINT `sys_file_ibfk_1` FOREIGN KEY (`upload_user_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_file`
--

LOCK TABLES `sys_file` WRITE;
/*!40000 ALTER TABLE `sys_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_log`
--

DROP TABLE IF EXISTS `sys_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `operation_content` varchar(500) DEFAULT NULL COMMENT '操作内容',
  `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` text COMMENT '请求参数',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `status` tinyint DEFAULT NULL COMMENT '操作状态: 0-失败 1-成功',
  `error_msg` text COMMENT '错误信息',
  `cost_time_ms` int DEFAULT NULL COMMENT '耗时(毫秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `sys_log_ibfk_1` FOREIGN KEY (`operator_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_log`
--

LOCK TABLES `sys_log` WRITE;
/*!40000 ALTER TABLE `sys_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notification`
--

DROP TABLE IF EXISTS `sys_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` varchar(50) NOT NULL COMMENT '通知类型',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联业务类型',
  `related_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读: 0-未读 1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_type` (`type`),
  CONSTRAINT `sys_notification_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notification`
--

LOCK TABLES `sys_notification` WRITE;
/*!40000 ALTER TABLE `sys_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_permission`
--

DROP TABLE IF EXISTS `sys_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父权限ID',
  `permission_code` varchar(100) NOT NULL COMMENT '权限编码',
  `permission_name` varchar(50) NOT NULL COMMENT '权限名称',
  `type` tinyint NOT NULL COMMENT '类型: 1-菜单 2-按钮',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_permission`
--

LOCK TABLES `sys_permission` WRITE;
/*!40000 ALTER TABLE `sys_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_permission`
--

DROP TABLE IF EXISTS `sys_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `permission_id` (`permission_id`),
  CONSTRAINT `sys_role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT `sys_role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_permission`
--

LOCK TABLES `sys_role_permission` WRITE;
/*!40000 ALTER TABLE `sys_role_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `sys_user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `sys_user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_campus_auth`
--

DROP TABLE IF EXISTS `user_campus_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_campus_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '认证ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `auth_type` varchar(20) NOT NULL COMMENT '认证类型: student/teacher',
  `student_no` varchar(30) DEFAULT NULL COMMENT '学号/工号',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `campus` varchar(50) DEFAULT NULL COMMENT '校区',
  `college` varchar(100) DEFAULT NULL COMMENT '学院',
  `id_card_hash` varchar(64) DEFAULT NULL COMMENT '身份证号哈希',
  `credential_file_id` bigint DEFAULT NULL COMMENT '凭证文件ID',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-待审核 1-已通过 2-已拒绝',
  `review_remark` varchar(200) DEFAULT NULL COMMENT '审核备注',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `credential_file_id` (`credential_file_id`),
  CONSTRAINT `user_campus_auth_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `user_campus_auth_ibfk_2` FOREIGN KEY (`credential_file_id`) REFERENCES `sys_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园认证表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_campus_auth`
--

LOCK TABLES `user_campus_auth` WRITE;
/*!40000 ALTER TABLE `user_campus_auth` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_campus_auth` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_trust_score_log`
--

DROP TABLE IF EXISTS `user_trust_score_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_trust_score_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `type` varchar(30) NOT NULL COMMENT '变动类型: order_complete/order_cancel/violation/review/system_adjust',
  `score_change` decimal(3,1) NOT NULL COMMENT '变动分值',
  `score_before` decimal(3,1) NOT NULL COMMENT '变动前分数',
  `score_after` decimal(3,1) NOT NULL COMMENT '变动后分数',
  `reason` varchar(200) DEFAULT NULL COMMENT '变动原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_type` (`type`),
  CONSTRAINT `user_trust_score_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `dbs_user` (`id`),
  CONSTRAINT `user_trust_score_log_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `dbs_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='信任分变动记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_trust_score_log`
--

LOCK TABLES `user_trust_score_log` WRITE;
/*!40000 ALTER TABLE `user_trust_score_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_trust_score_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'dabashou'
--
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-01 16:03:53
