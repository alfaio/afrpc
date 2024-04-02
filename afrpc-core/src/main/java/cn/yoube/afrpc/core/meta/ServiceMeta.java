package cn.yoube.afrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述服务元数据
 * @author LimMF
 * @since 2024/3/20
 **/
@Data
@Builder
public class ServiceMeta {

    String app;
    String namespace;
    String env;
    String name;
    private Map<String, String> parameters = new HashMap<>();

    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }

    public String toMetas() {
        return JSON.toJSONString(parameters);
    }
}
