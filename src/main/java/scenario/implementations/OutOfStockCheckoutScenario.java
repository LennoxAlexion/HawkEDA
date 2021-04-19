package scenario.implementations;

import cep.CEP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.interfaces.ScenarioInterface;
import scenario.implementations.entities.BasketItem;
import scenario.implementations.entities.CatalogItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static scenario.implementations.EShopHelper.*;

public class OutOfStockCheckoutScenario implements ScenarioInterface {
    private static final Logger log = LoggerFactory.getLogger(OutOfStockCheckoutScenario.class);

    private List<UUID> userIds;
    private int numberOfUsers = 0;

    @Override
    public void registerCEPQueries(ArrayList<String> args, CEP cep) {
        EShopHelper.concurrentCheckoutCEP(cep);
    }

    @Override
    public void initScenario(ArrayList<String> args) {
        parseArgs(args);
        //Initialize stock.
        addCatalogItem(DEFAULT_PRODUCT_ID, DEFAULT_PRODUCT_NAME, 1, DEFAULT_PRODUCT_PRICE);
        HashMap<Integer, CatalogItem> catalogItems = generateCatalogItem(20);

        // Create users
        userIds = createUsersFromArgs(numberOfUsers);

        // add the default product to all the users and add other generated items to basket reasonably from the catalog item.
        for (UUID userId : userIds) {
            UUID basketId = UUID.randomUUID();
            ArrayList<BasketItem> basketItems = generateBasketItemsFromCatalog(basketId, catalogItems, 10);
            BasketItem defaultBasketItem = new BasketItem(basketId, DEFAULT_PRODUCT_ID, DEFAULT_PRODUCT_NAME, DEFAULT_PRODUCT_PRICE, 1);
            basketItems.add(defaultBasketItem);
            addBasketItemsToBasket(userId, basketItems);
        }
    }

    @Override
    public void execute(ArrayList<String> args) {
        checkoutUsersConcurrent(userIds, log);
    }

    private void parseArgs(ArrayList<String> args){
        // args: [Number of Users: Integer]
        try {
            numberOfUsers = Integer.parseInt(args.get(0));
        } catch (Exception e) {
            numberOfUsers = 0;
            e.printStackTrace();
        }
    }
}