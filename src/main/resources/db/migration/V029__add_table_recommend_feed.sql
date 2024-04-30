CREATE TABLE `RECOMMEND_FEED`
(
    `ID`        BIGINT NOT NULL AUTO_INCREMENT,
    `MEMBER_ID` BIGINT NOT NULL,
    `FEED_ID`   BIGINT NOT NULL,
    `CREATED_AT` DATETIME(6) NOT NULL,
    PRIMARY KEY (`ID`),
    INDEX `IDX_RECOMMEND_FEED_MEMBER_CREATED_AT` (`MEMBER_ID`, `CREATED_AT`)
) ENGINE = InnoDB
  DEFAULT CHARSET = UTF8MB4;