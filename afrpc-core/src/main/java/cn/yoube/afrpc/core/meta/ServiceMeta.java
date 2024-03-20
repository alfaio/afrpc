package cn.yoube.afrpc.core.meta;

import lombok.Builder;
import lombok.Data;

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


    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }

}
