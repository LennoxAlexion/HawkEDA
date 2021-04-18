package scenario.implementations;

import cep.CEP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.interfaces.ScenarioInterface;
import scenario.implementations.entities.BasketItem;
import scenario.implementations.entities.CatalogItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static scenario.implementations.EShopHelper.*;

public class CheckoutWhilePriceUpdateScenario implements ScenarioInterface {
    private static final Logger log = LoggerFactory.getLogger(CheckoutWhilePriceUpdateScenario.class);

    private List<UUID> userIds;

    @Override
    public void registerCEPQueries(ArrayList<String> args, CEP cep){
        checkoutWhilePriceUpdateCEP(cep);
    }

    @Override
    public void initScenario(ArrayList<String> args) {
        //Initialize price and stock.
        addCatalogItem(DEFAULT_PRODUCT_ID, DEFAULT_PRODUCT_NAME, 10000, DEFAULT_PRODUCT_PRICE);
        ArrayList<CatalogItem> catalogItems = generateCatalogItem(200);

        // Create users
        userIds = createUsersFromArgs(args);

        // add the default product to all the users and add other generated items to basket reasonably from the catalog item.
        for (UUID userId : userIds){
            UUID basketId = UUID.randomUUID();
            ArrayList<BasketItem> basketItems = generateBasketItemsFromCatalog(basketId, catalogItems, 10);
            BasketItem defaultBasketItem = new BasketItem(basketId, DEFAULT_PRODUCT_ID, DEFAULT_PRODUCT_NAME, DEFAULT_PRODUCT_PRICE, 1);
            basketItems.add(defaultBasketItem);
            addBasketItemsToBasket(userId, basketItems);
        }
    }

    @Override
    public void execute(ArrayList<String> args) {
        // Update the price of the product to 250
        updateCatalogItem(DEFAULT_PRODUCT_ID, DEFAULT_PRODUCT_NAME, 10000, 115);
        checkoutUsersConcurrent(userIds, log);
    }
}