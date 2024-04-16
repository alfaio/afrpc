package cn.yoube.afrpc.demo.provider;

import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.provider.ProviderConfig;
import cn.yoube.afrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import(ProviderConfig.class)
public class AfrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfrpcDemoProviderApplication.class, args);
    }

    @Autowired
    UserService userService;

    @GetMapping(value = "/setPorts")
    public RpcResponse<String> setPorts(@RequestParam("ports") String ports) {
         userService.setSleepPorts(ports);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("OK:" + ports);
        return response;
    }


    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            /*RpcRequest request = new RpcRequest();
            request.setService("cn.yoube.afrpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100L});
            RpcResponse<Object> response = invoke(request);
            System.out.println(response);

            RpcRequest request2 = new RpcRequest();
            request2.setService("cn.yoube.afrpc.demo.api.UserService");
            request2.setMethodSign("findById@2_int_java.lang.String");
            request2.setArgs(new Object[]{100L, "alfa"});
            RpcResponse<Object> response2 = invoke(request2);
            System.out.println(response2);*/
        };
    }

}
