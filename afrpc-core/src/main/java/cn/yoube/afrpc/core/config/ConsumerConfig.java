package cn.yoube.afrpc.core.config;

import cn.yoube.afrpc.core.api.*;
import cn.yoube.afrpc.core.cluster.GrayRouter;
import cn.yoube.afrpc.core.cluster.RoundRibonLoadBalancer;
import cn.yoube.afrpc.core.consumer.ConsumerBootstrap;
import cn.yoube.afrpc.core.filter.ContextParameterFilter;
import cn.yoube.afrpc.core.meta.InstanceMeta;
import cn.yoube.afrpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author LimMF
 * @since 2024/3/10
 **/
@Configuration
@Import({AppProperties.class, ConsumerProperties.class})
public class ConsumerConfig {

    @Autowired
    AppProperties appProperties;
    @Autowired
    ConsumerProperties consumerProperties;

    @Bean
    ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            consumerBootstrap.start();
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRibonLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(consumerProperties.getGrayRatio());
    }

   /* @Bean
    public Filter cacheFilter() {
        return new CacheFilter();
    }*/

    @Bean
    public Filter mockFilter() {
        return new ContextParameterFilter();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter registryCenter() {
        return new ZkRegistryCenter();
    }

    @Bean
    @RefreshScope
    public RpcContext rpcContext(@Autowired Router<InstanceMeta> router,
                                 @Autowired LoadBalancer<InstanceMeta> loadBalancer,
                                 @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.id", appProperties.getId());
        context.getParameters().put("app.namespace", appProperties.getNamespace());
        context.getParameters().put("app.env", appProperties.getEnv());
        context.setConsumerProperties(consumerProperties);
        return context;
    }

}
