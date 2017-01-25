package com.github.athenaengine.core.util;

import com.github.athenaengine.core.exceptions.ResourcesLoadException;
import com.github.athenaengine.core.exceptions.InvalidJarLoadException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JarUtils {

    private static final Logger LOGGER = Logger.getLogger(JarUtils.class.getName());

    public static boolean writeFileFromResources(ClassLoader loader, String folderPath, String fileName) {
        try {
            File folder = new File(folderPath);

            if (loader == null) throw new ResourcesLoadException("The class loader is null");

            if (!folder.exists() && !folder.mkdirs()) {
                throw new ResourcesLoadException("The folder for " + fileName + " couldn't be created");
            }

            File existingFile = new File(folderPath + fileName);
            if (existingFile.exists()) return true;

            InputStream is = loader.getResourceAsStream(fileName);

            if (is == null) throw new ResourcesLoadException("The config.conf file for " + fileName + " doens't exist");

            try {
                JarUtils.writeFile(is, folderPath + fileName);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ResourcesLoadException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        return false;
    }

    public static Object loadJar(ClassLoader loader, File jar)
    {
        try {
            String classPath;
            URL classUrl;
            URL[] classUrls;

            if (loader == null) throw new InvalidJarLoadException("The class loader is null");
            if (jar == null) throw new InvalidJarLoadException("The jar file is null");

            try {
                classPath = getMainClassFromJar(jar);
            } catch (IOException ex) {
                throw new InvalidJarLoadException(jar.getName() + " couldn't be loaded");
            }

            try {
                classUrl = jar.toURI().toURL();
                classUrls = new URL[] { classUrl };
            } catch (MalformedURLException ex) {
                throw new InvalidJarLoadException("File URL malformed " + jar.getName());
            }

            URLClassLoader child = new URLClassLoader(classUrls, loader);
            Class classToLoad;

            try {
                classToLoad = Class.forName(classPath, true, child);
            } catch (ClassNotFoundException ex) {
                throw new InvalidJarLoadException("Cannot find main class " + classPath);
            }

            if (!classToLoad.getSuperclass().getSimpleName().equalsIgnoreCase("BaseEventContainer")) {
                throw new InvalidJarLoadException("Wrong inheritance for " + classToLoad.getSimpleName());
            }

            try {
                return classToLoad.newInstance();
            } catch (InstantiationException ex) {
                throw new InvalidJarLoadException("The class " + classToLoad.getSimpleName() + " cannot be instantiated");
            } catch (IllegalAccessException ex) {
                throw new InvalidJarLoadException("The class " + classToLoad.getSimpleName() + " cannot be accessed");
            }
        } catch (InvalidJarLoadException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }

        return null;
    }

    private static String getMainClassFromJar(File jar) throws IOException
    {
        // Open the JAR file
        JarFile jarfile = new JarFile(jar);

        // Get the manifest
        Manifest manifest = jarfile.getManifest();

        // Get the main attributes in the manifest
        Attributes attrs = manifest.getMainAttributes();

        // Enumerate each attribute
        for (Iterator it = attrs.keySet().iterator(); it.hasNext(); ) {
            // Get attribute name
            Attributes.Name attrName = (Attributes.Name)it.next();

            if (attrName.toString().equalsIgnoreCase("Main-Class")) {
                return attrs.getValue(attrName);
            }
        }
        return null;
    }

    private static void writeFile(InputStream is, String path) throws IOException {
        OutputStream os = new FileOutputStream(path);

        byte[] buffer = new byte[is.available()];

        int bytesRead;
        //read from is to buffer
        while((bytesRead = is.read(buffer)) !=-1){
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        //flush OutputStream to write any buffered data to file
        os.flush();
        os.close();
    }
}
