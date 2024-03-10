package cn.yoube.afrpc.core.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author LimMF
 * @since 2024/3/10
 **/
@Configuration
public class ConsumerConfig {

    @Bean
    ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumer_Runner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            consumerBootstrap.start();
        };
    }
}
