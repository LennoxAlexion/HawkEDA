package scenario.implementations;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

//<itemId, SeqId>

public class EShopSyncHelper {
    private static final String CATALOG_API_URL = "http://localhost:5101/api/v1/Catalog/";
    private static final String BASKET_API_URL = "http://localhost:5103/api/v1/Basket/";

    public static ConcurrentHashMap<Integer, UUID>  priceUpdateReq = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, Integer>  priceUpdateSeqId = new ConcurrentHashMap<>();
    public static HashMap<UUID, BasketItem> userBasketItem = new HashMap<>();
    // puReqId is used to keep track of update request and comparison in CEP result
    // seqId is to define the order

    public static void addBasketItemsToBasket(UUID userId, ArrayList<BasketItem> basketItems) {
        Basket basket = new Basket();
        basket.setBuyerId(userId);
        basket.setItems(basketItems);
        userBasketItem.put(userId, basketItems.get(0));

//        System.out.println("addBasketItemsToBasket request body: " + basket.toJSON().toString());
        HTTPRestHelper.HTTPPost(BASKET_API_URL, basket.toJSON().toString(), new ArrayList<>());
    }

    public static void updateCatalogItem(int productId, String productName, int quantity, float price) {
        // Update catalog item using PUT request.
        CatalogItem catalogItem = new CatalogItem(productId, productName, quantity, price);
        JSONObject requestBody = catalogItem.toJSON();
        UUID puReqId = UUID.randomUUID();
        priceUpdateReq.put(productId, puReqId);
        requestBody.put("PuReqId", puReqId);

        AtomicInteger seqId = new AtomicInteger();
        priceUpdateSeqId.compute( productId, (key, value) -> {
            value = (value == null ? 1 : value + 1);
            seqId.set(value);
            return value;
        });
        requestBody.put("SeqId", seqId);
        HTTPRestHelper.HTTPPut(CATALOG_API_URL + "items", requestBody.toString());
    }

    public static void checkout(UUID userId) {
        //Add user details and checkout the basket for the user.
        String requestIdStr = UUID.randomUUID().toString();

        UserDetail userDetails = new UserDetail(userId);
        JSONObject requestBody = userDetails.toJSON();
        requestBody.put("requestId", requestIdStr);

        BasketItem basketItem = userBasketItem.get(userId);
        UUID puReqId = priceUpdateReq.getOrDefault(basketItem.productId, null);
        //Add the latest puRequestId
        requestBody.put("PuReqId", puReqId);

        AtomicInteger seqId = new AtomicInteger();
        priceUpdateSeqId.compute( basketItem.productId, (key, value) -> {
            value = (value == null ? 1 : value + 1);
            seqId.set(value);
            return value;
        });
        requestBody.put("SeqId", seqId);

        List<NameValuePair> headerParams = new ArrayList<>();
        headerParams.add(new BasicNameValuePair("x-requestid", requestIdStr));
        headerParams.add(new BasicNameValuePair("userId", userId.toString()));
        HTTPRestHelper.HTTPPost(BASKET_API_URL + "checkout", requestBody.toString(), headerParams);
    }

    public static void checkoutUsersConcurrent(List<UUID> userIds, Logger log) {
//        System.out.println("CPU Cores: " + Runtime.getRuntime().availableProcessors() + " max memory: " + Runtime.getRuntime().maxMemory());
        int nThreads = 10;
        try (InputStream input = EShopHelper.class.getClassLoader().getResourceAsStream("HawkEDA.properties")) {
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
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            });
        }
        executorService.shutdown();
    }
}
