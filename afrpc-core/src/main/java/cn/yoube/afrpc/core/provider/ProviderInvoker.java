package cn.yoube.afrpc.core.provider;

import cn.yoube.afrpc.core.api.RpcException;
import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.meta.ProviderMeta;
import cn.yoube.afrpc.core.util.TypeUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * @author LimMF
 * @since 2024/3/20
 **/
public class ProviderInvoker {
    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootstrap bootstrap) {
        this.skeleton = bootstrap.getSkeleton();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
        Method method = meta.getMethod();
        RpcResponse<Object> response = new RpcResponse<>();
        response.setStatus(false);
        try {
            Object data = method.invoke(meta.getServiceImpl(), TypeUtils.cast(request.getArgs(), method.getParameterTypes()));
            response.setStatus(true);
            response.setData(data);
        } catch (InvocationTargetException e) {
            response.setException(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            response.setException(new RpcException(e.getMessage()));
        }
        return response;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream().filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElseThrow(() -> new RuntimeException("方法签名不存在"));
    }
}
