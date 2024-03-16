package cn.yoube.afrpc.core.consumer;

import cn.yoube.afrpc.core.api.*;
import cn.yoube.afrpc.core.util.MethodUtils;
import cn.yoube.afrpc.core.util.TypeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author LimMF
 * @since 2024/3/10
 **/
public class AfInvocationHandler implements InvocationHandler {

    final static MediaType MEDIA_JSON = MediaType.get("application/json");

    Class<?> service;
    RpcContext context;
    List<String> providers;

    public AfInvocationHandler(Class<?> service, RpcContext context, List<String> providers) {
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

        List<String> urls = context.getRouter().choose(providers);
        String url = (String) context.getLoadBalancer().choose(urls);

        RpcResponse response = post(request, url);
        Class<?> returnType = method.getReturnType();
        if (response.getStatus()) {
            Object data = response.getData();
            if (data instanceof JSONObject jsonObject) {
                if (Map.class.isAssignableFrom(returnType)) {
                    Map resultMap = new HashMap();
                    Type genericReturnType = method.getGenericReturnType();
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        Class<?> keyType = (Class<?>) actualTypeArguments[0];
                        Class<?> valueType = (Class<?>) actualTypeArguments[1];
                        jsonObject.entrySet().forEach(entry->{
                            resultMap.put(TypeUtils.cast(entry.getKey(), keyType), TypeUtils.cast(entry.getValue(), valueType));
                        });
                        return resultMap;
                    }
                }
                return jsonObject.toJavaObject(returnType);
            } else if (data instanceof JSONArray jsonArray){
                Object[] array = jsonArray.toArray();
                if (returnType.isArray()) {
                    Class<?> componentType = returnType.getComponentType();
                    Object result = Array.newInstance(componentType, array.length);
                    for (int i = 0; i < array.length; i++) {
                        Object target = TypeUtils.cast(array[i], componentType);
                        Array.set(result, i , target);
                    }
                    return result;
                } else if (List.class.isAssignableFrom(returnType)) {
                    Type genericReturnType = method.getGenericReturnType();
                    System.out.println(genericReturnType);
                    List<Object> result = new ArrayList<>(array.length);
                    if (genericReturnType instanceof ParameterizedType parameterizedType) {
                        Type actualType = parameterizedType.getActualTypeArguments()[0];
                        System.out.println(actualType);
                        for (Object object : array) {
                            result.add(TypeUtils.cast(object, (Class<?>) actualType));
                        }
                    } else {
                        result.addAll(Arrays.asList(array));
                    }
                    return result;
                } else {
                    return null;
                }
            }else {
                return TypeUtils.cast(data, returnType);
            }
        } else {
            throw new RuntimeException(response.getException());
        }
    }

    OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();
    private RpcResponse post(RpcRequest request, String url) {
        String reqJson = JSON.toJSONString(request);
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, MEDIA_JSON))
                .build();
        try {
            String respJson = httpClient.newCall(req).execute().body().string();
            return JSON.parseObject(respJson, RpcResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
