package webapp.storage;

/**
 * User: gkislin
 * Date: 18.04.2014
 */
public class SerializeStorageTest extends StorageTest {
    static {
        storage = new SerializeStorage("./file_storage");
    }
}
