package cn.yoube.afrpc.demo.consumer;

import cn.yoube.afrpc.core.annotation.RpcConsumer;
import cn.yoube.afrpc.core.consumer.ConsumerConfig;
import cn.yoube.afrpc.demo.api.OrderService;
import cn.yoube.afrpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ConsumerConfig.class)
public class AfrpcDemoConsumerApplication {

    @RpcConsumer
    UserService userService;
    @RpcConsumer
    OrderService orderService;

    public static void main(String[] args) {
        SpringApplication.run(AfrpcDemoConsumerApplication.class, args);
    }

    @Bean
    public ApplicationRunner consumerRunner() {
        return x -> {
            System.out.println(userService.findById(100));

//            System.out.println(userService.toString());

//            System.out.println(userService.getId(100));

            System.out.println(userService.getName());

//            System.out.println(orderService.findById(10));

//            System.out.println(orderService.findById(404));
        };
    }
}
