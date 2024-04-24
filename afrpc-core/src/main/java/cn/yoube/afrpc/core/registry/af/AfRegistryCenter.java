package cn.yoube.afrpc.core.registry.af;

import cn.yoube.afrpc.core.api.RegistryCenter;
import cn.yoube.afrpc.core.consumer.HttpInvoker;
import cn.yoube.afrpc.core.meta.InstanceMeta;
import cn.yoube.afrpc.core.meta.ServiceMeta;
import cn.yoube.afrpc.core.registry.ChangedListener;
import cn.yoube.afrpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author LimMF
 * @since 2024/4/24
 **/
@Slf4j
public class AfRegistryCenter implements RegistryCenter {

    @Value("${afregistry.servers}")
    private String servers;

    @Override
    public void start() {
        log.info(" ===> [AFRegistry] : start with server: {}", servers);
        executor = Executors.newScheduledThreadPool(1);
    }

    @SneakyThrows
    @Override
    public void stop() {
        log.info(" ===> [AFRegistry] : stop with server: {}", servers);
        executor.shutdown();
        boolean isTerminated = executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        if (!isTerminated) {
            executor.shutdownNow();
        }
        log.info(" ===> [AFRegistry] : stop success");
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ===> [AFRegistry] : register instance {} for service:{}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/reg?service=" + service.toPath(), Void.class);
        log.info(" ===> [AFRegistry] : registered {}}", instance);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ===> [AFRegistry] : unregister instance {} for service:{}", instance, service);
        HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/unreg?service=" + service.toPath(), Void.class);
        log.info(" ===> [AFRegistry] : unregistered {}}", instance);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ===> [AFRegistry] : find all instance for service:{}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(servers + "/findAll?service=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ===> [AFRegistry] : findAll = {}", instances);
        return instances;
    }

    Map<String, Long> VERSIONS = new HashMap<>();
    ScheduledExecutorService executor = null;

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        executor.scheduleWithFixedDelay(() -> {
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
