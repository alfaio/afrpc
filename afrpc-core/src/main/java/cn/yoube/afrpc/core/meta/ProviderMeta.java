package cn.yoube.afrpc.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author LimMF
 * @since 2024/3/13
 **/
@Data
public class ProviderMeta {
    Method method;
    String methodSign;
    Object serviceImpl;
}
