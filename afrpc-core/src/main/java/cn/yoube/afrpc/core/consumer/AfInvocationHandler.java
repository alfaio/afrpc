package cn.yoube.afrpc.core.consumer;

import cn.yoube.afrpc.core.api.*;
import cn.yoube.afrpc.core.consumer.http.OkHttpInvoker;
import cn.yoube.afrpc.core.meta.InstanceMeta;
import cn.yoube.afrpc.core.util.MethodUtils;
import cn.yoube.afrpc.core.util.TypeUtils;
import goverance.SlidingTimeWindow;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    final List<InstanceMeta> providers;
    final List<InstanceMeta> isolateProviders = new ArrayList<>();
    final List<InstanceMeta> halfOpenProviders = new ArrayList<>();
    final Map<String, SlidingTimeWindow> windows = new HashMap<>();
    HttpInvoker httpInvoker;

    ScheduledExecutorService executor;

    public AfInvocationHandler(Class<?> service, RpcContext context, List<InstanceMeta> providers) {
        this.service = service;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters().getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
        this.executor = Executors.newScheduledThreadPool(1);
        this.executor.scheduleWithFixedDelay(this::halfOpen, 10, 60, TimeUnit.SECONDS);
    }

    private void halfOpen() {
        log.debug(" ===> half open isolateProviders: {}", isolateProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolateProviders);
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

                InstanceMeta instance;
                synchronized (halfOpenProviders) {
                    if (halfOpenProviders.isEmpty()) {
                        List<InstanceMeta> instances = context.getRouter().choose(providers);
                        instance = context.getLoadBalancer().choose(instances);
                        log.debug(" ===> loadBalance choose instance: " + instance);
                    } else {
                        instance = halfOpenProviders.remove(0);
                        log.debug(" ===> check alive instance: {}", instance);
                    }
                }

                RpcResponse<?> response;
                String url = instance.toUrl();
                try {
                    response = httpInvoker.post(request, url);
                } catch (Exception e) {
                    // 故障的规则统计和隔离，
                    // 每一次异常，记录一次，统计30s的异常数。
                    synchronized (windows) {
                        SlidingTimeWindow window = windows.computeIfAbsent(url, k -> new SlidingTimeWindow());
                        window.record(System.currentTimeMillis());
                        log.debug("instance {} in window with {}", url, window.getSum());
                        // 发生了10次，就做故障隔离
                        if (window.getSum() >= 10) {
                            isolate(instance);
                        }
                    }
                    throw e;
                }

                synchronized (providers) {
                    if (!providers.contains(instance)) {
                        isolateProviders.remove(instance);
                        providers.add(instance);
                        log.debug(" ===> instance {} is recovered, isolateProviders: {}, providers: {}", instance, isolateProviders, providers);
                    }
                }

                // 这里拿到的可能不是最终结果
                for (Filter filter : context.getFilters()) {
                    response = filter.postFilter(request, response);
                    if (response!=null) {
                        break;
                    }
                }

                return castReturnResult(method, response);
            } catch (Exception e) {
                if (!(e.getCause() instanceof SocketTimeoutException) || retries <= 0) {
                    throw e;
                }
            }
        }
        return null;

    }

    private void isolate(InstanceMeta instance) {
        log.debug(" ===> isolate instance: {}", instance);
        providers.remove(instance);
        log.debug(" ===> providers: {}", providers);
        isolateProviders.add(instance);
        log.debug(" ===> isolateProviders: {}", isolateProviders);
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
