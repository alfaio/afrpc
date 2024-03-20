package cn.yoube.afrpc.core.registry;

import cn.yoube.afrpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author LimMF
 * @since 2024/3/19
 **/
@Data
@AllArgsConstructor
public class Event {

    List<InstanceMeta> data;
}
