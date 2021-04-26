package cep.result;

import cep.dto.EventDTO;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.LogEvents;
import utilities.StopToolExecution;

import java.time.Instant;
import java.util.HashMap;

public class CheckoutWhilePriceUpdateResult implements UpdateListener {
    private static final Logger log = LoggerFactory.getLogger(cep.result.ConcurrentCheckoutStmtResult.class);
    private int countOutdatedPrices = 0;
    private Instant priceUpdateTimestamp;
    final private HashMap<Integer, Float> updatedPriceList = new HashMap<>();

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement epStatement, EPRuntime epRuntime) {
        if (newEvents == null || newEvents.length == 0) {
            log.info("No events arrived...");
            return;
        }

        log.info("-------CEP Analysis (ConcurrentCheckoutStmtResult)-------");
        for (EventBean event : newEvents) {
            log.info("---------------------Result---------------------------");
            log.info(event.getUnderlying().toString());

            EventDTO eventDTO = (EventDTO) event.getUnderlying();
            if ("ProductPriceChangedIntegrationEvent".equals(eventDTO.getEventName())) {
                updatedPriceList.put(eventDTO.getMessageBody().getInt("ProductId"), eventDTO.getMessageBody().getFloat("NewPrice"));

            } else if ("UserCheckoutAcceptedIntegrationEvent".equals(eventDTO.getEventName())) {
                JSONArray itemsJsonArray = eventDTO.getMessageBody().getJSONObject("Basket").getJSONArray("Items");

                // Check if all the items present in the basket has the latest price.
                for (int i = 0; i < itemsJsonArray.length(); i++) {
                    JSONObject item = itemsJsonArray.getJSONObject(i);
                    int productId = item.getInt("ProductId");
                    if (updatedPriceList.containsKey(productId) &&
                            updatedPriceList.get(productId) != item.getFloat("UnitPrice")) {
                        countOutdatedPrices++;
                        log.warn("Old product ordered." +
                                " RequestId " + eventDTO.getMessageBody().getString("RequestId") +
                                " Product Id: " + productId +
                                " Old Price: " + item.getFloat("UnitPrice") +
                                " New Price: " + updatedPriceList.get(productId));
                    }
                }
            } else {
                return;
            }
            System.out.println(eventDTO.getEventName() +
                    " Event. The current count of items ordered with outdated price is "
                    + countOutdatedPrices
                    + " Updated Items: "
                    + updatedPriceList);
            JSONObject resultLogObj = new JSONObject();
            resultLogObj.put("Outdated Items Ordered", countOutdatedPrices);
            LogEvents.writeResultToLog(resultLogObj);
            StopToolExecution.getInstance().stopExecution();
        }
        log.info("---------------------------------------------------------\n");
    }
}