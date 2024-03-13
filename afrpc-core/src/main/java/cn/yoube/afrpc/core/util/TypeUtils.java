package cn.yoube.afrpc.core.util;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author LimMF
 * @since 2024/3/13
 **/
public class TypeUtils {

    public static Object cast(Object origin, Class<?> type) {
        if (origin == null) return null;
        if (type.isArray()) {
            if (origin instanceof Collection collection) {
                origin = collection.toArray();
            }
            int length = Array.getLength(origin);
            Class<?> componentType = type.getComponentType();
            Object result = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Object originItem = Array.get(origin, i);
                Array.set(result, i, TypeUtils.castOne(originItem, componentType));
            }
            return result;
        }else {
            return TypeUtils.castOne(origin, type);
        }
    }

    private static Object castOne(Object origin, Class<?> type) {
        if (origin == null) return null;
        Class<?> originClass = origin.getClass();
        if (type.isAssignableFrom(originClass)) {
            return origin;
        }
        if (origin instanceof HashMap map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(type);
        }
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return Integer.valueOf(origin.toString());
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return Long.valueOf(origin.toString());
        } else if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return Float.valueOf(origin.toString());
        } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return Double.valueOf(origin.toString());
        } else if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
            return Byte.valueOf(origin.toString());
        } else if (type.equals(Short.class) || type.equals(Short.TYPE)) {
            return Short.valueOf(origin.toString());
        } else if (type.equals(Character.class) || type.equals(Character.TYPE)) {
            return origin.toString().charAt(0);
        }
        return null;
    }


    public static Object[] cast(Object[] origins, Class<?>[] types) {
        if (origins == null || origins.length == 0) {
            return origins;
        }
        Object[] targets = new Object[origins.length];
        for (int i = 0; i < origins.length; i++) {
            targets[i] = TypeUtils.cast(origins[i], types[i]);
        }
        return targets;
    }
}
