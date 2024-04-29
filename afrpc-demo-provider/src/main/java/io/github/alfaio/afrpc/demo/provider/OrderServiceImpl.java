package io.github.alfaio.afrpc.demo.provider;

import io.github.alfaio.afrpc.core.annotation.RpcProvider;
import io.github.alfaio.afrpc.demo.api.Order;
import io.github.alfaio.afrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

/**
 * @author LimMF
 * @since 2024/3/7
 **/
@Component
@RpcProvider
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findById(Integer id) {
        if(id == 404) {
            throw new RuntimeException("404 exception");
        }
        return new Order(id.longValue(), 100f);
    }
}
