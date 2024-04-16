package cn.yoube.afrpc.core.api;

import cn.yoube.afrpc.core.config.ConsumerProperties;
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
    private Map<String, String> parameters = new HashMap<>();
    private ConsumerProperties consumerProperties;

    public static ThreadLocal<Map<String, String>> contextParameters = ThreadLocal.withInitial(() -> new HashMap<>());

    public String getParam(String key) {
        return parameters.get(key);
    }

    public static void setContextParameter(String key, String value) {
        contextParameters.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return contextParameters.get().get(key);
    }

    public static void removeContextParameter(String key) {
        contextParameters.get().remove(key);
    }
}
