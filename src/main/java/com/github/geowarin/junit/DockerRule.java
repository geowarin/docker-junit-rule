package com.github.geowarin.junit;

import com.spotify.docker.client.*;
import com.spotify.docker.client.messages.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JUnit rule starting a docker container before the test and killing it
 * afterwards.
 * <p>
 * <p>
 * Uses spotify/docker-client.
 * Adapted from https://gist.github.com/mosheeshel/c427b43c36b256731a0b
 * </p>
 * <p>
 * author: Geoffroy Warin (geowarin.github.io)
 */
public class DockerRule extends ExternalResource {
  protected final Log logger = LogFactory.getLog(getClass());
  public static final String DOCKER_MACHINE_SERVICE_URL = "https://192.168.99.100:2376";

  private final DockerClient dockerClient;
  private ContainerCreation container;
  private Map<String, List<PortBinding>> ports;
  private DockerRuleParams params;

  public static DockerRuleBuilder builder() {
    return new DockerRuleBuilder();
  }

  DockerRule(DockerRuleParams params) {
    this.params = params;
    dockerClient = createDockerClient();
    ContainerConfig containerConfig = createContainerConfig(params.imageName, params.ports, params.cmd);

    try {
      dockerClient.pull(params.imageName);
      container = dockerClient.createContainer(containerConfig);
    } catch (DockerException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    dockerClient.startContainer(container.id());
    ContainerInfo info = dockerClient.inspectContainer(container.id());
    ports = info.networkSettings().ports();

    if (params.portToWaitOn != null) {
      waitForPort(getHostPort(params.portToWaitOn));
    }
  }

  @Override
  protected void after() {
    super.after();
    try {
      dockerClient.killContainer(container.id());
      dockerClient.removeContainer(container.id(), true);
      dockerClient.close();
    } catch (DockerException | InterruptedException e) {
      throw new RuntimeException("Unable to stop/remove docker container " + container.id(), e);
    }
  }

  /**
   * Utility method to get the docker host.
   * Can be different from localhost if using docker-machine
   *
   * @return The current docker host
   */
  public String getDockerHost() {
    return dockerClient.getHost();
  }

  /**
   * Utility method to ensure a container is started
   *
   * @param port The port to wait on
   */
  public void waitForPort(int port) {
    waitForPort(port, 10000);
  }

  /**
   * Utility method to ensure a container is started
   *
   * @param port            The port to wait on
   * @param timeoutInMillis Maximum waiting time in milliseconds
   */
  public void waitForPort(int port, long timeoutInMillis) {
    SocketAddress address = new InetSocketAddress(getDockerHost(), port);
    long totalWait = 0;
    while (true) {
      try {
        SocketChannel.open(address);
        return;
      } catch (IOException e) {
        try {
          Thread.sleep(100);
          totalWait += 100;
          if (totalWait > timeoutInMillis) {
            throw new IllegalStateException("Timeout while waiting for port " + port);
          }
        } catch (InterruptedException ie) {
          throw new IllegalStateException(ie);
        }
      }
    }
  }

  private DockerClient createDockerClient() {
    if (isUnix() || System.getenv("DOCKER_HOST") != null) {
      try {
        return DefaultDockerClient.fromEnv().build();
      } catch (DockerCertificateException e) {
        System.err.println(e.getMessage());
      }
    }

    logger.info("Could not create docker client from the environment. Assuming docker-machine environment with url " + DOCKER_MACHINE_SERVICE_URL);
    DockerCertificates dockerCertificates = null;
    try {
      String userHome = System.getProperty("user.home");
      dockerCertificates = new DockerCertificates(Paths.get(userHome, ".docker/machine/certs"));
    } catch (DockerCertificateException e) {
      System.err.println(e.getMessage());
    }
    return DefaultDockerClient.builder()
      .uri(URI.create(DOCKER_MACHINE_SERVICE_URL))
      .dockerCertificates(dockerCertificates)
      .build();
  }

  private ContainerConfig createContainerConfig(String imageName, String[] ports, String cmd) {
    Map<String, List<PortBinding>> portBindings = new HashMap<>();
    for (String port : ports) {
      List<PortBinding> hostPorts = Collections.singletonList(PortBinding.randomPort("0.0.0.0"));
      portBindings.put(port, hostPorts);
    }

    HostConfig hostConfig = HostConfig.builder()
      .portBindings(portBindings)
      .build();

    ContainerConfig.Builder configBuilder = ContainerConfig.builder()
      .hostConfig(hostConfig)
      .image(imageName)
      .networkDisabled(false)
      .exposedPorts(ports);

    if (cmd != null) {
      configBuilder = configBuilder.cmd(cmd);
    }
    return configBuilder.build();
  }

  public int getHostPort(String containerPort) {
    List<PortBinding> portBindings = ports.get(containerPort);
    if (portBindings.isEmpty()) {
      return -1;
    }
    return Integer.parseInt(portBindings.get(0).hostPort());
  }

  private static boolean isUnix() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("nix") || os.contains("nux") || os.contains("aix");
  }

  protected void waitForLog(String messageToMatch) throws DockerException, InterruptedException {
    LogStream logs = dockerClient.logs(container.id(), DockerClient.LogsParam.follow());
    String log = "";
    do {
      LogMessage logMessage = logs.next();
      log = logMessage.content().asCharBuffer().toString();
      System.out.println(log);
    } while (!log.contains(messageToMatch));
  }
}
