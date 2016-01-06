package rules;

public class RabbitContainerRule extends DockerContainerRule {
    public static final String RABBIT_CONTAINER_IMAGE_NAME = "rabbitmq:management";

    public RabbitContainerRule() {
        super(RABBIT_CONTAINER_IMAGE_NAME, new String[]{"5672", "61613", "15672"});
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        // wait for container to boot
        Thread.sleep(2000);
    }

    public int getRabbitServicePort() {
        return getHostPort("5672/tcp");
    }

    public int getRabbitManagementPort() {
        return getHostPort("15672/tcp");
    }
}