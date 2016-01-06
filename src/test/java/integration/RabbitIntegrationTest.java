package integration;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.ClassRule;
import org.junit.Test;
import rules.RabbitContainerRule;

public class RabbitIntegrationTest {

  @ClassRule
  public static RabbitContainerRule rabbitContainerRule = new RabbitContainerRule();

  @Test
  public void testConnectsToDocker() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(rabbitContainerRule.getDockerHost());
    factory.setPort(rabbitContainerRule.getRabbitServicePort());
    factory.newConnection();
  }
}
