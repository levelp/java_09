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
        String webappRoot = System.getenv("WEBAPP_ROOT");
        if (webappRoot == null) {
            try {
                webappRoot = new File("webapp").getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (webappRoot == null) {
            throw new IllegalStateException("Define environment variable WEBAPP_ROOT");
        }
        File webappRootDir = new File(webappRoot);
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

    public static IStorage getStorage() {
        return SQL_STORAGE;
    }
}
