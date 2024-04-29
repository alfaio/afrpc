package io.github.alfaio.afrpc.core.annotation;

import io.github.alfaio.afrpc.core.config.ConsumerConfig;
import io.github.alfaio.afrpc.core.config.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableAfrpc {
}
