package io.github.alfaio.afrpc.core.api;

import java.util.List;

/**
 * 1、负载均衡：wightedRR-权重
 * 8081 w=25%；8082 w=75%
 * 0-99 random < 25 -> 8081; else 8082
 *
 * 2、负载均衡：AAWR-自适应，按各个服务的处理能力分配
 * 8081响应时间：10ms  (8082的十倍处理能力)
 * 8082响应时间：100ms
 * w = avg * 0.3 + last * 0.7
 * avg:历史 last:最近一次，最近一次的处理能力更有参考价值
 *
 * @author LimMF
 * @since 2024/3/16
 */
public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer Default = providers -> (providers == null || providers.isEmpty()) ? null : providers.get(0);

}
