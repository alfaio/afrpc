package cn.yoube.afrpc.core.test;

import lombok.SneakyThrows;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.utils.CloseableUtils;

/**
 * @author LimMF
 * @since 2024/3/26
 **/
public class TestZkServer {

    TestingCluster cluster;
    @SneakyThrows
    public void start() {
        InstanceSpec instanceSpec = new InstanceSpec(null, 2181,
                -1, -1, true,
                -1, -1, -1);
        cluster = new TestingCluster(instanceSpec);
        System.out.println("TestingZooKeeperServer starting ...");
        cluster.start();
        cluster.getServers().forEach(s -> System.out.println(s.getInstanceSpec()));
        System.out.println("TestingZooKeeperServer started.");
    }

    @SneakyThrows
    public void stop() {
        System.out.println("TestingZooKeeperServer stopping ...");
        cluster.stop();
        CloseableUtils.closeQuietly(cluster);
        System.out.println("TestingZooKeeperServer stopped.");
    }
}
