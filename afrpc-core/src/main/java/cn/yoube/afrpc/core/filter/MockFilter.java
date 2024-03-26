package cn.yoube.afrpc.core.filter;

import cn.yoube.afrpc.core.api.Filter;
import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.util.MethodUtils;
import cn.yoube.afrpc.core.util.MockUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author LimMF
 * @since 2024/3/26
 **/
public class MockFilter implements Filter {
    @SneakyThrows
    @Override
    public RpcResponse preFilter(RpcRequest request) {
        Class<?> service = Class.forName(request.getService());
        Method method = findMethod(service, request.getMethodSign());
        Object result = MockUtils.mock(method.getReturnType());
        RpcResponse<Object> rpcResponse = new RpcResponse<>();
        rpcResponse.setStatus(true);
        rpcResponse.setData(result);
        return rpcResponse;
    }

    private Method findMethod(Class<?> service, String methodSign) {
        Arrays.stream(service.getMethods())
                .filter(method -> !MethodUtils.checkLocalMethod(method))
                .filter(method -> methodSign.equals(MethodUtils.methodSign(method)))
                .findFirst().orElse(null);
        return null;
    }

    @Override
    public RpcResponse postFilter(RpcRequest request, RpcResponse response) {
        return null;
    }
}
