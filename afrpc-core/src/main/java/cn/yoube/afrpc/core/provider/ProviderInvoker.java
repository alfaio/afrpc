package cn.yoube.afrpc.core.provider;

import cn.yoube.afrpc.core.api.RpcContext;
import cn.yoube.afrpc.core.api.RpcException;
import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.meta.ProviderMeta;
import cn.yoube.afrpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * invoker for provider
 *
 * @author LimMF
 * @since 2024/3/20
 **/
@Slf4j
public class ProviderInvoker {
    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootstrap bootstrap) {
        this.skeleton = bootstrap.getSkeleton();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        log.debug(" ===> ProviderInvoker.invoke(request:{})", request);
        if (!request.getParams().isEmpty()) {
            request.getParams().forEach(RpcContext::setContextParameter);
        }
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
        Method method = meta.getMethod();
        RpcResponse<Object> response = new RpcResponse<>();
        response.setStatus(false);
        try {
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
            Object data = method.invoke(meta.getServiceImpl(), args);
            response.setStatus(true);
            response.setData(data);
        } catch (InvocationTargetException e) {
            response.setException(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException | IllegalArgumentException e) {
            response.setException(new RpcException(e.getMessage()));
        } finally {
            RpcContext.contextParameters.get().clear(); // 防止内存泄露和上下文污染
        }
        return response;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {
        if (args == null || args.length == 0) return args;
        Object[] actual = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actual[i] = TypeUtils.castGeneric(args[i], parameterTypes[i], genericParameterTypes[i]);
        }
        return actual;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream().filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElseThrow(() -> new RuntimeException("方法签名不存在"));
    }
}
