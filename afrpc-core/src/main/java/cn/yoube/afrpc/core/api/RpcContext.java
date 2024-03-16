package cn.yoube.afrpc.core.api;

import lombok.Data;

import java.util.List;

/**
 * @author LimMF
 * @since 2024/3/16
 **/
@Data
public class RpcContext {

    List<Filter> filters;
    Router router;
    LoadBalancer loadBalancer;

}
