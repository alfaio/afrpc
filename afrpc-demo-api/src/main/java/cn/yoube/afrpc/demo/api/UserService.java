package cn.yoube.afrpc.demo.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    User findById(int id);

    User findById(int id, String name);

    long getId(long id);

    long getId(User user);

    long getId(float id);

    String getName();

    String getName(int id);

    int[] getIds();

    long[] getLongIds();

    int[] getIds(int[] ids);

    Long[] getLongIds(Long[] longIds);

    User[] findUsers(User[] users);

    List<User> getList(List<User> userList);

    Set<User> getSet(Set<User> userSet);

    Map<String, User> getMap(Map<String, User> userMap);

    Boolean getFlag(boolean flag);

    User findById(long id);

    User ex(boolean flag);

    User findWithTimeout(int sleepTime);

    void setSleepPorts(String ports);
}
