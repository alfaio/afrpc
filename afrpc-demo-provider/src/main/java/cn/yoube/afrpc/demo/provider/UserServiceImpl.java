package cn.yoube.afrpc.demo.provider;

import cn.yoube.afrpc.core.annotation.RpcProvider;
import cn.yoube.afrpc.demo.api.User;
import cn.yoube.afrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author LimMF
 * @since 2024/3/7
 **/
@Component
@RpcProvider
public class UserServiceImpl implements UserService {

    @Autowired
    Environment environment;

    @Override
    public User findById(int id) {
        return new User(id, "AF-" + environment.getProperty("server.port") +
                "_" + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "AF-" + name + "_" + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId().longValue();
    }

    @Override
    public long getId(float id) {
        return 1L;
    }

    @Override
    public String getName() {
        return "AF123";
    }

    @Override
    public String getName(int id) {
        return "AF-" + id;
    }

    @Override
    public int[] getIds() {
        return new int[] {100,200,300};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{1,2,3};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }

    @Override
    public Long[] getLongIds(Long[] longIds) {
        return longIds;
    }
}
