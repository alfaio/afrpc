package cn.yoube.afrpc.core.cluster;

import cn.yoube.afrpc.core.api.LoadBalancer;

import java.util.List;
import java.util.Random;

/**
 * @author LimMF
 * @since 2024/3/16
 **/
public class RandomLoadBalancer<T> implements LoadBalancer<T> {
    Random random = new Random();

    @Override
    public T choose(List<T> providers) {
        if (providers==null || providers.isEmpty()) return null;
        if (providers.size() ==1) return providers.get(0);
        return providers.get(random.nextInt(providers.size()));
    }

}
