package cn.yoube.afrpc.core.consumer;

import cn.yoube.afrpc.core.annotation.RpcConsumer;
import cn.yoube.afrpc.core.api.LoadBalancer;
import cn.yoube.afrpc.core.api.RegistryCenter;
import cn.yoube.afrpc.core.api.Router;
import cn.yoube.afrpc.core.api.RpcContext;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LimMF
 * @since 2024/3/10
 **/
@Data
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {
    ApplicationContext applicationContext;
    Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    public void start() {

        Router router = applicationContext.getBean(Router.class);
        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        RegistryCenter registryCenter = applicationContext.getBean(RegistryCenter.class);

        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);

        /*String urls = environment.getProperty("afrpc.providers", "");
        if (Strings.isEmpty(urls)) {
            System.out.println("afrpc.providers is empty");
        }
        List<String> providers = List.of(urls.split(","));*/

        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
            List<Field> fields = findAnnotatedField(bean.getClass());
            fields.stream().forEach(f -> {
                try {
                    Class<?> service = f.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
//                        consumer = createConsumer(service, context, providers);
                        consumer = createFromRegistry(service, context, registryCenter);
                    }
                    f.setAccessible(true);
                    f.set(bean, consumer);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private Object createFromRegistry(Class<?> service, RpcContext context, RegistryCenter registryCenter) {
        String serviceName = service.getCanonicalName();
        List<String> providers = registryCenter.fetchAll(serviceName);
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new AfInvocationHandler(service, context, providers));
    }

    private Object createConsumer(Class<?> service, RpcContext context, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new AfInvocationHandler(service, context, providers));
    }

    private List<Field> findAnnotatedField(Class<?> aClass) {
        List<Field> result = new ArrayList<>();
        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(RpcConsumer.class)) {
                    result.add(field);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return result;
    }
}
