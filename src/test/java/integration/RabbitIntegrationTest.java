package integration;

import org.junit.ClassRule;
import org.junit.Test;
import producer.RabbitProducer;
import rules.RabbitContainerRule;

public class RabbitIntegrationTest {

    @ClassRule
    public static RabbitContainerRule rabbitContainerRule = new RabbitContainerRule();

    @Test
    public void testConnectsToDocker() throws Exception {
        RabbitProducer rabbitProducer = new RabbitProducer(rabbitContainerRule.getDockerHost(), rabbitContainerRule.getRabbitServicePort());
        rabbitProducer.produce();
    }

}