package dev.lone.scratchit.nms;

import lonelibs.dev.lone.fastnbt.nms.Version;
import org.bukkit.Bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NMS
{
    @SuppressWarnings("unchecked")
    static <T> T get(Class<T> abstractNmsImpl, Logger logger)
    {
        // This is the only way I found to avoid cyclic dependency in Maven.
        T instance;
        try
        {
            Class<?> clazz = findClass("dev.lone.scratchit.nms." + abstractNmsImpl.getSimpleName() + "_" + Version.get().name());
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            instance = (T) constructor.newInstance();
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, "Please check if the version of the plugin is compatible with the server version.");
            Bukkit.getServer().shutdown();
            throw new RuntimeException(e);
        }

        return instance;
    }

    private static Class<?> findClass(String name) throws ClassNotFoundException
    {
        return Class.forName(name);
    }

    public static <T> MethodHandle constructor(Class<T> clazz, Class<?>... parameterTypes)
    {
        try
        {
            MethodType constructorType = MethodType.methodType(void.class, parameterTypes);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            return MethodHandles.privateLookupIn(clazz, lookup).findConstructor(clazz, constructorType);
        }
        catch (Throwable throwable)
        {
            throw new RuntimeException("Failed to find constructor.", throwable);
        }
    }
}
