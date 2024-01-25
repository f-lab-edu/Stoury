package learning;

public class CalculateTest {
    public static final String ZERO_DIVISION_MESSAGE = "Divide by Zero";
    public static long sum(long num1, long num2) {
        return num1 + num2;
    }

    public static long divide(long num1, long num2) {
        try {
            return num1 / num2;
        } catch (ArithmeticException e) {
            throw new ArithmeticException(ZERO_DIVISION_MESSAGE);
        }
    }

    public long sub(long num1, long num2){
        return num1 - num2;
    }
}
