package org.jeecg.module.maintenance.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 描述:反射的相关帮助类.
 *
 * @author ppliu
 * created in 2019/4/19 9:53
 */
public class ReflectUtil {
    /**
     * 根据属性，获取get方法
     *
     * @param ob   对象
     * @param name 属性名
     */
    public static Object getValue(Object ob, String name) {
        Method[] m = ob.getClass().getMethods();
        for (int i = 0; i < m.length; i++) {
            if (("get" + name).toLowerCase().equals(m[i].getName().toLowerCase())) {
                try {
                    return m[i].invoke(ob);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 根据属性，拿到set方法，并把值set到对象中
     *
     * @param obj   对象
     */
    public static void setValue(Object obj, String filedName, Class<?> typeClass, Object value) {
        String methodName = "set" + filedName.substring(0, 1).toUpperCase() + filedName.substring(1);
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, typeClass);
            method.invoke(obj, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
