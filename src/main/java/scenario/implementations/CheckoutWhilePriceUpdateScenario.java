package scenario.implementations;

import cep.CEP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.interfaces.ScenarioInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static scenario.implementations.eShopHelper.*;

public class CheckoutWhilePriceUpdateScenario implements ScenarioInterface {
    private static final Logger log = LoggerFactory.getLogger(CheckoutWhilePriceUpdateScenario.class);

    private List<UUID> userIds;

    public static final float oldPrice = 100;

    @Override
    public void registerCEPQueries(CEP cep){
        checkoutWhilePriceUpdateCEP(cep);
    }

    @Override
    public void initScenario() {
        //Initialize price and stock.
        initDefaultItemStock(10000, oldPrice);
        int numberOfUsers = 10;

        userIds = new ArrayList<>();
        for(int i =0; i <numberOfUsers; i++){
            userIds.add(UUID.randomUUID());
        }
        // Add items to basket for all the users.
        for (UUID userId : userIds){
            addDefaultItemToBasket(userId);
        }
    }

    @Override
    public void execute() {
        // Update the price of the product to 250
        initDefaultItemStock(10000, 250);
        checkoutUsersConcurrent(userIds, log);
    }
}