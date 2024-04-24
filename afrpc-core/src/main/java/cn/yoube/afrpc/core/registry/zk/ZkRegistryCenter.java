package cn.yoube.afrpc.core.registry.zk;

import cn.yoube.afrpc.core.api.RegistryCenter;
import cn.yoube.afrpc.core.api.RpcException;
import cn.yoube.afrpc.core.meta.InstanceMeta;
import cn.yoube.afrpc.core.meta.ServiceMeta;
import cn.yoube.afrpc.core.registry.ChangedListener;
import cn.yoube.afrpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LimMF
 * @since 2024/3/19
 **/
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    @Value("${afrpc.zk.server:localhost:2181}")
    String servers;
    @Value("${afrpc.zk.root:afrpc}")
    String root;
    private CuratorFramework client = null;

    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root)
                .retryPolicy(retryPolicy)
                .build();
        log.info(" ===> zk client starting to server [" + servers + "/" + root + "]");
        client.start();
    }

    @Override
    public void stop() {
        client.close();
        log.info(" ===> zk client stopped.");
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.toMetas().getBytes());
            }
            // 创建实例的临时节点
            String instancePath = servicePath + "/" + instance.toPath();
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instance.toMetas().getBytes());
            log.info(" ===> register to zk: " + instance);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            // 删除实例节点
            String instancePath = servicePath + "/" + instance.toPath();
            client.delete().forPath(instancePath);
            log.info(" ===> unregister from zk: " + instance.toPath());
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所以子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info(" ===> fetchAll from zk: " + servicePath);
            return nodes.stream().map(node -> {
                String[] strings = node.split("_");
                InstanceMeta instance = InstanceMeta.http (strings[0], Integer.valueOf(strings[1]));

                String nodePath = servicePath + "/" + node;
                byte[] bytes;
                try {
                    bytes = client.getData().forPath(nodePath);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Map<String, Object> params = JSON.parseObject(new String(bytes));
                params.forEach((k, v) -> {
                    instance.getParameters().put(k, v == null ? null : v.toString());
                });
                return instance;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true).setMaxDepth(2).build();
        cache.getListenable().addListener((curator, event) -> {
            // 有任何节点变动就会执行
            log.info("zh subscribe event: " + event);
            List<InstanceMeta> nodes = fetchAll(service);
            listener.fire(new Event(nodes));
        });
        cache.start();
    }
}
