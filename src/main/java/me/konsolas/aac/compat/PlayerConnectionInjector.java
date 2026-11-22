package me.konsolas.aac.compat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bukkit.Bukkit;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class PlayerConnectionInjector implements Runnable {
   @Override
   public void run() {
      Class<?> serverClass = Bukkit.getServer().getClass();
      String packageName = serverClass.getPackage().getName();
      String nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
      Bukkit.getLogger().info("[AAC] Attempting to inject into PlayerConnection");

      try (ZipFile serverJar = new ZipFile(new File(serverClass.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
         Enumeration<? extends ZipEntry> entries = serverJar.entries();
         boolean found = false;

         while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().endsWith("PlayerConnection.class")) {
               found = true;
               ClassWriter writer = new ClassWriter(0);
               PlayerConnectionVisitor visitor = new PlayerConnectionVisitor(writer, nmsVersion);
               try (InputStream in = serverJar.getInputStream(entry)) {
                  new ClassReader(in).accept(visitor, 0);
               }
               if (!visitor.wasPatched()) {
                  throw new RuntimeException("Couldn't inject packet handler in PlayerConnection.class");
               }
               defineClass(writer.toByteArray());
            }
         }

         if (!found) {
            throw new IllegalStateException("Couldn't find PlayerConnection.class");
         }
      } catch (IOException | URISyntaxException e) {
         throw new RuntimeException(e);
      }

      Bukkit.getLogger().info("[AAC] Successfully injected.");
   }

   public static void defineClass(byte[] bytecode) {
      try {
         ClassLoader classLoader = Bukkit.class.getClassLoader();
         Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
         method.setAccessible(true);
         method.invoke(classLoader, null, bytecode, 0, bytecode.length);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
         throw new RuntimeException(e);
      }
   }
}
