package scenario.implementations;

import cep.CEP;
import cep.result.AllEventsResult;
import cep.result.CheckoutWhilePriceUpdateResult;
import cep.result.ConcurrentCheckoutStmtResult;
import cep.statement.AllEventsStmt;
import cep.statement.CheckoutWhilePriceUpdateStmt;
import cep.statement.ConcurrentCheckoutStmt;
import messaging.RabbitMQConnector;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import scenario.interfaces.ScenarioInterface;
import utilities.HTTPRestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class eShopHelper {
    private static final String CATALOG_API_URL = "http://localhost:5101/api/v1/Catalog/";
    private static final String BASKET_API_URL = "http://localhost:5103/api/v1/Basket/";

    public static void  initDefaultItemStock(int quantity, float price) {
        String requestBodyJSONString = "{\"id\":8,\"name\":\"Kudu Purple Hoodie\",\"description\":\"Kudu Purple Hoodie\",\"price\":"  + price + ",\"pictureFileName\":\"8.png\",\"pictureUri\":\"http://host.docker.internal:5202/c/api/v1/catalog/items/8/pic/\",\"catalogTypeId\":2,\"catalogType\":null,\"catalogBrandId\":5,\"catalogBrand\":null,\"availableStock\":" + quantity + ",\"restockThreshold\":0,\"maxStockThreshold\":0,\"onReorder\":false}";
        HTTPRestHelper.HTTPPut( CATALOG_API_URL + "items", requestBodyJSONString);
    }

    public static void addDefaultItemToBasket(UUID userId){
        //Add items to the basket.
        UUID shoppingCartId = UUID.randomUUID();
        String requestBodyJSONString = "{ \"buyerId\": \""+ userId.toString() + "\", \"items\": [ { \"id\": \""+ shoppingCartId.toString() + "\", \"productId\": 8, \"productName\": \"Kudu Purple Hoodie\", \"unitPrice\": 100, \"oldUnitPrice\": 0, \"quantity\": 1, \"pictureUrl\": \"http://host.docker.internal:5202/c/api/v1/catalog/items/8/pic/\" } ] }";
        HTTPRestHelper.HTTPPost(BASKET_API_URL, requestBodyJSONString, new ArrayList<>());
    }

    public static void checkout(UUID userId){
        //Add checkout basket.
        String requestIdStr = UUID.randomUUID().toString();
        String requestBodyJSONString = "{ \"city\": \"Gotham\", \"street\": \"Mountain Drive\", \"state\": \"NA\", \"country\": \"USA\", \"zipCode\": \"1007\", \"cardNumber\": \"1234567890123456\", \"cardHolderName\": \"Batman\", \"cardExpiration\": \"2022-03-02T00:00:00\", \"cardSecurityNumber\": \"123\", \"cardTypeId\": 1, \"buyer\": \""+ userId.toString() +"\", \"requestId\": \"" + requestIdStr + "\" }";

        List<NameValuePair> headerParams = new ArrayList<>();
        headerParams.add(new BasicNameValuePair("x-requestid", requestIdStr));
        headerParams.add(new BasicNameValuePair("userId", userId.toString()));
        HTTPRestHelper.HTTPPost(BASKET_API_URL + "checkout", requestBodyJSONString, headerParams);
    }

    public static void checkoutUsersConcurrent(List<UUID> userIds, Logger log) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (UUID userId : userIds){
            executorService.execute(() -> {
                log.info("Concurrently checking out user: " + userId);
                RabbitMQConnector.getInstance().publishEvent(ScenarioInterface.ScenarioExecuteEvent);
                checkout(userId);
            });
        }
        executorService.shutdown();
    }

    public static void concurrentCheckoutCEP(CEP cep){
        ConcurrentCheckoutStmt concurrentCheckoutStmt = new ConcurrentCheckoutStmt(cep.getEPRuntime().getDeploymentService(), cep.getCEPConfig());
        ConcurrentCheckoutStmtResult concurrentCheckoutStmtResult = new ConcurrentCheckoutStmtResult();
        concurrentCheckoutStmt.addListener(concurrentCheckoutStmtResult);
    }

    public static void checkoutWhilePriceUpdateCEP(CEP cep){
        CheckoutWhilePriceUpdateStmt checkoutWhilePriceUpdateStmt = new CheckoutWhilePriceUpdateStmt(cep.getEPRuntime().getDeploymentService(), cep.getCEPConfig());
        CheckoutWhilePriceUpdateResult checkoutWhilePriceUpdateResult = new CheckoutWhilePriceUpdateResult();
        checkoutWhilePriceUpdateStmt.addListener(checkoutWhilePriceUpdateResult);
    }

    //TODO:delete
    public static void allEventsCEP(CEP cep){
        AllEventsStmt allEventsStmt = new AllEventsStmt(cep.getEPRuntime().getDeploymentService(), cep.getCEPConfig());
        AllEventsResult allEventsResult = new AllEventsResult();
        allEventsStmt.addListener(allEventsResult);
    }
}
