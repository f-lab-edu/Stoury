CREATE FULLTEXT INDEX `FEED_RESPONSE_INDEX_FULLTEXT_TAG_NAMES` ON `FEED_RESPONSE` (`TAG_NAMES`) WITH PARSER NGRAM;
CREATE INDEX `FEED_RESPONSE_INDEX_FEED_ID_MEMBER_ID` ON `FEED_RESPONSE` (`FEED_ID`, `WRITER_ID`);