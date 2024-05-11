package io.github.alfaio.afrpc.core.registry.af;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.github.alfaio.afrpc.core.api.RegistryCenter;
import io.github.alfaio.afrpc.core.consumer.HttpInvoker;
import io.github.alfaio.afrpc.core.meta.InstanceMeta;
import io.github.alfaio.afrpc.core.meta.ServiceMeta;
import io.github.alfaio.afrpc.core.registry.ChangedListener;
import io.github.alfaio.afrpc.core.registry.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LimMF
 * @since 2024/4/24
 **/
@Slf4j
public class AfRegistryCenter implements RegistryCenter {

    public static final String REG_PATH = "/reg";
    public static final String UNREG_PATH = "/unreg";
    public static final String FIND_ALL_PATH = "/findAll";
    public static final String VERSION_PATH = "/version";
    public static final String RENEW_PATH = "/renews";

    @Value("${afregistry.servers}")
    private String servers;

    Map<String, Long> VERSIONS = new HashMap<>();
    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    AfHealthChecker healthChecker = new AfHealthChecker();

    @Override
    public void start() {
        log.info(" ===> [AFRegistry] : start with server: {}", servers);
        healthChecker.start();
        providerCheck();
    }

    private void providerCheck() {
        healthChecker.providerCheck(() -> {
            RENEWS.forEach((instance, services) -> {
                log.info(" ===> [AFRegistry] : renews instance {} for services:{}", instance, services);
                Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance), renewPath(services), Long.class);
                log.info(" ===> [AFRegistry] : renewed {}", timestamp);
            });
        });
    }

    @Override
    public void stop() {
        log.info(" ===> [AFRegistry] : stop with server: {}", servers);
        healthChecker.stop();
        log.info(" ===> [AFRegistry] : stop success");
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ===> [AFRegistry] : register instance {} for service:{}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), regPath(service), Void.class);
        log.info(" ===> [AFRegistry] : registered {}}", instance);
        RENEWS.add(instance, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ===> [AFRegistry] : unregister instance {} for service:{}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), unregPath(service), Void.class);
        log.info(" ===> [AFRegistry] : unregistered {}}", instance);
        RENEWS.remove(instance, service);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ===> [AFRegistry] : find all instance for service:{}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(findAllPath(service), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ===> [AFRegistry] : findAll = {}", instances);
        return instances;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        healthChecker.consumerCheck(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(versionPath(service), Long.class);
            log.info(" ===> [AFRegistry] : version = {}, newVersion = {}", version, newVersion);
            if (newVersion > version) {
                List<InstanceMeta> instances = fetchAll(service);
                listener.fire(new Event(instances));
                VERSIONS.put(service.toPath(), newVersion);
            }
        });
    }

    private String regPath(ServiceMeta service) {
        return path(REG_PATH, service);
    }

    private String unregPath(ServiceMeta service) {
        return path(UNREG_PATH, service);
    }

    private String findAllPath(ServiceMeta service) {
        return path(FIND_ALL_PATH, service);
    }

    private String versionPath(ServiceMeta service) {
        return path(VERSION_PATH, service);
    }

    private String renewPath(List<ServiceMeta> services) {
        return path(RENEW_PATH, services);
    }

    private String path(String context, ServiceMeta service) {
        return servers + context + "?service=" + service.toPath();
    }

    private String path(String context, List<ServiceMeta> services) {
        String serviceJoin = services.stream().map(ServiceMeta::toPath).collect(Collectors.joining(","));
        return servers + context + "?services=" + serviceJoin;
    }
}
