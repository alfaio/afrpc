package cn.yoube.afrpc.core.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author LimMF
 * @since 2024/3/13
 **/
@Slf4j
public class TypeUtils {

    public static Object castMethodResult(Method method, Object data) {
        Class<?> returnType = method.getReturnType();
        if (data instanceof JSONObject jsonObject) {
            if (Map.class.isAssignableFrom(returnType)) {
                Map resultMap = new HashMap();
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    Class<?> keyType = (Class<?>) actualTypeArguments[0];
                    Class<?> valueType = (Class<?>) actualTypeArguments[1];
                    jsonObject.entrySet().forEach(entry->{
                        resultMap.put(TypeUtils.cast(entry.getKey(), keyType), TypeUtils.cast(entry.getValue(), valueType));
                    });
                    return resultMap;
                }
            }
            return jsonObject.toJavaObject(returnType);
        } else if (data instanceof JSONArray jsonArray){
            Object[] array = jsonArray.toArray();
            if (returnType.isArray()) {
                Class<?> componentType = returnType.getComponentType();
                Object result = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    Object target = TypeUtils.cast(array[i], componentType);
                    Array.set(result, i , target);
                }
                return result;
            } else if (List.class.isAssignableFrom(returnType)) {
                Type genericReturnType = method.getGenericReturnType();
                List<Object> result = new ArrayList<>(array.length);
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    for (Object object : array) {
                        result.add(TypeUtils.cast(object, (Class<?>) actualType));
                    }
                } else {
                    result.addAll(Arrays.asList(array));
                }
                return result;
            } else {
                return null;
            }
        }else {
            return TypeUtils.cast(data, returnType);
        }
    }

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
        if (origin instanceof JSONObject jsonObject) {
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
        } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return Boolean.valueOf(origin.toString());
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
