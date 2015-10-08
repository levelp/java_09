import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Выводим все сетевые карты
 */
public class Interfaces {
    public static void main(String[] args) throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface networkInterface : Collections.list(interfaces)) {
            List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
            for (InetAddress inetAddress : addresses) {
                if (inetAddress instanceof Inet4Address) {
                    System.out.println("IP = " + inetAddress.getHostAddress() +
                            "  isLoopback = " + networkInterface.isLoopback());
                }
            }
        }
    }
}
