package com.stoury.batch;

import com.stoury.dto.FrequentTags;
import com.stoury.utils.cachekeys.FrequentTagsKey;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.ArrayList;
import java.util.Set;

public class FrequentTagsScanReader implements ItemReader<FrequentTags> {
    private static final int BATCH_SCAN_SIZE = 100;
    private final RedisTemplate redisTemplate;
    private Cursor<byte[]> cursor;
    private boolean batchFinished = false;
    private final ScanOptions scanOption = ScanOptions.scanOptions()
            .match(FrequentTagsKey.getFrequentTagsKey("*"))
            .count(BATCH_SCAN_SIZE)
            .build();

    public FrequentTagsScanReader(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.cursor = this.redisTemplate.getConnectionFactory().getConnection().keyCommands().scan(scanOption);
    }

    @Override
    public FrequentTags read() {
        if (batchFinished) {
            return null;
        }

        while (cursor.hasNext()) {
            String key = new String(cursor.next());
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

    private Long extractMemberIdFromKey(String key) {
        return Long.parseLong(key.split(":")[1]);
    }
}
