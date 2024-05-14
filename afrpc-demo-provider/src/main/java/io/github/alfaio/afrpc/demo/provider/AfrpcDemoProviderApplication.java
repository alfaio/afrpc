package io.github.alfaio.afrpc.demo.provider;

import io.github.alfaio.afconfig.client.annotation.EnableAFConfig;
import io.github.alfaio.afrpc.core.api.RpcResponse;
import io.github.alfaio.afrpc.core.config.ProviderConfig;
import io.github.alfaio.afrpc.core.config.ProviderProperties;
import io.github.alfaio.afrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@EnableAFConfig
@SpringBootApplication
@RestController
@Import(ProviderConfig.class)
public class AfrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfrpcDemoProviderApplication.class, args);
    }

    @Autowired
    ProviderProperties providerProperties;

    @Autowired
    UserService userService;

    @GetMapping("metas")
    public String meta() {
        System.out.println(System.identityHashCode(providerProperties.getMetas()));
        return providerProperties.getMetas().toString();
    }

    @GetMapping(value = "/setPorts")
    public RpcResponse<String> setPorts(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("OK:" + ports);
        return response;
    }


    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            /*RpcRequest request = new RpcRequest();
            request.setService("io.github.alfaio.afrpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100L});
            RpcResponse<Object> response = invoke(request);
            System.out.println(response);

            RpcRequest request2 = new RpcRequest();
            request2.setService("io.github.alfaio.afrpc.demo.api.UserService");
            request2.setMethodSign("findById@2_int_java.lang.String");
            request2.setArgs(new Object[]{100L, "alfa"});
            RpcResponse<Object> response2 = invoke(request2);
            System.out.println(response2);*/
        };
    }

}
