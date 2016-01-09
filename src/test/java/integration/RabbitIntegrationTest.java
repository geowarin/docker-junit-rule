package integration;

import com.github.geowarin.junit.DockerRule;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.ClassRule;
import org.junit.Test;

public class RabbitIntegrationTest {

  @ClassRule
  public static DockerRule rabbitContainerRule =
    DockerRule.builder()
      .image("rabbitmq:management")
      .ports("5672")
//      .waitForPort("5672/tcp")
      .waitForLog("Server startup complete")
      .build();

  @Test
  public void testConnectsToDocker() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitContainerRule.getDockerHost());
    factory.setPort(rabbitContainerRule.getHostPort("5672/tcp"));
    factory.newConnection();
  }
}
