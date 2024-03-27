package cn.yoube.afrpc.core.consumer.http;

import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author LimMF
 * @since 2024/3/20
 **/
@Slf4j
public class OkHttpInvoker  implements HttpInvoker {
    final static MediaType MEDIA_JSON = MediaType.get("application/json");

    OkHttpClient client;

    public OkHttpInvoker(int timeout) {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MICROSECONDS)
                .writeTimeout(timeout, TimeUnit.MICROSECONDS)
                .connectTimeout(timeout, TimeUnit.MICROSECONDS)
                .build();
    }

    @Override
    public RpcResponse<?> post(RpcRequest request, String url) {
        String reqJson = JSON.toJSONString(request);
        log.debug(" ===> request json = "+ reqJson);
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, MEDIA_JSON))
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
}
