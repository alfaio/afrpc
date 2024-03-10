package cn.yoube.afrpc.core.consumer;

import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author LimMF
 * @since 2024/3/10
 **/
public class AlInvocationHandler implements InvocationHandler {

    final static MediaType MEDIA_JSON = MediaType.get("application/json");

    Class<?> service;

    public AlInvocationHandler(Class<?> service) {
        this.service = service;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setService(service.getCanonicalName());
        request.setMethod(method.getName());
        request.setArgs(args);

        RpcResponse response = post(request);
        if (response.getStatus()) {
            Object data = response.getData();
            if (data instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) data;
                return jsonObject.toJavaObject(method.getReturnType());
            } else {
                return data;
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
