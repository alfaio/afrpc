package cn.yoube.afrpc.demo.consumer;

import cn.yoube.afrpc.core.annotation.RpcConsumer;
import cn.yoube.afrpc.core.consumer.ConsumerConfig;
import cn.yoube.afrpc.demo.api.OrderService;
import cn.yoube.afrpc.demo.api.User;
import cn.yoube.afrpc.demo.api.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
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

    @GetMapping("/")
    public User findById(@RequestParam int id) {
        return userService.findById(id);
    }

    @Bean
    public ApplicationRunner consumerRunner() {
        return x -> {

            System.out.println(" userService.getId(10) = " + userService.getId(10));

            System.out.println(" userService.getId(10f) = " +
                    userService.getId(10f));

            System.out.println(" userService.getId(new User(100,\"AF\")) = " +
                    userService.getId(new User(100,"AF")));

            User user = userService.findById(1);
            System.out.println("RPC result userService.findById(1) = " + user);

            User user1 = userService.findById(1, "ak");
            System.out.println("RPC result userService.findById(1, \"ak\") = " + user1);

            System.out.println(userService.getName());

            System.out.println(userService.getName(123));

            System.out.println(userService.toString());

            System.out.println(userService.getId(11));

            System.out.println(userService.getName());

            System.out.println(" ===> userService.getLongIds()");
            for (long id : userService.getLongIds()) {
                System.out.println(id);
            }

            System.out.println(" ===> userService.getLongIds(int[] ids)");
            for (long id : userService.getIds(new int[]{4,5,6})) {
                System.out.println(id);
            }

            System.out.println(" ===> userService.getLongIds(Long[] longIds)");
            for (Long id : userService.getLongIds(new Long[]{10L,15L,16L})) {
                System.out.println(id);
            }

            System.out.println(" ===> 测试参数和返回值都是List类型");
            List<User> list = userService.getList(List.of(
                    new User(100, "AF100"),
                    new User(101, "AF101")));
            list.forEach(System.out::println);

            /*System.out.println(" ===> 测试参数和返回值都是Set类型");
            Set<User> set = userService.getSet(Set.of(
                    new User(100, "AF100"),
                    new User(101, "AF101")));
            set.forEach(System.out::println);*/

            // 测试参数和返回值都是Map类型
            System.out.println(" ===> 测试参数和返回值都是Map类型");
            Map<String, User> map = new HashMap<>();
            map.put("A200", new User(200, "AF200"));
            map.put("A201", new User(201, "AF201"));
            userService.getMap(map).forEach(
                    (k,v) -> System.out.println(k + " -> " + v)
            );

            System.out.println(" ===> 测试参数和返回值都是Boolean/boolean类型");
            System.out.println("userService.getFlag(false) = " + userService.getFlag(false));

            //Order order = orderService.findById(2);
            //System.out.println("RPC result orderService.findById(2) = " + order);

            //demo2.test();

//            Order order404 = orderService.findById(404);
//            System.out.println("RPC result orderService.findById(2) = " + order404);
        };
    }
}
