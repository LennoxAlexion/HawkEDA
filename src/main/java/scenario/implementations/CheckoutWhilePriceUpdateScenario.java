package scenario.implementations;

import cep.CEP;
import com.yahoo.ycsb.WorkloadException;
import com.yahoo.ycsb.generator.NumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.implementations.entities.BasketItem;
import scenario.implementations.entities.CatalogItem;
import scenario.interfaces.ScenarioInterface;

import java.util.*;

import static scenario.implementations.EShopHelper.*;

public class CheckoutWhilePriceUpdateScenario implements ScenarioInterface {
    private static final Logger log = LoggerFactory.getLogger(CheckoutWhilePriceUpdateScenario.class);

    private List<UUID> userIds;
    private int numberOfUsers = 0;
    private String distribution = "uniform";
    private float itemUpdateProportion = 0;
    private HashMap<Integer, CatalogItem> catalogItems;
    private final int catalogSize = 50;

    @Override
    public void registerCEPQueries(ArrayList<String> args, CEP cep) {
        checkoutWhilePriceUpdateCEP(cep);
    }

    @Override
    public void initScenario(ArrayList<String> args) {
        parseArgs(args);

        //Initialize catalog items, price and stock.
        catalogItems = generateCatalogItem(catalogSize);

        // Create users
        userIds = createUsersFromArgs(numberOfUsers);

        NumberGenerator addItemToCartKeyChooser;
        try {
            addItemToCartKeyChooser = getKeyChooser(distribution, catalogSize, 0.99);
        } catch (WorkloadException e) {
            e.printStackTrace();
            return;
        }

        // add items to basket from the catalog item using given distribution.
        for (UUID userId : userIds) {
            UUID basketId = UUID.randomUUID();
            ArrayList<BasketItem> basketItems = generateBasketItemsFromCatalog(basketId,
                    catalogItems, addItemToCartKeyChooser);
            addBasketItemsToBasket(userId, basketItems);
        }
    }

    @Override
    public void execute(ArrayList<String> args) {
        updateItemPricesConcurrently();
        checkoutUsersConcurrent(userIds, log);
    }

    // Update the price of some products using given distribution.
    private void updateItemPricesConcurrently() {
        try {
//            ExecutorService executorService = Executors.newFixedThreadPool(10);
            NumberGenerator itemToUpdateKeyChooser = getKeyChooser(distribution, catalogSize, 0.99);

            int updateLimit;
            if (distribution.equals("stress")) {
                updateLimit = 1;
            } else {
                updateLimit = (int) (itemUpdateProportion * catalogSize / 100);
            }

            for (int i = 0; i < updateLimit; i++) {
                int itemId = Integer.parseInt(itemToUpdateKeyChooser.nextString()) + 1;
                CatalogItem catalogItem = catalogItems.get(itemId);
                catalogItem.availableStock = 9999;
                catalogItem.price += 10;
                catalogItems.put(itemId, catalogItem);

                log.info("Updating item price : " + itemId);
                updateCatalogItem(catalogItem.id, catalogItem.name, catalogItem.availableStock, catalogItem.price);
            }


        } catch (WorkloadException e) {
            e.printStackTrace();
        }
    }

    private void parseArgs(ArrayList<String> args) {
        // args= [number of users: int, distribution = String, Update proportion of items: int (max 100)]
        try {
            numberOfUsers = Integer.parseInt(args.get(0));
        } catch (Exception e) {
            numberOfUsers = 0;
            e.printStackTrace();
        }
        try {
            distribution = args.get(1);
        } catch (Exception e) {
            distribution = "uniform";
            e.printStackTrace();
        }
        try {
            itemUpdateProportion = Float.parseFloat(args.get(2));
            if (itemUpdateProportion > 100) {
                itemUpdateProportion = 100;
            } else if (itemUpdateProportion < 0) {
                itemUpdateProportion = 0;
            }
        } catch (Exception e) {
            itemUpdateProportion = 0;
            e.printStackTrace();
        }
    }
}