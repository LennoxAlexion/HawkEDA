package messaging;

import cep.CEP;
import cep.dto.EventDTO;
import com.rabbitmq.client.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.interfaces.ScenarioInterface;
import utilities.LogEvents;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnector {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQConnector.class);

    private static RabbitMQConnector instance = null;
    private String userName;
    private String password;
    private String virtualHost;
    private String hostName;
    private int portNumber;

    private static Connection connection = null;
    private static String queueName;

    private RabbitMQConnector(){
        try (InputStream input = RabbitMQConnector.class.getClassLoader().getResourceAsStream("tool.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            userName = properties.getProperty("rabbitmq.username");
            password = properties.getProperty("rabbitmq.password");
            virtualHost = properties.getProperty("rabbitmq.virtualhost");
            hostName = properties.getProperty("rabbitmq.hostname");
            portNumber = Integer.parseInt(properties.getProperty("rabbitmq.port"));
        } catch (IOException e) {
            e.printStackTrace();
            //Set default connection settings maybe
        }
    }

    public static RabbitMQConnector getInstance() {
        if (instance == null)
            instance = new RabbitMQConnector();

        return instance;
    }

    public void getConnection() throws IOException, TimeoutException {
        if (connection == null) {
            connect();
        }
    }

    private void connect() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setHost(hostName);
        factory.setPort(portNumber);

        connection = factory.newConnection();
        log.info("Successfully connected to RabbitMQ at " + new java.util.Date());
    }

    private void closeConnection(Channel channel) throws IOException, TimeoutException {
//        channel.basicCancel(consumerTag);
        channel.close();
        connection.close();
    }

    // Assign only the events required for CEP processing.
    private Channel setExchangeConfiguration(String exchangeName) throws IOException {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchangeName, "direct", false);
        queueName = channel.queueDeclare().getQueue();  //RabbitMQ assigns and sets a non durable queue.
        //Listen for all the Routing Keys
        // TODO: Add ability to bind events based on scenarios.
        channel.queueBind(queueName, exchangeName, ScenarioInterface.ScenarioInitEvent);
        channel.queueBind(queueName, exchangeName, ScenarioInterface.ScenarioExecuteEvent);
//        channel.queueBind(queueName, exchangeName, "OrderStartedIntegrationEvent");
        channel.queueBind(queueName, exchangeName, "ProductPriceChangedIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStockRejectedIntegrationEvent");
        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToPaidIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToAwaitingValidationIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToSubmittedIntegrationEvent");
        channel.queueBind(queueName, exchangeName, "UserCheckoutAcceptedIntegrationEvent");

//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToAwaitingValidationIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "GracePeriodConfirmedIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderPaymentFailedIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderPaymentSucceededIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStockConfirmedIntegrationEvent");



//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToCancelledIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToShippedIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToStockConfirmedIntegrationEvent");


        log.info("[*] Queue " + queueName + " is bound and listening for messages.");
        return channel;
    }

    private void consumeEvents(Channel channel) throws IOException {
        channel.basicConsume(queueName, true, (DeliverCallback) (consumerTag, deliveryMessage) -> {
            String body = new String(deliveryMessage.getBody());
            String routingKey = deliveryMessage.getEnvelope().getRoutingKey();

//            System.out.println("Rabbit Event: Routing Key = " + routingKey + ", Body = " + body);
            //  Send events to CEP.
            JSONObject bodyJson = new JSONObject(body);
            log.info("Routing Key:" + routingKey + "\nBody JSON of message: " + bodyJson + "\n");
            EventDTO eventDTO = new EventDTO(routingKey, bodyJson.getString("CreationDate"), bodyJson);
            CEP.getInstance().getEPRuntime().getEventService().sendEventBean(eventDTO, "EventDTO");

            LogEvents.writeEventToLog(eventDTO.toJSONObject());

        }, (CancelCallback) consumerTag -> {
//            Handle Cancel scenarios.
        });
    }

    public void listenToeShopEvents() {
        String exchangeName = "eshop_event_bus";
        try {
            instance.getConnection();
            Channel channel = instance.setExchangeConfiguration(exchangeName); //, queueName, routingKey);
            instance.consumeEvents(channel); //, queueName, );
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void publishEvent(String routingKey) {
        String exchangeName = "eshop_event_bus";
        try {
            instance.getConnection();
            Channel channel = connection.createChannel(); //, queueName, routingKey);
            channel.exchangeDeclare(exchangeName, "direct");

            JSONObject message = new JSONObject();
            message.put("CreationDate", Instant.now());

            channel.basicPublish(exchangeName, routingKey, null, message.toString().getBytes(StandardCharsets.UTF_8));

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}