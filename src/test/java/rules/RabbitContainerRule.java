package rules;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.PortBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: moshe
 * Date: 9/29/14
 * Time: 4:02 PM
 */
public class RabbitContainerRule extends DockerContainerRule {
    public static final String RABBIT_CONTAINER_IMAGE_NAME = "rabbitmq:management";
//    public static final String RABBIT_CONTAINER_IMAGE_NAME = "kenshoo/rabbitmq";


    public static final String SERVICE_PORT = "RabbitMQServicePort";
    public static final String MANAGEMENT_PORT = "RabbitMQManagementPort";

    static int servicePort = SocketUtil.findFreePort();
//    static int managementPort = SocketUtil.findFreePort();

//    static String externalServicePort = Integer.toString(SocketUtil.findFreePort());
//    static String externalManagementPort = Integer.toString(SocketUtil.findFreePort());


//    static Map.Entry<String, String> internalPortEntry = new HashMap.SimpleEntry<>(servicePort, externalServicePort);
//    static Map.Entry<String, String> externalPortEntry = new HashMap.SimpleEntry<>(managementPort, externalManagementPort);
//    static Map<String, Map.Entry<String, String>> portEntryMap = ImmutableMap.of(SERVICE_PORT, internalPortEntry, MANAGEMENT_PORT, externalPortEntry);
//    static String[] cmd = new String[] {"-p" + servicePort, "-h" + managementPort} ;

//    static Map<String, List<PortBinding>> portBindings = ;

    public RabbitContainerRule() {
        super(RABBIT_CONTAINER_IMAGE_NAME, servicePort);
    }


    public int getRabbitServicePort() {
        return servicePort;
    }

//    public String getRabbitManagementPort() {
//        return this.getExternalServicePorts().get(MANAGEMENT_PORT);
//    }
}