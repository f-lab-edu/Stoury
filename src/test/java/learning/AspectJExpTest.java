package learning;

import com.stoury.dto.MemberCreateRequest;
import com.stoury.service.BasicMemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import java.lang.reflect.Method;

public class AspectJExpTest {
    @Test
    @DisplayName("execution(* com.stoury.service.*Service.*(..))이 BasicMemberService의 메소드에 부합해야함")
    void expressionTest() throws NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* com.stoury.service.*Service.*(..))");

        Method createMember = BasicMemberService.class.getMethod("createMember", MemberCreateRequest.class);

        Assertions.assertThat(pointcut.matches(createMember, BasicMemberService.class)).isTrue();
    }
}
