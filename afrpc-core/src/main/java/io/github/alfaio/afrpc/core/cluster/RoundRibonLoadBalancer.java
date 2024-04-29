package io.github.alfaio.afrpc.core.cluster;

import io.github.alfaio.afrpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LimMF
 * @since 2024/3/16
 **/
public class RoundRibonLoadBalancer<T> implements LoadBalancer<T> {

    AtomicInteger index = new AtomicInteger();

    @Override
    public T choose(List<T> providers) {
        if (providers==null || providers.isEmpty()) return null;
        if (providers.size() ==1) return providers.get(0);
//        return providers.get(index.getAndIncrement() % providers.size());
        // getAndIncrement 有可能返回负数，负数取模还是负数，可以通过“& 0x7fffffff”保证为正数
        return providers.get((index.getAndIncrement() & 0x7fffffff) % providers.size());
    }

}
