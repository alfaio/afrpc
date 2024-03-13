package cn.yoube.afrpc.core.consumer;

import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.util.MethodUtils;
import cn.yoube.afrpc.core.util.TypeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author LimMF
 * @since 2024/3/10
 **/
public class AfInvocationHandler implements InvocationHandler {

    final static MediaType MEDIA_JSON = MediaType.get("application/json");

    Class<?> service;

    public AfInvocationHandler(Class<?> service) {
        this.service = service;
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
        RpcResponse response = post(request);
        if (response.getStatus()) {
            Object data = response.getData();
            if (data instanceof JSONObject jsonObject) {
                return jsonObject.toJavaObject(method.getReturnType());
            } else if (data instanceof JSONArray jsonArray){
                Object[] array = jsonArray.toArray();
                Class<?> componentType = method.getReturnType().getComponentType();
                Object result = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    Object target = TypeUtils.cast(array[i], componentType);
                    Array.set(result, i , target);
                }
                return result;
            }else {
                return TypeUtils.cast(data, method.getReturnType());
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
    private RpcResponse post(RpcRequest request) {
        String reqJson = JSON.toJSONString(request);
        Request req = new Request.Builder()
                .url("http://localhost:8080/")
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
