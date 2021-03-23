package messaging;

import cep.CEP;
import cep.dto.EventDTO;
import com.rabbitmq.client.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.implementations.CheckoutWhilePriceUpdateScenario;
import scenario.interfaces.ScenarioInterface;
import utilities.LogEvents;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnector {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQConnector.class);

    private static RabbitMQConnector instance = null;
    // TODO: Move this to properties file.
    final private static String userName = "guest";
    final private static String password = "guest";
    final private static String virtualHost = "/";
    final private static String hostName = "localhost";
    final private static int portNumber = 5672;

    private static Connection connection = null;
    private static String queueName;

    private RabbitMQConnector() {
    }

    public static RabbitMQConnector getInstance() {
        if (instance == null)
            instance = new RabbitMQConnector();

        return instance;
    }

    public Connection getConnection() throws IOException, TimeoutException {
        if(connection == null){
            connect();
        }
        return connection;
    }

    private void connect() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setHost(hostName);
        factory.setPort(portNumber);

        connection = factory.newConnection();
        System.out.println("Successfully connected to RabbitMQ at " + new java.util.Date());
    }

    private void closeConnection(Channel channel) throws IOException, TimeoutException {
//        channel.basicCancel(consumerTag);
        channel.close();
        connection.close();
    }

    private Channel setExchangeConfiguration(String exchangeName) throws IOException {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchangeName, "direct", false);
        queueName = channel.queueDeclare().getQueue();  //RabbitMQ assigns and sets a non durable queue.
        //Listen for all the Routing Keys
        channel.queueBind(queueName, exchangeName, "OrderStartedIntegrationEvent");
        channel.queueBind(queueName, exchangeName, "ProductPriceChangedIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToAwaitingValidationIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "GracePeriodConfirmedIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderPaymentFailedIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderPaymentSucceededIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStockConfirmedIntegrationEvent");
        channel.queueBind(queueName, exchangeName, "OrderStockRejectedIntegrationEvent");
        channel.queueBind(queueName, exchangeName, "UserCheckoutAcceptedIntegrationEvent");
        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToPaidIntegrationEvent");
        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToAwaitingValidationIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToCancelledIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToShippedIntegrationEvent");
//        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToStockConfirmedIntegrationEvent");
        channel.queueBind(queueName, exchangeName, "OrderStatusChangedToSubmittedIntegrationEvent");

        channel.queueBind(queueName, exchangeName, ScenarioInterface.ScenarioInitEvent);
        channel.queueBind(queueName, exchangeName, ScenarioInterface.ScenarioExecuteEvent);

        System.out.println("[*] Queue " + queueName + " is bound and listening for messages.");
        return channel;
    }

    private void consumeEvents(Channel channel) throws IOException {
        channel.basicConsume(queueName, true, (DeliverCallback) (consumerTag, deliveryMessage) -> {
            String body = new String(deliveryMessage.getBody());
            String routingKey = deliveryMessage.getEnvelope().getRoutingKey();

//            System.out.println("Rabbit Event: Routing Key = " + routingKey + ", Body = " + body);
            //  Send events to CEP.
            JSONObject bodyJson = new JSONObject(body);
            log.info("Routing Key:" + routingKey + "\nBody JSON of message: " + bodyJson.toString() + "\n");
            EventDTO eventDTO = new EventDTO(routingKey, bodyJson.getString("CreationDate"), bodyJson);
            CEP.getInstance().getEPRuntime().getEventService().sendEventBean(eventDTO, "EventDTO");

            LogEvents.appendToJsonArray(eventDTO.toJSONObject());

        }, (CancelCallback) consumerTag -> {
//            Handle Cancel scenarios.
        });
    }

    public void listenToeShopEvents(){
        String exchangeName = "eshop_event_bus";
        try {
            instance.getConnection();
            Channel channel = instance.setExchangeConfiguration(exchangeName); //, queueName, routingKey);
            instance.consumeEvents(channel); //, queueName, );
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void publishEvent(String routingKey){
        String exchangeName = "eshop_event_bus";
        try {
            instance.getConnection();
            Channel channel =  connection.createChannel(); //, queueName, routingKey);
            channel.exchangeDeclare(exchangeName, "direct");

            JSONObject message = new JSONObject();
            message.put("CreationDate", Instant.now());

            channel.basicPublish(exchangeName, routingKey, null, message.toString().getBytes(StandardCharsets.UTF_8));

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}


// get these info from rabbitmq http api:
//    ExchangeName = "eshop_event_bus"
//    QueueName RoutingKey
//    Basket OrderStartedIntegrationEvent
//    Basket ProductPriceChangedIntegrationEvent
//    Catalog OrderStatusChangedToAwaitingValidationIntegrationEvent
//    Catalog OrderStatusChangedToPaidIntegrationEvent
//    Ordering GracePeriodConfirmedIntegrationEvent
//    Ordering OrderPaymentFailedIntegrationEvent
//    Ordering OrderPaymentSucceededIntegrationEvent
//    Ordering OrderStockConfirmedIntegrationEvent
//    Ordering OrderStockRejectedIntegrationEvent
//    Ordering UserCheckoutAcceptedIntegrationEvent