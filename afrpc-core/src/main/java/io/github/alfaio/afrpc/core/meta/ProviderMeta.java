package io.github.alfaio.afrpc.core.meta;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 描述provider的映射关系
 *
 * @author LimMF
 * @since 2024/3/13
 **/
@Data
@Builder
public class ProviderMeta {
    Method method;
    String methodSign;
    Object serviceImpl;
}
