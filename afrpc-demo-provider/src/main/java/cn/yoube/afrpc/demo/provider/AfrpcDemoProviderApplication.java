package cn.yoube.afrpc.demo.provider;

import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.provider.ProviderBootstrap;
import cn.yoube.afrpc.core.provider.ProviderConfig;
import cn.yoube.afrpc.core.util.MethodUtils;
import cn.yoube.afrpc.demo.api.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@Import(ProviderConfig.class)
public class AfrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfrpcDemoProviderApplication.class, args);
    }

    @Autowired
    ProviderBootstrap providerBootstrap;

    @PostMapping(value = "/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerBootstrap.invoke(request);
    }

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            /*RpcRequest request = new RpcRequest();
            request.setService("cn.yoube.afrpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100L});
            RpcResponse response = invoke(request);
            System.out.println(response);

            RpcRequest request2 = new RpcRequest();
            request2.setService("cn.yoube.afrpc.demo.api.UserService");
            request2.setMethodSign("findById@2_int_java.lang.String");
            request2.setArgs(new Object[]{100L, "alfa"});
            RpcResponse response2 = invoke(request2);
            System.out.println(response2);*/
        };
    }

}
