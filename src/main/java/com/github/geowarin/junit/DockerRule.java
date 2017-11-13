package com.github.geowarin.junit;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.spotify.docker.client.DockerClient.LogsParam.follow;
import static com.spotify.docker.client.DockerClient.LogsParam.stdout;
import static com.spotify.docker.client.DockerClient.RemoveContainerParam.removeVolumes;

/**
 * <p>
 * JUnit rule starting a docker container before the test and killing it
 * afterwards.
 * </p>
 * <p>
 * Uses spotify/docker-client.
 * Adapted from https://gist.github.com/mosheeshel/c427b43c36b256731a0b
 * </p>
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
      if (!params.isLocalImage) {
        dockerClient.pull(params.imageName);
      }
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
      waitForPort(getHostPort(params.portToWaitOn), params.waitTimeout);
    }

    if (params.logToWait != null) {
      waitForLog(params.logToWait);
    }
  }

  @Override
  protected void after() {
    super.after();
    try {
      dockerClient.killContainer(container.id());
      dockerClient.removeContainer(container.id(), removeVolumes());
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

  protected void waitForPort(int port, long timeoutInMillis) {
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

  protected DockerClient createDockerClient() {
    try {
      return DefaultDockerClient.fromEnv().build();
    } catch (DockerCertificateException e) {
      throw new IllegalStateException("Could not create docker client from environement", e);
    }
  }

  protected ContainerConfig createContainerConfig(String imageName, String[] ports, String cmd) {
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

  protected void waitForLog(String messageToMatch) throws DockerException, InterruptedException, UnsupportedEncodingException {
    LogStream logs = dockerClient.logs(container.id(), follow(), stdout());
    String log;
    do {
      LogMessage logMessage = logs.next();
      ByteBuffer buffer = logMessage.content();
      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      log = new String(bytes);
    } while (!log.contains(messageToMatch));
  }
}
