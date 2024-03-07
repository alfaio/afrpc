package cn.yoube.afrpc.core.api;

import lombok.Data;

/**
 * @author LimMF
 * @since 2024/3/7
 **/@Data
public class RpcRequest {

    String service;
    String method;
    Object[] args;
}
