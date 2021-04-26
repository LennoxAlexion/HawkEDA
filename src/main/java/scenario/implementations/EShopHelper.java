package scenario.implementations;

import cep.CEP;
import cep.result.AllEventsResult;
import cep.result.CheckoutWhilePriceUpdateResult;
import cep.result.ConcurrentCheckoutStmtResult;
import cep.statement.AllEventsStmt;
import cep.statement.CheckoutWhilePriceUpdateStmt;
import cep.statement.ConcurrentCheckoutStmt;
import com.github.javafaker.Faker;
import com.yahoo.ycsb.WorkloadException;
import com.yahoo.ycsb.generator.ConstantIntegerGenerator;
import com.yahoo.ycsb.generator.NumberGenerator;
import com.yahoo.ycsb.generator.UniformLongGenerator;
import com.yahoo.ycsb.generator.ZipfianGenerator;
import messaging.RabbitMQConnector;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import scenario.implementations.entities.Basket;
import scenario.implementations.entities.BasketItem;
import scenario.implementations.entities.CatalogItem;
import scenario.implementations.entities.UserDetail;
import utilities.HTTPRestHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EShopHelper {

    private static final String CATALOG_API_URL = "http://localhost:5101/api/v1/Catalog/";
    private static final String BASKET_API_URL = "http://localhost:5103/api/v1/Basket/";
    public static final int DEFAULT_PRODUCT_ID = 8;
    public static final String DEFAULT_PRODUCT_NAME = "Default Product";
    public static final float DEFAULT_PRODUCT_PRICE = 10;
    public static final int DEFAULT_PRODUCT_QUANTITY = 1000;

    public static void addCatalogItem(int productId, String productName, int quantity, float price) {
        // Add/replace the catalog item based on productId
        CatalogItem catalogItem = new CatalogItem(productId, productName, quantity, price);
        JSONObject requestBody = catalogItem.toJSON();
        HTTPRestHelper.HTTPPost(CATALOG_API_URL + "items", requestBody.toString(), new ArrayList<>());
    }

    public static void addCatalogItem(CatalogItem catalogItem) {
        // Add/replace the catalog item based on productId
        JSONObject requestBody = catalogItem.toJSON();
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

    public static HashMap<Integer, CatalogItem> generateCatalogItem(int catalogSize) {
        HashMap<Integer, CatalogItem> catalogItems = new HashMap<>();
        for (int itemId = 1; itemId <= catalogSize; itemId++) {
            Faker faker = new Faker();
            String productName = faker.commerce().productName();
            float price = faker.number().numberBetween(10, 500);
            CatalogItem catalogItem = new CatalogItem(itemId, productName, 10000, price);
            catalogItems.put(itemId, catalogItem);
            addCatalogItem(catalogItem);
        }
        return catalogItems;
    }

    public static ArrayList<BasketItem> generateBasketItemsFromCatalog(UUID basketId,
                                                                       HashMap<Integer, CatalogItem> catalogItems,
                                                                       int maxBasketSize) {
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

    public static ArrayList<BasketItem> generateBasketItemsFromCatalog(UUID basketId,
                                                                       HashMap<Integer, CatalogItem> catalogItems,
                                                                       NumberGenerator itemKeyChooser) {
        //Generate basket item of size 1 and different catalog items in it.
        ArrayList<BasketItem> basketItems = new ArrayList<>();
        Faker faker = new Faker();
        //Item is selected using given distribution.
        int catalogIndex = Integer.parseInt(itemKeyChooser.nextString()) + 1;
        basketItems.add(new BasketItem(basketId,
                catalogItems.get(catalogIndex).id,
                catalogItems.get(catalogIndex).name,
                catalogItems.get(catalogIndex).price,
                faker.number().numberBetween(1, 10)));
        return basketItems;
    }

    public static void checkout(UUID userId) {
        //Add user details and checkout the basket for the user.
        String requestIdStr = UUID.randomUUID().toString();

        UserDetail userDetails = new UserDetail(userId);
        JSONObject requestBody = userDetails.toJSON();
        requestBody.put("requestId", requestIdStr);

        List<NameValuePair> headerParams = new ArrayList<>();
        headerParams.add(new BasicNameValuePair("x-requestid", requestIdStr));
        headerParams.add(new BasicNameValuePair("userId", userId.toString()));
        HTTPRestHelper.HTTPPost(BASKET_API_URL + "checkout", requestBody.toString(), headerParams);
    }

    public static void checkoutUsersConcurrent(List<UUID> userIds, Logger log) {
//        System.out.println("CPU Cores: " + Runtime.getRuntime().availableProcessors() + " max memory: " + Runtime.getRuntime().maxMemory());
        int nThreads = 10;
        try (InputStream input = EShopHelper.class.getClassLoader().getResourceAsStream("tool.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            nThreads = Integer.parseInt(properties.getProperty("executors.nThreads"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        for (UUID userId : userIds) {
            executorService.execute(() -> {
                log.info("Concurrently checking out user: " + userId);
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

    public static void allEventsCEP(CEP cep) {
        AllEventsStmt allEventsStmt = new AllEventsStmt(cep.getEPRuntime().getDeploymentService(), cep.getCEPConfig());
        AllEventsResult allEventsResult = new AllEventsResult();
        allEventsStmt.addListener(allEventsResult);
    }

    public static List<UUID> createUsersFromArgs(int numberOfUsers) {
        List<UUID> userIds = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            userIds.add(UUID.randomUUID());
        }
        return userIds;
    }

    // From YCSB
    public static NumberGenerator getKeyChooser(String requestDistrib, int size, double zipfContant) throws WorkloadException {
        NumberGenerator keychooser;

        if ("uniform".equals(requestDistrib)) {
            keychooser = new UniformLongGenerator(0, size - 1);
        } else if ("zipfian".equals(requestDistrib)) {
            keychooser = new ZipfianGenerator(size, zipfContant);
        } else if ("stress".equals(requestDistrib)) {
            keychooser = new ConstantIntegerGenerator(0);
        } else {
            throw new WorkloadException("Unknown request distribution \"" + requestDistrib + "\"");
        }
        return keychooser;
    }
}
