package cn.yoube.afrpc.demo.api;

public interface UserService {

    User findById(Integer id);

    Integer getId(Integer id);

    String getName();
}
