package cn.yoube.afrpc.demo.provider;

import cn.yoube.afrpc.core.annotation.RpcProvider;
import cn.yoube.afrpc.demo.api.User;
import cn.yoube.afrpc.demo.api.UserService;
import org.springframework.stereotype.Component;

/**
 * @author LimMF
 * @since 2024/3/7
 **/
@Component
@RpcProvider
public class UserServiceImpl implements UserService {
    @Override
    public User findById(Integer id) {
        return new User(id, "al" + System.currentTimeMillis());
    }

    @Override
    public Integer getId(Integer id) {
        return id;
    }

    @Override
    public String getName() {
        return "al" + System.currentTimeMillis();
    }
}
