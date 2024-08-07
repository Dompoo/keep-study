package hello.proxy.pureproxy.decorator;

import hello.proxy.pureproxy.decorator.code.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DecoratorPatternTest {

    @Test
    void noDecorator() {
        RealComponent component = new RealComponent();
        DecoratorPatternClient client = new DecoratorPatternClient(component);

        client.execute();
    }

    @Test
    void decorator1() {
        Component component = new RealComponent();
        Component decorator = new MessageDecorator(component);
        DecoratorPatternClient client = new DecoratorPatternClient(decorator);

        client.execute();
    }

    @Test
    void decorator2() {
        Component component = new RealComponent();
        Component decorator1 = new MessageDecorator(component);
        Component decorator2 = new TimeDecorator(decorator1);
        DecoratorPatternClient client = new DecoratorPatternClient(decorator2);

        client.execute();
    }
}
