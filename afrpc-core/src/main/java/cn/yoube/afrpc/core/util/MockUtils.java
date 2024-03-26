package cn.yoube.afrpc.core.util;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

/**
 * @author LimMF
 * @since 2024/3/26
 **/
public class MockUtils {

    public static Object mock(Class type) {
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return 1;
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return 100L;
        }
        if (Number.class.isAssignableFrom(type)) {
            return 1;
        }
        if (String.class.equals(type)) {
            return "this is a mock string";
        }
        return mockPojo(type);
    }

    @SneakyThrows
    private static Object mockPojo(Class type) {
        Object result = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            Object mock = mock(fieldType);
            field.set(result, mock);
        }
        return result;
    }
}
