package pak;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

public class Version {
    private static final ReentrantLock PROPERTIES_LOCK = new ReentrantLock();
    private static Properties properties;
    private static Properties getProperties() {
        PROPERTIES_LOCK.lock();
        try {
            if (properties == null) {
                properties = new Properties();
                try {
                    properties.load(Version.class.getClassLoader().getResourceAsStream("application.properties"));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read application.properties!", e);
                }
            }
        } finally {
            PROPERTIES_LOCK.unlock();
        }

        return properties;
    }

    public static String getVersion() {
        return getProperties().getProperty("app.version", "N/A");
    }

    public static String getFullVersion() {
        return getProperties().getProperty("app.version.full", "N/A");
    }
}
