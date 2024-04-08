package com.stoury.service

import com.stoury.exception.tag.TagCreateException
import com.stoury.repository.TagRepository
import spock.lang.Specification

class TagServiceTest extends Specification {
    def tagRepository = Mock(TagRepository)
    def tagService = new TagService(tagRepository)

    def "태그 길이 제한 조건 테스트"() {
        given:
        String shortTagName = "zx"
        when:
        tagService.createTagEntity(shortTagName)
        then:
        thrown(TagCreateException.class)
    }

    def "태그 불용어 제한 조건 테스트"() {
        given:
        String stopWord = "how"
        when:
        tagService.createTagEntity(stopWord)
        then:
        thrown(TagCreateException.class)
    }
}
