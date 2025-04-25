package app.racla.raclaspringproxyservicev2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.cloud.config.enabled=false"})
class RaclaSpringProxyServiceV2ApplicationTests {

    @Test
    void contextLoads() {}

}
