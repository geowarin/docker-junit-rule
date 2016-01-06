package integration;

import org.junit.*;
import org.junit.rules.ExpectedException;
import producer.RabbitProducer;
import rules.RabbitContainerRule;

public class RabbitIntegrationTest {

    @ClassRule
    public static RabbitContainerRule rabbitContainerRule = new RabbitContainerRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        System.out.println("servicePort " + rabbitContainerRule.getRabbitServicePort());
//        System.out.println("managementPort " + rabbitContainerRule.getRabbitManagementPort());
    }


    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConnectsToDocker() throws Exception {
        RabbitProducer rabbitProducer = new RabbitProducer(rabbitContainerRule.getRabbitServicePort());
        rabbitProducer.produce();
    }

}