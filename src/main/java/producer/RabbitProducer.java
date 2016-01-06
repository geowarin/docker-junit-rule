package producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;

public class RabbitProducer {

    private Channel channel;

    public RabbitProducer(int port) throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.99.100");
//        factory.setPort(port);
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        this.channel.queueDeclare("greetings", false, false, true, null);
    }

    public void produce() throws IOException {
        channel.basicPublish("greetings", "", MessageProperties.PERSISTENT_TEXT_PLAIN, "hello".getBytes());
    }
}
