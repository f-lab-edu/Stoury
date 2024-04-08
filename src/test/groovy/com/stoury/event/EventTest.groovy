package com.stoury.event

import com.stoury.domain.Feed
import com.stoury.domain.GraphicContent
import com.stoury.domain.Member
import com.stoury.domain.Tag
import com.stoury.projection.FeedResponseEntity
import com.stoury.repository.FeedRepository
import com.stoury.service.FeedService
import com.stoury.service.storage.StorageService
import spock.lang.Specification

class EventTest extends Specification {
    def storageService = Mock(StorageService)
    def feedRepository = Mock(FeedRepository)

    def eventHandler = new EventHandlers(storageService, feedRepository)

    def "FEED_RESPONSE 삽입 테스트"() {
        given:
        def feed = new Feed(id: 1L,
                member: new Member(id:1L,username: "user"),
                graphicContents: [
                        new GraphicContent(id: 11L, path: "/gc1"),
                        new GraphicContent(id: 12L, path: "/gc2"),
                        new GraphicContent(id: 13L, path: "/gc3"),
                ],
                tags: [
                        new Tag("t1"),
                        new Tag("t2"),
                        new Tag("t3"),
                ]
        )
        def event = new FeedResponseCreateEvent(_, feed)
        when:
        eventHandler.onFeedResponseCreateEventHandler(event)
        then:
        1 * feedRepository.saveFeedResponse(_ as FeedResponseEntity) >>
                new FeedResponseEntity(feedId: 1L,
                        writerId: 1L,
                        writerUsername: "user",
                        graphicContentPaths: '[{"id": 11, "path": "/gc1"}, {"id": 12, "path": "/gc2"}, {"id": 13, "path": "/gc3"}]',
                        tagNames: '["t1", "t2", "t3"]'
                )
    }

    def "graphicContent json화 테스트"(){
        given:
        def graphicContents =  [
                new GraphicContent(id: 11L, path: "/gc1"),
                new GraphicContent(id: 12L, path: "/gc2"),
                new GraphicContent(id: 13L, path: "/gc3"),
        ]
        when:
        def paths = eventHandler.concat(graphicContents).replaceAll("\\s+", "")
        then:
        paths == '[{"id": 11, "path": "/gc1"}, {"id": 12, "path": "/gc2"}, {"id": 13, "path": "/gc3"}]'.replaceAll("\\s+", "")
    }

    def "tagNames json화 테스트"() {
        given:
        def tags = [
                new Tag("t1"),
                new Tag("t2"),
                new Tag("t3"),
        ] as Set
        when:
        def tagNames = eventHandler.concat(tags).replaceAll("\\s+", "")
        then:
        tagNames == '["t1", "t2", "t3"]'.replaceAll("\\s+", "")

    }
}
