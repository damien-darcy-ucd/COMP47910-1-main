-- MySQL dump 10.13  Distrib 8.0.36, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: vaxapp
-- ------------------------------------------------------
-- Server version	8.0.36-2ubuntu3

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

--
-- Table structure for table `appointment`
--

DROP TABLE IF EXISTS `appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `time` time DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `vaccine_centre_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7bo52i6usixwb7ira9l16y3bu` (`user_id`),
  KEY `FKbdwbqvnyflcplcahpdcxic0ua` (`vaccine_centre_id`),
  CONSTRAINT `FK7bo52i6usixwb7ira9l16y3bu` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKbdwbqvnyflcplcahpdcxic0ua` FOREIGN KEY (`vaccine_centre_id`) REFERENCES `vaccine_centre` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment`
--

LOCK TABLES `appointment` WRITE;
/*!40000 ALTER TABLE `appointment` DISABLE KEYS */;
INSERT INTO `appointment` VALUES (1,'2022-01-15','done','04:30:00',3,1),(2,'2021-09-09','done','04:30:00',3,1),(3,'2099-04-01','pending','04:30:00',2,1),(4,'2020-08-08','done','04:30:00',4,2),(5,'2022-02-12','done','04:30:00',4,2),(6,'2022-02-12','done','04:30:00',4,2);
/*!40000 ALTER TABLE `appointment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `appointment_slot`
--

DROP TABLE IF EXISTS `appointment_slot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `appointment_slot` (
  `id` int NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `start_time` time DEFAULT NULL,
  `vaccine_centre_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbipw5arh7rc89jg5l9tk9tg93` (`vaccine_centre_id`),
  CONSTRAINT `FKbipw5arh7rc89jg5l9tk9tg93` FOREIGN KEY (`vaccine_centre_id`) REFERENCES `vaccine_centre` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment_slot`
--

