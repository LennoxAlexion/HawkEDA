package scenario.implementations;

import cep.CEP;
import lombok.extern.slf4j.Slf4j;
import scenario.implementations.entities.BasketItem;
import scenario.interfaces.ScenarioInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static scenario.implementations.EShopHelper.*;

@Slf4j
public class SingleBasketMultipleCheckoutScenario implements ScenarioInterface {

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
