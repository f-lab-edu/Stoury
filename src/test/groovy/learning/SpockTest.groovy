package learning

import spock.lang.Specification

class SpockTest extends Specification {
    def "Spock"() {
        given:
        def num1 = 10L
        def num2 = 10L

        when:
        def sum = CalculateTest.sum(num1, num2)

        then:
        sum == 20L
    }

    def "Parameterized Test"() {
        given:
        def num1 = 10L

        expect:
        CalculateTest.sum(num1, num2) == result

        where:
        num2 | result
        1L   | 11L
        2L   | 12L
        10L  | 20L
    }

    def "Error Exception divide by zero"() {
        given:
        def num2 = 0L

        when:
        CalculateTest.divide(15L, num2)

        then:
        def e = thrown(ArithmeticException.class)
        e.message == CalculateTest.ZERO_DIVISION_MESSAGE
    }

    def "Mocking Test"() {
        given:
        def mockedCalculator = Mock(CalculateTest.class)

        when:
        long sub = mockedCalculator.sub(10L, 0L)

        then:
        mockedCalculator.sub(10L, 0L) >> 0L
        0L == sub
    }
}
