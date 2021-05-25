import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j(topic = "testSlf4j")
public class TestSlf4j {
    @Test
    public void test(){
        log.debug("{}", "hello, world");
    }
}
