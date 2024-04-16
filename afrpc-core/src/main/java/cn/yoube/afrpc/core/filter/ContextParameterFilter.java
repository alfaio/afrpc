package cn.yoube.afrpc.core.filter;


import cn.yoube.afrpc.core.api.Filter;
import cn.yoube.afrpc.core.api.RpcContext;
import cn.yoube.afrpc.core.api.RpcRequest;
import cn.yoube.afrpc.core.api.RpcResponse;

import java.util.Map;

/**
 * 处理上下文参数.
 *
 * @author LimMF
 * @since 2024/3/26
 **/
public class ContextParameterFilter implements Filter {
    @Override
    public RpcResponse preFilter(RpcRequest request) {
        Map<String, String> params = RpcContext.contextParameters.get();
        if (!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public RpcResponse postFilter(RpcRequest request, RpcResponse response) {
        RpcContext.contextParameters.get().clear();
        return null;
    }
}
