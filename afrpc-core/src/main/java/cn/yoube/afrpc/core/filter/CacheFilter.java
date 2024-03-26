package cn.yoube.afrpc.core.filter;

import cn.yoube.afrpc.core.api.Filter;
import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LimMF
 * @since 2024/3/26
 **/
public class CacheFilter implements Filter {

    // 替换成guava cache，加容量限制、过期时间
    static Map<String, RpcResponse> cache = new ConcurrentHashMap<>();

    @Override
    public RpcResponse preFilter(RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public RpcResponse postFilter(RpcRequest request, RpcResponse response) {
        cache.putIfAbsent(request.toString(), response);
        return response;
    }
}
