package io.github.alfaio.afrpc.core.consumer.http;

import com.alibaba.fastjson.JSON;
import io.github.alfaio.afrpc.core.api.RpcRequest;
import io.github.alfaio.afrpc.core.api.RpcResponse;
import io.github.alfaio.afrpc.core.consumer.HttpInvoker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.concurrent.TimeUnit;

/**
 * @author LimMF
 * @since 2024/3/20
 **/
@Slf4j
public class OkHttpInvoker  implements HttpInvoker {
    final static MediaType JSON_TYPE = MediaType.get("application/json");

    OkHttpClient client;

    public OkHttpInvoker(int timeout) {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public RpcResponse<?> post(RpcRequest request, String url) {
        String reqJson = JSON.toJSONString(request);
        log.debug(" ===> request json = "+ reqJson);
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();
        try {
            String respJson = client.newCall(req).execute().body().string();
            log.debug(" ===> response json = " + respJson);
            RpcResponse<Object> response = JSON.parseObject(respJson, RpcResponse.class);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String post(String requestString, String url) {
        log.debug(" ===> post  url = {}, requestString = {}", requestString, url);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestString, JSON_TYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug(" ===> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) {
        log.debug(" ===> get url = " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug(" ===> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
