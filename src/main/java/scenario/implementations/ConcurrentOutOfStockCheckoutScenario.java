package scenario.implementations;

import cep.CEP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.interfaces.ScenarioInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static scenario.implementations.eShopHelper.*;

public class ConcurrentOutOfStockCheckoutScenario implements ScenarioInterface {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentOutOfStockCheckoutScenario.class);

    private List<UUID> userIds;

    @Override
    public void registerCEPQueries(CEP cep){
        eShopHelper.concurrentCheckoutCEP(cep);
    }

    @Override
    public void initScenario() {
        //Initialize stock.
        initDefaultItemStock(1, 100);
        int numberOfUsers = 1;

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
        checkoutUsersConcurrent(userIds, log);
    }
}