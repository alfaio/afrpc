package cn.yoube.afrpc.core.provider;

import cn.yoube.afrpc.core.annotation.RpcProvider;
import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LimMF
 * @since 2024/3/7
 **/
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;
    private HashMap<String, Object> svcMap = new HashMap<>();

    public RpcResponse invoke(RpcRequest request) {
        Object service = svcMap.get(request.getService());
        Method method = findMethod(service, request.getMethod());
        RpcResponse response = new RpcResponse();
        response.setStatus(false);
        try {
            Object data = method.invoke(service, request.getArgs());
            response.setStatus(true);
            response.setData(data);
        } catch (InvocationTargetException e) {
            response.setException(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            response.setException(new RuntimeException(e.getMessage()));
        }
        return response;
    }

    private Method findMethod(Object service, String methodName) {
        //            return service.getClass().getMethod(method);
        for (Method method : service.getClass().getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new RuntimeException("方法不存在：" + methodName);
    }

    @PostConstruct
    private void setSvcMap() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        providers.forEach((k, v) -> svcMap.put(getSvcName(v), v));
    }

    private String getSvcName(Object v) {
        Class<?> anInterface = v.getClass().getInterfaces()[0];
        return anInterface.getCanonicalName();
    }
}
