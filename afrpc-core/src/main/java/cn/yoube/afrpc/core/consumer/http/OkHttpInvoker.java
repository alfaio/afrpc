package cn.yoube.afrpc.core.consumer.http;

import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;
import cn.yoube.afrpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson.JSON;
import com.google.errorprone.annotations.Var;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author LimMF
 * @since 2024/3/20
 **/
public class OkHttpInvoker  implements HttpInvoker {
    final static MediaType MEDIA_JSON = MediaType.get("application/json");

    OkHttpClient client;

    public OkHttpInvoker() {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public RpcResponse<?> post(RpcRequest request, String url) {
        String reqJson = JSON.toJSONString(request);
        System.out.println(" ===> request json = "+ reqJson);
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, MEDIA_JSON))
                .build();
        try {
            String respJson = client.newCall(req).execute().body().string();
            System.out.println(" ===> response json = " + respJson);
            RpcResponse<Object> response = JSON.parseObject(respJson, RpcResponse.class);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
