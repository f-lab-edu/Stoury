package com.stoury.batch;

import com.stoury.dto.FrequentTags;
import com.stoury.utils.cachekeys.FrequentTagsKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.ArrayList;
import java.util.Set;

@Slf4j
public class FrequentTagsScanReader implements ItemReader<FrequentTags> {
    private static final int BATCH_SCAN_SIZE = 100;
    private final RedisTemplate redisTemplate;

    private boolean batchFinished = false;
    private FrequentTagsCursor cursor;

    public FrequentTagsScanReader(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public FrequentTags read() {
        if (batchFinished) {
            return null;
        }

        cursor = loadCursor();

        while (cursor.hasNext()) {
            String key = cursor.next();

            Set<String> tags = redisTemplate.opsForSet().members(key);
            Long memberId = extractMemberIdFromKey(key);

            if (tags != null && !tags.isEmpty()) {
                return new FrequentTags(memberId, new ArrayList<>(tags));
            }
        }

        batchFinished = true; // cursor에 더 이상 데이터가 없으면 배치 완료로 표시
        cursor.close();
        return null;
    }

    private FrequentTagsCursor loadCursor() {
        if (cursor == null) {
            return new FrequentTagsCursor(redisTemplate, FrequentTagsKey.FREQUENT_TAGS_KEY);
        }
        return cursor;
    }

    private Long extractMemberIdFromKey(String key) {
        return Long.parseLong(key.split(":")[1]);
    }

    class FrequentTagsCursor {
        private final Cursor<byte[]> cursor;

        public FrequentTagsCursor(RedisTemplate redisTemplate, String keyPrefix) {
            ScanOptions scanOption = ScanOptions.scanOptions()
                    .match(keyPrefix + "*")
                    .count(BATCH_SCAN_SIZE)
                    .build();
            RedisConnection connection = getConnection(redisTemplate);

            cursor = connection.keyCommands()
                    .scan(scanOption);
        }

        public boolean hasNext() {
            return cursor.hasNext();
        }

        public String next() {
            return new String(cursor.next());
        }

        public void close() {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception ex) {
                log.error("error occured while iterating cursor");
            }
        }

        private RedisConnection getConnection(RedisTemplate redisTemplate) {
            RedisConnectionFactory connectionFactory = getConnectionFactory(redisTemplate);

            RedisConnection connection = connectionFactory.getConnection();
            if (connection == null) { // NOSONAR
                throw new RedisConnectionFailureException("Cannot get redis connection");
            }
            return connection;
        }

        private RedisConnectionFactory getConnectionFactory(RedisTemplate redisTemplate) {
            RedisConnectionFactory redisConnectionFactory = redisTemplate.getConnectionFactory();
            if (redisConnectionFactory == null) {
                throw new RedisConnectionFailureException("No connection factory set");
            }
            return redisConnectionFactory;
        }
    }
}
