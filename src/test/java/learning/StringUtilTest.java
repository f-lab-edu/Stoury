package learning;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.*;

public class StringUtilTest {
    @Test
    @DisplayName("hasText()가 only 공백이면 거짓임")
    void hasTextTest(){
        String space = " ";
        String tap = "\t";
        String newLine = "\n";

        String wordAndSpace = "word and word";

        assertThat(StringUtils.hasText(space)).isFalse();
        assertThat(StringUtils.hasText(tap)).isFalse();
        assertThat(StringUtils.hasText(newLine)).isFalse();
        assertThat(StringUtils.hasText(wordAndSpace)).isTrue();
    }
}
