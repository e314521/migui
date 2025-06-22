package com.imgui;

import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;



public class ImClassLoader extends DexClassLoader {
    public static class DexUtils {
        public static void addDexPath(ClassLoader classLoader, File dexFile, File optimizedDir)
                throws Exception {

            // 1. 获取DexPathList对象
            Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(classLoader);

            // 2. 获取原dexElements数组
            Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object[] dexElements = (Object[]) dexElementsField.get(pathList);

            // 3. 生成新DEX的Element数组
            Method makeDexElements = pathList.getClass().getDeclaredMethod(
                    "makeDexElements", List.class, File.class, List.class, ClassLoader.class);
            makeDexElements.setAccessible(true);
            Object[] newElements = (Object[]) makeDexElements.invoke(
                    pathList, Arrays.asList(dexFile), optimizedDir, null, classLoader);

            // 4. 合并数组并写回
            Object[] combined = Arrays.copyOf(dexElements, dexElements.length + newElements.length);
            System.arraycopy(newElements, 0, combined, dexElements.length, newElements.length);
            dexElementsField.set(pathList, combined);
        }
    }

    /**
     * Creates a {@code DexClassLoader} that finds interpreted and native
     * code.  Interpreted classes are found in a set of DEX files contained
     * in Jar or APK files.
     *
     * <p>The path lists are separated using the character specified by the
     * {@code path.separator} system property, which defaults to {@code :}.
     *
     * @param dexPath            the list of jar/apk files containing classes and
     *                           resources, delimited by {@code File.pathSeparator}, which
     *                           defaults to {@code ":"} on Android
     * @param optimizedDirectory this parameter is deprecated and has no effect since API level 26.
     * @param librarySearchPath  the list of directories containing native
     *                           libraries, delimited by {@code File.pathSeparator}; may be
     *                           {@code null}
     * @param parent             the parent class loader
     */
    public ImClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 自定义加载逻辑（如解密字节码）
        return super.findClass(name);
    }
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 1. 检查是否已加载
        Log.i("loadClass",name);
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                // 2. 优先从当前模块加载
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                // 3. 未找到则委托父加载器
                c = super.loadClass(name, resolve);
            }
        }
        return c;
    }
}
