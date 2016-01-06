package rules;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spotify.docker.client.*;
import com.spotify.docker.client.messages.*;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.URI;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: moshe
 * Date: 9/23/14
 * Time: 11:57 AM
 */
public class DockerContainerRule extends ExternalResource {

    public static final String DOCKER_SERVICE_URL = "http://192.168.99.100:2375";
    private final DockerClient dockerClient;
    private final HostConfig hostConfig;
    private ContainerCreation container;


    public DockerContainerRule(String imageName, int servicePort) {

//        dockerClient = new DefaultDockerClient(dockerServiceUrl);
        DockerCertificates dockerCertificates = null;
        try {
            dockerCertificates = new DockerCertificates(Paths.get("/Users/geowarin/.docker/machine/certs"));
        } catch (DockerCertificateException e) {
            throw new IllegalStateException(e);
        }
        dockerClient = DefaultDockerClient.builder()
                .uri(URI.create("https://192.168.99.100:2376"))
                .dockerCertificates(dockerCertificates)
                .build();

        final String[] ports = {"5672", "61613", "15672"};
//        PortBinding portBinding = PortBinding.randomPort("0.0.0.0");


        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

        hostConfig = HostConfig.builder()
                .portBindings(portBindings)
                .build();

        ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(imageName)
                .networkDisabled(false)
//                .cmd(cmd)
                .exposedPorts(ports)
                .build();

        try {
            dockerClient.pull(imageName);
            container = dockerClient.createContainer(containerConfig);
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isSet(String value) {
        return value != null && !value.equals("");
    }

//    public Map<String, String> getExternalServicePorts() {
//        return externalServicePorts;
//    }

    @Override
    public Statement apply(Statement base, Description description) {
        return super.apply(base, description);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        dockerClient.startContainer(container.id());
        final ContainerInfo info = dockerClient.inspectContainer(container.id());
        System.out.println(info);
    }

    @Override
    protected void after() {
        super.after();
        try {
            dockerClient.killContainer(container.id());
            dockerClient.removeContainer(container.id(), true);
        } catch (DockerException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}