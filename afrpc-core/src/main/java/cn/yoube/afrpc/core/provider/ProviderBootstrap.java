package cn.yoube.afrpc.core.provider;

import cn.yoube.afrpc.core.annotation.RpcProvider;
import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.meta.ProviderMeta;
import cn.yoube.afrpc.core.util.MethodUtils;
import cn.yoube.afrpc.core.util.TypeUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author LimMF
 * @since 2024/3/7
 **/
@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @PostConstruct
    private void start() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        providers.values().forEach(this::setSkeleton);
    }

    public RpcResponse invoke(RpcRequest request) {
        String methodSign = request.getMethodSign();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        ProviderMeta meta = findProviderMeta(providerMetas, methodSign);
        Method method = meta.getMethod();
        RpcResponse response = new RpcResponse();
        response.setStatus(false);
        try {
            Object data = method.invoke(meta.getServiceImpl(), TypeUtils.cast(request.getArgs(), method.getParameterTypes()));
            response.setStatus(true);
            response.setData(data);
        } catch (InvocationTargetException e) {
            response.setException(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            response.setException(new RuntimeException(e.getMessage()));
        }
        return response;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream().filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElseThrow(() -> new RuntimeException("方法签名不存在"));
    }

    private void setSkeleton(Object o) {
        Arrays.stream(o.getClass().getInterfaces()).forEach(anInterface -> {
            Method[] methods = anInterface.getMethods();
            for (Method method : methods) {
                if (MethodUtils.checkLocalMethod(method)) {
                    continue;
                }
                createProvider(anInterface, o, method);
            }
        });
    }

    private void createProvider(Class<?> anInterface, Object o, Method method) {
        ProviderMeta meta = new ProviderMeta();
        meta.setMethod(method);
        meta.setServiceImpl(o);
        meta.setMethodSign(MethodUtils.methodSign(method));
        skeleton.add(anInterface.getCanonicalName(), meta);
    }
}
