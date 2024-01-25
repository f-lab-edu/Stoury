package learning

import spock.lang.Specification

class SpockFixtureTest extends Specification {
    def list = new ArrayList()

    def "Empty List"() {
        expect:
        list.size() == 0
    }

    def "Add Element"() {
        when:
        list.add("Element")

        then:
        list.size() == 1
    }

    def "Count method calls"() {
        given:
        def calculator = Mock(CalculateTest)

        when:
        calculator.sub(10, 10)
        calculator.sub(10, 10)
        calculator.sub(10, 10)

        then:
        3 * calculator.sub(10, 10)
    }

    def "Stub test"() {
        setup:
        def calculator = Mock(CalculateTest)
        calculator.sub(_, _) >> 5L

        when:
        def result = calculator.sub(0,0)

        then:
        calculator != null
        result == 5L
    }
}
