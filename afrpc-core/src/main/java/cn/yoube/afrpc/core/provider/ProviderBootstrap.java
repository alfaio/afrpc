package cn.yoube.afrpc.core.provider;

import cn.yoube.afrpc.core.annotation.RpcProvider;
import cn.yoube.afrpc.core.api.RegistryCenter;
import cn.yoube.afrpc.core.config.AppProperties;
import cn.yoube.afrpc.core.config.ProviderProperties;
import cn.yoube.afrpc.core.meta.InstanceMeta;
import cn.yoube.afrpc.core.meta.ProviderMeta;
import cn.yoube.afrpc.core.meta.ServiceMeta;
import cn.yoube.afrpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * 提供者启动类
 *
 * @author LimMF
 * @since 2024/3/7
 **/
@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;
    RegistryCenter rc;
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
    private String ip;
    private InstanceMeta instance;

    @Value("${server.port}")
    private String port;

    @Autowired
    AppProperties appProperties;
    @Autowired
    ProviderProperties providerProperties;

    @PostConstruct
    private void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        providers.values().forEach(this::genInterface);
        rc = applicationContext.getBean(RegistryCenter.class);
    }

    @SneakyThrows
    public void start() {
        ip = InetAddress.getLocalHost().getHostAddress();
        instance = InstanceMeta.http(ip, Integer.valueOf(port));
        instance.getParameters().putAll(providerProperties.getMetas());
        rc.start();
        skeleton.keySet().forEach(this::registryService);
    }

    @PreDestroy
    private void stop() {
        log.info(" ===> unregister all services");
        skeleton.keySet().forEach(this::unRegistryService);
        rc.stop();
    }

    private void registryService(String service) {
        ServiceMeta serviceMeta = builderService(service);
        rc.register(serviceMeta, instance);
    }

    private ServiceMeta builderService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .env(appProperties.getEnv()).name(service).build();
        return serviceMeta;
    }

    private void unRegistryService(String service) {
        ServiceMeta serviceMeta = builderService(service);
        rc.unregister(serviceMeta, instance);
    }

    private void genInterface(Object serviceImpl) {
        Arrays.stream(serviceImpl.getClass().getInterfaces()).forEach(anInterface -> {
            Method[] methods = anInterface.getMethods();
            for (Method method : methods) {
                if (MethodUtils.checkLocalMethod(method)) {
                    continue;
                }
                createProvider(anInterface, serviceImpl, method);
            }
        });
    }

    private void createProvider(Class<?> anInterface, Object serviceImpl, Method method) {
        ProviderMeta providerMeta = ProviderMeta.builder().method(method).serviceImpl(serviceImpl).methodSign(MethodUtils.methodSign(method)).build();
        log.info(" ===> create a provider: " + providerMeta);
        skeleton.add(anInterface.getCanonicalName(), providerMeta);
    }
}
