package cn.yoube.afrpc.core.annotation;

import cn.yoube.afrpc.core.config.ConsumerConfig;
import cn.yoube.afrpc.core.config.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableAfrpc {
}
