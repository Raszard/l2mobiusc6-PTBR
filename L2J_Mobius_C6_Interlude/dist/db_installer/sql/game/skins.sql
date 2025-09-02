/*
 Navicat Premium Data Transfer

 Source Server         : L2Localhost
 Source Server Type    : MariaDB
 Source Server Version : 110002
 Source Host           : localhost:3306
 Source Schema         : l2jmobiusc6

 Target Server Type    : MariaDB
 Target Server Version : 110002
 File Encoding         : 65001

 Date: 22/06/2023 23:10:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for skins
-- ----------------------------
DROP TABLE IF EXISTS `skins`;
CREATE TABLE `skins`  (
  `playerId` int(255) NOT NULL,
  `face` int(255) NULL DEFAULT NULL,
  `head` int(255) NULL DEFAULT NULL,
  `gloves` int(255) NULL DEFAULT NULL,
  `chest` int(255) NULL DEFAULT NULL,
  `legs` int(255) NULL DEFAULT NULL,
  `feet` int(255) NULL DEFAULT NULL,
  `back` int(255) NULL DEFAULT NULL,
  `hair` int(255) NULL DEFAULT NULL,
  PRIMARY KEY (`playerId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
