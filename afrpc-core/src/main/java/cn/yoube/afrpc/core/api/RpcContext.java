package cn.yoube.afrpc.core.api;

import cn.yoube.afrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LimMF
 * @since 2024/3/16
 **/
@Data
public class RpcContext {

    List<Filter> filters;
    Router<InstanceMeta> router;
    LoadBalancer<InstanceMeta> loadBalancer;

    Map<String, String> parameters = new HashMap<>();

}
