package com.stoury.utils;

import java.util.Set;

// MySql 에서 fulltext index에서 아래 키워드들은 검색대상이 안됨
public class StopWords {
    private static Set<String> stopWordList = Set.of(
            "a",
            "about",
            "an",
            "are",
            "as",
            "at",
            "be",
            "by",
            "com",
            "de",
            "en",
            "for",
            "from",
            "how",
            "i",
            "in",
            "is",
            "it",
            "la",
            "of",
            "on",
            "or",
            "that",
            "the",
            "this",
            "to",
            "was",
            "what",
            "when",
            "where",
            "who",
            "will",
            "with",
            "und",
            "www"
    );

    public static boolean isStopWord(String word){
        return stopWordList.contains(word);
    }
}
