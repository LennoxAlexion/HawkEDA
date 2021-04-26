package scenario.implementations;

import cep.CEP;
import messaging.RabbitMQConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.interfaces.ScenarioInterface;
import scenario.implementations.entities.BasketItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static scenario.implementations.EShopHelper.*;

public class SingleBasketMultipleCheckoutScenario implements ScenarioInterface {
    private static final Logger log = LoggerFactory.getLogger(SingleBasketMultipleCheckoutScenario.class);

    private UUID userId;

    @Override
    public void registerCEPQueries(ArrayList<String> args, CEP cep) {
        concurrentCheckoutCEP(cep);
    }

    @Override
    public void initScenario(ArrayList<String> args) {
        //Make sure we have enough stock.
        addCatalogItem(DEFAULT_PRODUCT_ID, DEFAULT_PRODUCT_NAME, DEFAULT_PRODUCT_QUANTITY, DEFAULT_PRODUCT_PRICE);

        //Add items to the user's basket.
        ArrayList<BasketItem> basketItems = new ArrayList<>();
        userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        basketItems.add(new BasketItem(basketId, DEFAULT_PRODUCT_ID, DEFAULT_PRODUCT_NAME, DEFAULT_PRODUCT_PRICE, 1));
        addBasketItemsToBasket(userId, basketItems);
    }

    @Override
    public void execute(ArrayList<String> args) {
        int numberOfCheckouts = 0;
        if (args.size() > 0) {
            numberOfCheckouts = Integer.parseInt(args.get(0));
        }
        List<UUID> userIdToCheckout = new ArrayList<>();
        for (int i = 0; i < numberOfCheckouts; i++) {
            userIdToCheckout.add(userId);
        }
        checkoutUsersConcurrent(userIdToCheckout, log);
    }
}
