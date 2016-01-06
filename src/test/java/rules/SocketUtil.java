package rules;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by geowarin on 06/01/16.
 */
public class SocketUtil {
    static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return -1;
    }
}
