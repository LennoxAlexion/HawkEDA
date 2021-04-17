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
import scenario.implementations.CheckoutWhilePriceUpdateScenario;

import java.time.Instant;

import static scenario.implementations.EShopHelper.DEFAULT_PRODUCT_ID;
import static scenario.implementations.EShopHelper.DEFAULT_PRODUCT_PRICE;

public class CheckoutWhilePriceUpdateResult implements UpdateListener {
    private static final Logger log = LoggerFactory.getLogger(cep.result.ConcurrentCheckoutStmtResult.class);
    private int countOutdatedPrices = 0;
    private Instant priceUpdateTimestamp;

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement epStatement, EPRuntime epRuntime) {
        if (newEvents == null || newEvents.length == 0) {
            log.info("No events arrived...");
            return;
        }

        log.info("-------CEP Analysis (ConcurrentCheckoutStmtResult)-------");
        for (EventBean event : newEvents) {
            log.info("---------------------Result---------------------------");
            log.info(event.getUnderlying().toString());

            EventDTO eventDTO = (EventDTO) event.get("checkout");
            float eventPrice = 0;
            if (eventDTO != null) {
                JSONArray itemsJsonArray = eventDTO.getMessageBody().getJSONObject("Basket").getJSONArray("Items");

                for (int i = 0; i < itemsJsonArray.length(); i++) {
                    JSONObject item = itemsJsonArray.getJSONObject(i);
                    if (item.get("ProductId").equals(DEFAULT_PRODUCT_ID)) {
                        eventPrice = item.getFloat("UnitPrice");
                        break;
                    }
                }
                if (eventPrice == DEFAULT_PRODUCT_PRICE) {
                    countOutdatedPrices++;
                }
            }
            System.out.println("The current count of orders placed with outdated prices is " + countOutdatedPrices + " Ordered Price: " + eventPrice);
            // AssertExpected can be used to stop.
        }
        log.info("---------------------------------------------------------\n");
    }
}