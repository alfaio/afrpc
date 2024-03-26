package cn.yoube.afrpc.demo.consumer;

import cn.yoube.afrpc.core.test.TestZkServer;
import cn.yoube.afrpc.demo.provider.AfrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class AfrpcDemoConsumerApplicationTests {

    static ApplicationContext context;

    static TestZkServer zkServer = new TestZkServer();

    @BeforeAll
    static void init() {
        zkServer.start();
        context = SpringApplication.run(AfrpcDemoProviderApplication.class, "--server.port=8084");
    }

    @Test
    void contextLoads() {
    }

    @AfterAll
    static void destory() {
        SpringApplication.exit(context, () -> 1);
        zkServer.stop();
    }

}
