package cn.yoube.afrpc.core.consumer;

import cn.yoube.afrpc.core.api.RpcContext;
import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.consumer.http.OkHttpInvoker;
import cn.yoube.afrpc.core.meta.InstanceMeta;
import cn.yoube.afrpc.core.util.MethodUtils;
import cn.yoube.afrpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
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
    HttpInvoker httpInvoker = new OkHttpInvoker();

    public AfInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
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

        List<InstanceMeta> instances = context.getRouter().choose(providers);
        InstanceMeta instance = context.getLoadBalancer().choose(instances);
        log.debug(" ===> loadBalance choose instance: " + instance);

        RpcResponse<?> response = httpInvoker.post(request, instance.toUrl());
        if (response.getStatus()) {
            Object data = response.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            throw new RuntimeException(response.getException());
        }
    }


}
