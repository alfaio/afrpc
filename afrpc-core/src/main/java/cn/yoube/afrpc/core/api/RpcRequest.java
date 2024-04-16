package cn.yoube.afrpc.core.api;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LimMF
 * @since 2024/3/7
 **/
@Data
public class RpcRequest {

    String service;
    String methodSign;
    Object[] args;
    // 跨调用方需要传递的参数
    private Map<String, String> params = new HashMap<>();
}
