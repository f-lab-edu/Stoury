ALTER TABLE `COMMENT` MODIFY `CREATED_AT` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE `DIARY` MODIFY `CREATED_AT` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE `FEED` MODIFY `CREATED_AT` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE `CHAT_MESSAGE` MODIFY `CREATED_AT` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);