LOCK TABLES `appointment_slot` WRITE;
/*!40000 ALTER TABLE `appointment_slot` DISABLE KEYS */;
INSERT INTO `appointment_slot` VALUES (1,'2024-05-26','13:30:00',1),(2,'2024-05-26','13:45:00',1),(3,'2024-05-26','14:00:00',1),(4,'2024-05-26','14:15:00',1),(5,'2024-05-26','14:30:00',1),(6,'2024-05-26','14:45:00',1),(7,'2024-05-27','13:30:00',1),(8,'2024-05-27','13:45:00',1),(9,'2024-05-27','14:00:00',1),(10,'2024-05-27','14:15:00',1),(11,'2024-05-27','14:30:00',1),(12,'2024-05-27','14:45:00',1),(13,'2024-05-28','13:30:00',1),(14,'2024-05-28','13:45:00',1),(15,'2024-05-28','14:00:00',1),(16,'2024-05-28','14:15:00',1),(17,'2024-05-28','14:30:00',1),(18,'2024-05-28','14:45:00',1),(19,'2024-05-29','13:30:00',1),(20,'2024-05-29','13:45:00',1),(21,'2024-05-29','14:00:00',1),(22,'2024-05-29','14:15:00',1),(23,'2024-05-29','14:30:00',1),(24,'2024-05-29','14:45:00',1),(25,'2024-05-30','13:30:00',1),(26,'2024-05-30','13:45:00',1),(27,'2024-05-30','14:00:00',1),(28,'2024-05-30','14:15:00',1),(29,'2024-05-30','14:30:00',1),(30,'2024-05-30','14:45:00',1),(31,'2024-05-31','13:30:00',1),(32,'2024-05-31','13:45:00',1),(33,'2024-05-31','14:00:00',1),(34,'2024-05-31','14:15:00',1),(35,'2024-05-31','14:30:00',1),(36,'2024-05-31','14:45:00',1),(37,'2024-05-26','13:30:00',2),(38,'2024-05-26','13:45:00',2),(39,'2024-05-26','14:00:00',2),(40,'2024-05-26','14:15:00',2),(41,'2024-05-26','14:30:00',2),(42,'2024-05-26','14:45:00',2),(43,'2024-05-27','13:30:00',2),(44,'2024-05-27','13:45:00',2),(45,'2024-05-27','14:00:00',2),(46,'2024-05-27','14:15:00',2),(47,'2024-05-27','14:30:00',2),(48,'2024-05-27','14:45:00',2),(49,'2024-05-28','13:30:00',2),(50,'2024-05-28','13:45:00',2),(51,'2024-05-28','14:00:00',2),(52,'2024-05-28','14:15:00',2),(53,'2024-05-28','14:30:00',2),(54,'2024-05-28','14:45:00',2),(55,'2024-05-29','13:30:00',2),(56,'2024-05-29','13:45:00',2),(57,'2024-05-29','14:00:00',2),(58,'2024-05-29','14:15:00',2),(59,'2024-05-29','14:30:00',2),(60,'2024-05-29','14:45:00',2),(61,'2024-05-30','13:30:00',2),(62,'2024-05-30','13:45:00',2),(63,'2024-05-30','14:00:00',2),(64,'2024-05-30','14:15:00',2),(65,'2024-05-30','14:30:00',2),(66,'2024-05-30','14:45:00',2),(67,'2024-05-31','13:30:00',2),(68,'2024-05-31','13:45:00',2),(69,'2024-05-31','14:00:00',2),(70,'2024-05-31','14:15:00',2),(71,'2024-05-31','14:30:00',2),(72,'2024-05-31','14:45:00',2),(73,'2024-05-26','13:30:00',3),(74,'2024-05-26','13:45:00',3),(75,'2024-05-26','14:00:00',3),(76,'2024-05-26','14:15:00',3),(77,'2024-05-26','14:30:00',3),(78,'2024-05-26','14:45:00',3),(79,'2024-05-27','13:30:00',3),(80,'2024-05-27','13:45:00',3),(81,'2024-05-27','14:00:00',3),(82,'2024-05-27','14:15:00',3),(83,'2024-05-27','14:30:00',3),(84,'2024-05-27','14:45:00',3),(85,'2024-05-28','13:30:00',3),(86,'2024-05-28','13:45:00',3),(87,'2024-05-28','14:00:00',3),(88,'2024-05-28','14:15:00',3),(89,'2024-05-28','14:30:00',3),(90,'2024-05-28','14:45:00',3),(91,'2024-05-29','13:30:00',3),(92,'2024-05-29','13:45:00',3),(93,'2024-05-29','14:00:00',3),(94,'2024-05-29','14:15:00',3),(95,'2024-05-29','14:30:00',3),(96,'2024-05-29','14:45:00',3),(97,'2024-05-30','13:30:00',3),(98,'2024-05-30','13:45:00',3),(99,'2024-05-30','14:00:00',3),(100,'2024-05-30','14:15:00',3),(101,'2024-05-30','14:30:00',3),(102,'2024-05-30','14:45:00',3),(103,'2024-05-31','13:30:00',3),(104,'2024-05-31','13:45:00',3),(105,'2024-05-31','14:00:00',3),(106,'2024-05-31','14:15:00',3),(107,'2024-05-31','14:30:00',3),(108,'2024-05-31','14:45:00',3);
/*!40000 ALTER TABLE `appointment_slot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_answer`
--

DROP TABLE IF EXISTS `forum_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_answer` (
  `id` int NOT NULL AUTO_INCREMENT,
  `body` varchar(255) DEFAULT NULL,
  `date_dubmitted` varchar(255) DEFAULT NULL,
  `admin_id` int DEFAULT NULL,
  `forum_question_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsm8jovg1cdo4tijnx0td7305y` (`admin_id`),
  KEY `FKs8s67hshh1bf91jnw7aa3rjcj` (`forum_question_id`),
  CONSTRAINT `FKs8s67hshh1bf91jnw7aa3rjcj` FOREIGN KEY (`forum_question_id`) REFERENCES `forum_question` (`id`),
  CONSTRAINT `FKsm8jovg1cdo4tijnx0td7305y` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_answer`
--

LOCK TABLES `forum_answer` WRITE;
/*!40000 ALTER TABLE `forum_answer` DISABLE KEYS */;
INSERT INTO `forum_answer` VALUES (1,'nflknfdlsfnsdkl','25/05/2024',1,1);
/*!40000 ALTER TABLE `forum_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_question`
--

DROP TABLE IF EXISTS `forum_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `date_submitted` varchar(255) DEFAULT NULL,
  `details` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6iiud5i6j0o5s4tn6aesdkhfd` (`user_id`),
  CONSTRAINT `FK6iiud5i6j0o5s4tn6aesdkhfd` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_question`
--

LOCK TABLES `forum_question` WRITE;
/*!40000 ALTER TABLE `forum_question` DISABLE KEYS */;
INSERT INTO `forum_question` VALUES (1,'2024-05-25','I got 2 doses of Pfizer and 2 doses of Moderna.\nDo I need another vaccine shot?','Do I really need my 5th (booster) shot?',4),(2,'2024-05-24','Hi! I was wondering what is the wait period between vaccination doses. Thanks!','How long do I have to wait for an appointment?',3);
/*!40000 ALTER TABLE `forum_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_pps` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `admin` int NOT NULL,
  `date_of_birth` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `nationality` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ne52mbkn6bvjvcancspi6mtui` (`user_pps`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'1234','The Internet',1,'07/10/1987','admin@vaxapp.com','John Doe','Male','Russian',''),(2,'1111','Bucharest',0,'05/06/1999','dragos@vaxapp.com','Dragos George','Male','Romanian',''),(3,'2222','Dublin',0,'05/06/1999','andra@vaxapp.com','Andra Antal','Female','Irish',''),(4,'3333','New York',0,'04/04/2000','andrei@vaxapp.com','Andrei Costin','Male','American','');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vaccine`
--

DROP TABLE IF EXISTS `vaccine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vaccine` (
  `id` int NOT NULL AUTO_INCREMENT,
  `date_received` date DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `admin_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `vaccine_centre_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKepicxryr0lousvihe0v5uc7vw` (`admin_id`),
  KEY `FK2n1jvw3n30bw4w5rgvsdt9f0e` (`user_id`),
  KEY `FKk0v6bt2bkypw2vebt8vk8cp2j` (`vaccine_centre_id`),
  CONSTRAINT `FK2n1jvw3n30bw4w5rgvsdt9f0e` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKepicxryr0lousvihe0v5uc7vw` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKk0v6bt2bkypw2vebt8vk8cp2j` FOREIGN KEY (`vaccine_centre_id`) REFERENCES `vaccine_centre` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vaccine`
--

LOCK TABLES `vaccine` WRITE;
/*!40000 ALTER TABLE `vaccine` DISABLE KEYS */;
INSERT INTO `vaccine` VALUES (1,'2021-09-09','pfizer',1,3,1),(2,'2022-01-15','pfizer',1,3,1),(3,'2020-08-08','moderna',1,4,2),(4,'2022-03-01','pfizer',1,4,2),(5,'2022-02-12','moderna',1,4,2);
/*!40000 ALTER TABLE `vaccine` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vaccine_centre`
--

DROP TABLE IF EXISTS `vaccine_centre`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vaccine_centre` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vaccine_centre`
--

LOCK TABLES `vaccine_centre` WRITE;
/*!40000 ALTER TABLE `vaccine_centre` DISABLE KEYS */;
INSERT INTO `vaccine_centre` VALUES (1,'RDS Vaccination Centre'),(2,'UCD Health Centre'),(3,'McDonald\'s Drive Thru');
/*!40000 ALTER TABLE `vaccine_centre` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
