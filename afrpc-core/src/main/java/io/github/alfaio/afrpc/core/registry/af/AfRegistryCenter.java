package io.github.alfaio.afrpc.core.registry.af;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.github.alfaio.afrpc.core.api.RegistryCenter;
import io.github.alfaio.afrpc.core.consumer.HttpInvoker;
import io.github.alfaio.afrpc.core.meta.InstanceMeta;
import io.github.alfaio.afrpc.core.meta.ServiceMeta;
import io.github.alfaio.afrpc.core.registry.ChangedListener;
import io.github.alfaio.afrpc.core.registry.Event;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author LimMF
 * @since 2024/4/24
 **/
@Slf4j
public class AfRegistryCenter implements RegistryCenter {

    @Value("${afregistry.servers}")
    private String servers;

    Map<String, Long> VERSIONS = new HashMap<>();
    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();

    ScheduledExecutorService consumerExecutor = null;
    ScheduledExecutorService providerExecutor = null;

    @Override
    public void start() {
        log.info(" ===> [AFRegistry] : start with server: {}", servers);
        consumerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor = Executors.newScheduledThreadPool(1);
        providerExecutor.scheduleWithFixedDelay(() -> {
            RENEWS.forEach((instance, services) -> {
                log.info(" ===> [AFRegistry] : renews instance {} for services:{}", instance, services);
                String serviceJoin = services.stream().map(ServiceMeta::toPath).collect(Collectors.joining(","));
                Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/renews?services=" + serviceJoin, Long.class);
                log.info(" ===> [AFRegistry] : renewed {}", timestamp);
            });
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        log.info(" ===> [AFRegistry] : stop with server: {}", servers);
        shutDown(consumerExecutor);
        shutDown(providerExecutor);
        log.info(" ===> [AFRegistry] : stop success");
    }

    @SneakyThrows
    private void shutDown(ScheduledExecutorService executor){
        executor.shutdown();
        boolean isTerminated = executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        if (!isTerminated) {
            executor.shutdownNow();
        }
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ===> [AFRegistry] : register instance {} for service:{}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/reg?service=" + service.toPath(), Void.class);
        log.info(" ===> [AFRegistry] : registered {}}", instance);
        RENEWS.add(instance, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ===> [AFRegistry] : unregister instance {} for service:{}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/unreg?service=" + service.toPath(), Void.class);
        log.info(" ===> [AFRegistry] : unregistered {}}", instance);
        RENEWS.remove(instance, service);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ===> [AFRegistry] : find all instance for service:{}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(servers + "/findAll?service=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ===> [AFRegistry] : findAll = {}", instances);
        return instances;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        consumerExecutor.scheduleWithFixedDelay(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(servers + "/version?service=" + service.toPath(), Long.class);
            log.info(" ===> [AFRegistry] : version = {}, newVersion = {}", version, newVersion);
            if (newVersion > version) {
                List<InstanceMeta> instances = fetchAll(service);
                listener.fire(new Event(instances));
                VERSIONS.put(service.toPath(), newVersion);
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }
}
