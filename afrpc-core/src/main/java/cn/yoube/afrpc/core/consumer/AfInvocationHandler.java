package cn.yoube.afrpc.core.consumer;

import cn.yoube.afrpc.core.api.*;
import cn.yoube.afrpc.core.consumer.http.OkHttpInvoker;
import cn.yoube.afrpc.core.meta.InstanceMeta;
import cn.yoube.afrpc.core.util.MethodUtils;
import cn.yoube.afrpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * 消费者动态代理类
 *
 * @author LimMF
 * @since 2024/3/10
 **/
@Slf4j
public class AfInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext context;
    List<InstanceMeta> providers;
    HttpInvoker httpInvoker;

    public AfInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters().getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }
        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethodSign(MethodUtils.methodSign(method));
        request.setArgs(args);
        // 重试是否需要包含filter这段？？？
        int retries = Integer.parseInt(context.getParameters()
                .getOrDefault("app.retries", "1"));

        while (retries-- > 0) {
            log.debug(" ===> retries: " + retries);
            try {
                for (Filter filter : context.getFilters()) {
                    RpcResponse preResponse = filter.preFilter(request);
                    if (preResponse != null) {
                        log.debug(filter.getClass().getName() + " ===> preFilter: " + preResponse.toString());
                        castReturnResult(method, preResponse);
                    }
                }

                List<InstanceMeta> instances = context.getRouter().choose(providers);
                InstanceMeta instance = context.getLoadBalancer().choose(instances);
                log.debug(" ===> loadBalance choose instance: " + instance);

                RpcResponse<?> response = httpInvoker.post(request, instance.toUrl());

                // 这里拿到的可能不是最终结果
                for (Filter filter : context.getFilters()) {
                    response = filter.postFilter(request, response);
                }

                return castReturnResult(method, response);
            } catch (Exception e) {
                if (!(e.getCause() instanceof SocketTimeoutException)) {
                    throw e;
                }
            }
        }
        return null;

    }

    @Nullable
    private static Object castReturnResult(Method method, RpcResponse<?> response) {
        if (response.getStatus()) {
            Object data = response.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            if (response.getException() instanceof RpcException rpcException) {
                throw rpcException;
            }
            throw new RpcException(response.getException(), RpcException.UnknownEx);
        }
    }


}
