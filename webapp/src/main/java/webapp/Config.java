package webapp;

import webapp.storage.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.LogManager;

/**
 * Настройки проекта
 */
public class Config {
    public static final String DIR_STORAGE;
    public static final String DB_URL, DB_USER, DB_PASSWORD;
    public static final IStorage SQL_STORAGE;
    public static final IStorage DS_STORAGE;
    public static final IStorage SER_STORAGE;
    public static final IStorage XML_STORAGE;

    static {
        File webappRootDir = findWebappRootDir();
        Properties props = new Properties();
        try (FileInputStream webappProps = new FileInputStream(new File(webappRootDir, "config/webapp.properties"));
             FileInputStream logProps = new FileInputStream(new File(webappRootDir, "config/logging.properties"))) {

            LogManager.getLogManager().readConfiguration(logProps);

            props.load(webappProps);
            DIR_STORAGE = props.getProperty("dir.storage");
            DB_URL = props.getProperty("db.url");
            DB_USER = props.getProperty("db.user");
            DB_PASSWORD = props.getProperty("db.password");

            // Initialize storage instances after DIR_STORAGE is set
            SQL_STORAGE = new SqlStorage();
            DS_STORAGE = new DataStreamStorage(DIR_STORAGE);
            SER_STORAGE = new SerializeStorage(DIR_STORAGE);
            XML_STORAGE = new XmlStorage(DIR_STORAGE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    private static File findWebappRootDir() {
        // Try multiple approaches to find webapp root directory
        File currDir = new File(".");

        // Strategy 1: Look for webapp directory in current or parent directories
        File tempDir = currDir;
        while (tempDir != null && !new File(tempDir, "webapp").exists()) {
            tempDir = tempDir.getParentFile();
        }
        if (tempDir != null) {
            return new File(tempDir, "webapp");
        }

        // Strategy 2: Check if we're already in webapp directory
        if (new File(currDir, "config/webapp.properties").exists()) {
            return currDir;
        }

        // Strategy 3: Check if we're in the project root
        if (new File(currDir, "webapp/config/webapp.properties").exists()) {
            return new File(currDir, "webapp");
        }

        throw new IllegalStateException("Cannot find webapp root directory");
    }

    public static IStorage getStorage() {
        return SQL_STORAGE;
    }
}
