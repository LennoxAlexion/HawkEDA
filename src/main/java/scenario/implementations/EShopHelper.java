package scenario.implementations;

import cep.CEP;
import cep.result.AllEventsResult;
import cep.result.CheckoutWhilePriceUpdateResult;
import cep.result.ConcurrentCheckoutStmtResult;
import cep.statement.AllEventsStmt;
import cep.statement.CheckoutWhilePriceUpdateStmt;
import cep.statement.ConcurrentCheckoutStmt;
import com.github.javafaker.Faker;
import messaging.RabbitMQConnector;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import scenario.implementations.entities.Basket;
import scenario.implementations.entities.BasketItem;
import scenario.implementations.entities.CatalogItem;
import scenario.implementations.entities.UserDetail;
import scenario.interfaces.ScenarioInterface;
import utilities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EShopHelper {

    private static final String CATALOG_API_URL = "http://localhost:5101/api/v1/Catalog/";
    private static final String BASKET_API_URL = "http://localhost:5103/api/v1/Basket/";
    public static final int DEFAULT_PRODUCT_ID = 8;
    public static final String DEFAULT_PRODUCT_NAME = "Default Product";
    public static final float DEFAULT_PRODUCT_PRICE = 100;
    public static final int DEFAULT_PRODUCT_QUANTITY = 1000;

//    private static HashMap<UUID, UserDetails> userDetails;

    public static void addCatalogItem(int productId, String productName, int quantity, float price) {
        // Add/replace the catalog item based on productId
        CatalogItem catalogItem = new CatalogItem(productId, productName, quantity, price);
        JSONObject requestBody = catalogItem.toJSON();
        // String requestBodyJSONString = "{\"id\":8,\"name\":\"Kudu Purple Hoodie\",\"description\":\"Kudu Purple Hoodie\",\"price\":" + price + ",\"pictureFileName\":\"8.png\",\"pictureUri\":\"http://host.docker.internal:5202/c/api/v1/catalog/items/8/pic/\",\"catalogTypeId\":2,\"catalogType\":null,\"catalogBrandId\":5,\"catalogBrand\":null,\"availableStock\":" + quantity + ",\"restockThreshold\":0,\"maxStockThreshold\":0,\"onReorder\":false}";
        // HTTPRestHelper.HTTPPut(CATALOG_API_URL + "items", requestBody.toString());
//        System.out.println("updateCatalogItem request body: " + requestBody.toString());
        HTTPRestHelper.HTTPPost(CATALOG_API_URL + "items", requestBody.toString(), new ArrayList<>());
    }

    public static void updateCatalogItem(int productId, String productName, int quantity, float price) {
        // Update catalog item using PUT request.
        CatalogItem catalogItem = new CatalogItem(productId, productName, quantity, price);
        JSONObject requestBody = catalogItem.toJSON();
        HTTPRestHelper.HTTPPut(CATALOG_API_URL + "items", requestBody.toString());
    }

    public static void addBasketItemsToBasket(UUID userId, ArrayList<BasketItem> basketItems) {
        Basket basket = new Basket();
        basket.setBuyerId(userId);
        basket.setItems(basketItems);
//        System.out.println("addBasketItemsToBasket request body: " + basket.toJSON().toString());
        HTTPRestHelper.HTTPPost(BASKET_API_URL, basket.toJSON().toString(), new ArrayList<>());
    }

    public static ArrayList<CatalogItem> generateCatalogItem(int catalogSize) {
        ArrayList<CatalogItem> catalogItems = new ArrayList<>();
        int itemId = 5000;
        for (int i = 0; i < catalogSize; i++) {
            Faker faker = new Faker();
            itemId += 1;
            String productName = faker.commerce().productName();
            float price = faker.number().numberBetween(10, 500);
            CatalogItem catalogItem = new CatalogItem(itemId, productName, 10000, price);
            catalogItems.add(catalogItem);
            addCatalogItem(itemId, productName, 10000, price);
        }
        return catalogItems;
    }

    public static ArrayList<BasketItem> generateBasketItemsFromCatalog(UUID basketId, ArrayList<CatalogItem> catalogItems, int maxBasketSize) {
        //Generate basket items with varying size and different catalog items in it.
        ArrayList<BasketItem> basketItems = new ArrayList<>();
        int basketLimit = Math.min(catalogItems.size(), maxBasketSize);
        Faker faker = new Faker();
        // TODO: add unique items only. Use hash set.
        for (int i = 0; i < faker.number().numberBetween(0, basketLimit); i++) {
            int catalogIndex = faker.number().numberBetween(0, catalogItems.size());
            basketItems.add(new BasketItem(basketId,
                    catalogItems.get(catalogIndex).id,
                    catalogItems.get(catalogIndex).name,
                    catalogItems.get(catalogIndex).price,
                    faker.number().numberBetween(1, 10)));
        }
        return basketItems;
    }

    public static void checkout(UUID userId) {
        //Add user details and checkout the basket for the user.
        String requestIdStr = UUID.randomUUID().toString();
        //        String requestBodyJSONString = "{ \"city\": \"Gotham\", \"street\": \"Mountain Drive\", \"state\": \"NA\", \"country\": \"USA\", \"zipCode\": \"1007\", \"cardNumber\": \"1234567890123456\", \"cardHolderName\": \"Batman\", \"cardExpiration\": \"2022-03-02T00:00:00\", \"cardSecurityNumber\": \"123\", \"cardTypeId\": 1, \"buyer\": \""+ userId.toString() +"\", \"requestId\": \"" + requestIdStr + "\" }";

        UserDetail userDetails = new UserDetail(userId);
        JSONObject requestBody = userDetails.toJSON();
        requestBody.put("requestId", requestIdStr);

        List<NameValuePair> headerParams = new ArrayList<>();
        headerParams.add(new BasicNameValuePair("x-requestid", requestIdStr));
        headerParams.add(new BasicNameValuePair("userId", userId.toString()));
        HTTPRestHelper.HTTPPost(BASKET_API_URL + "checkout", requestBody.toString(), headerParams);
    }

    public static void checkoutUsersConcurrent(List<UUID> userIds, Logger log) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (UUID userId : userIds) {
            executorService.execute(() -> {
                log.info("Concurrently checking out user: " + userId);
                RabbitMQConnector.getInstance().publishEvent(ScenarioInterface.ScenarioExecuteEvent);
                checkout(userId);
            });
        }
        executorService.shutdown();
    }

    public static void concurrentCheckoutCEP(CEP cep) {
        ConcurrentCheckoutStmt concurrentCheckoutStmt = new ConcurrentCheckoutStmt(cep.getEPRuntime().getDeploymentService(), cep.getCEPConfig());
        ConcurrentCheckoutStmtResult concurrentCheckoutStmtResult = new ConcurrentCheckoutStmtResult();
        concurrentCheckoutStmt.addListener(concurrentCheckoutStmtResult);
    }

    public static void checkoutWhilePriceUpdateCEP(CEP cep) {
        CheckoutWhilePriceUpdateStmt checkoutWhilePriceUpdateStmt = new CheckoutWhilePriceUpdateStmt(cep.getEPRuntime().getDeploymentService(), cep.getCEPConfig());
        CheckoutWhilePriceUpdateResult checkoutWhilePriceUpdateResult = new CheckoutWhilePriceUpdateResult();
        checkoutWhilePriceUpdateStmt.addListener(checkoutWhilePriceUpdateResult);
    }

    //TODO:delete
    public static void allEventsCEP(CEP cep) {
        AllEventsStmt allEventsStmt = new AllEventsStmt(cep.getEPRuntime().getDeploymentService(), cep.getCEPConfig());
        AllEventsResult allEventsResult = new AllEventsResult();
        allEventsStmt.addListener(allEventsResult);
    }

    public static List<UUID> createUsersFromArgs(ArrayList<String> args){
        int numberOfUsers = 0;
        if (args.size() > 0){
            numberOfUsers = Integer.parseInt(args.get(0));
        }
        List<UUID> userIds = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            userIds.add(UUID.randomUUID());
        }
        return userIds;
    }
}
