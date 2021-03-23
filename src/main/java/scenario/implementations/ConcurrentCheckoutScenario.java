package scenario.implementations;

import cep.CEP;
import messaging.RabbitMQConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.interfaces.ScenarioInterface;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static scenario.implementations.eShopHelper.addDefaultItemToBasket;
import static scenario.implementations.eShopHelper.checkout;

public class ConcurrentCheckoutScenario implements ScenarioInterface {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentCheckoutScenario.class);

    private UUID userId;

    @Override
    public void registerCEPQueries(CEP cep) {
//        eShopHelper.concurrentCheckoutCEP(cep);
        eShopHelper.checkoutWhilePriceUpdateCEP(cep);
    }

    @Override
    public void initScenario() {
        //Make sure we have enough stock.
        eShopHelper.initDefaultItemStock(1000, 100);
        //Add items to the user's basket.
        userId = UUID.randomUUID();
        addDefaultItemToBasket(userId);
    }

    @Override
    public void execute() {
        int numberOfThreads = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                log.info("Concurrently checking out user: " + userId);
                RabbitMQConnector.getInstance().publishEvent(ScenarioInterface.ScenarioExecuteEvent);
                checkout(userId);
            });
        }
        executorService.shutdown();
    }
}
