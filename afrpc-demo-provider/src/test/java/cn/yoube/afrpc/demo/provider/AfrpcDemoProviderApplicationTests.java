package cn.yoube.afrpc.demo.provider;

import cn.yoube.afrpc.core.test.TestZkServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AfrpcDemoProviderApplicationTests {

    static TestZkServer zkServer = new TestZkServer();

    @BeforeAll
    static void init() {
        zkServer.start();
    }

    @Test
    void contextLoads() {
    }

    @AfterAll
    static void destory() {
        zkServer.stop();
    }

}
