package scenario.implementations;

import cep.CEP;
import com.yahoo.ycsb.WorkloadException;
import com.yahoo.ycsb.generator.NumberGenerator;
import lombok.extern.slf4j.Slf4j;
import scenario.implementations.entities.BasketItem;
import scenario.implementations.entities.CatalogItem;
import scenario.interfaces.ScenarioInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static scenario.implementations.EShopHelper.*;

@Slf4j
public class OutOfStockCheckoutScenario implements ScenarioInterface {

    private List<UUID> userIds;
    private int numberOfUsers = 0;
    private String distribution = "uniform";
    private int catalogSize = 100;

    @Override
    public void registerCEPQueries(ArrayList<String> args, CEP cep) {
        EShopHelper.concurrentOutOfStockCheckoutCEP(cep);
    }

    @Override
    public void initScenario(ArrayList<String> args) {
        parseArgs(args);

        //Initialize catalog items, price and stock.
        HashMap<Integer, CatalogItem> catalogItems = generateCatalogItem(catalogSize, 1);

        // Create users
        userIds = createUsersFromArgs(numberOfUsers);

        NumberGenerator addItemToCartKeyChooser;
        try {
            addItemToCartKeyChooser = getKeyChooser(distribution, catalogSize, 0.99);
        } catch (WorkloadException e) {
            e.printStackTrace();
            return;
        }

        // add items to basket from the catalog using given distribution.
        for (UUID userId : userIds) {
            UUID basketId = UUID.randomUUID();
            ArrayList<BasketItem> basketItems = generateBasketItemsFromCatalog(basketId,
                    catalogItems, addItemToCartKeyChooser);
            addBasketItemsToBasket(userId, basketItems);
        }
    }

    @Override
    public void execute(ArrayList<String> args) {
        checkoutUsersConcurrent(userIds, log);
    }

    private void parseArgs(ArrayList<String> args){
        // args: [Number of Users: int, distribution = String, catalog size = int]
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
            catalogSize = Integer.parseInt(args.get(2));
        } catch (Exception e) {
            catalogSize = 100;
            e.printStackTrace();
        }
    }
}