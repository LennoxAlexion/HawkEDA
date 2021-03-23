package cep.result;

import cep.dto.EventDTO;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scenario.implementations.CheckoutWhilePriceUpdateScenario;

import java.time.Instant;

public class CheckoutWhilePriceUpdateResult implements UpdateListener {
    private static final Logger log = LoggerFactory.getLogger(cep.result.ConcurrentCheckoutStmtResult.class);
    private int countOutdatedPrices = 0;
    private Instant priceUpdateTimestamp;

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement epStatement, EPRuntime epRuntime) {
        if (newEvents == null || newEvents.length == 0) {
            log.info("No events arrived...");
            return;
        }

//        log.info("A total of {} New event(s) received in ConcurrentCheckoutStmtResult at {}.", newEvents.length, new java.util.Date());
        log.info("-------CEP Analysis (ConcurrentCheckoutStmtResult)-------");
        for (EventBean event : newEvents) {
            log.info("---------------------Result---------------------------");
            log.info(event.getUnderlying().toString());

            EventDTO eventDTO = (EventDTO) event.get("priceUpdate2");
            if (eventDTO != null) {
                priceUpdateTimestamp = Instant.parse(eventDTO.getTimestamp());
                countOutdatedPrices = 0;
            } else {
                eventDTO = (EventDTO) event.get("checkout");
                float eventPrice = eventDTO.getMessageBody().getJSONObject("Basket").getJSONArray("Items").getJSONObject(0).getFloat("UnitPrice");
                if (eventDTO != null &&
                        priceUpdateTimestamp != null &&
                        Instant.parse(eventDTO.getTimestamp()).compareTo(priceUpdateTimestamp) > 0 &&
                        eventPrice == CheckoutWhilePriceUpdateScenario.oldPrice) {
                    countOutdatedPrices++;
                }
            }
            System.out.println("The current count of orders placed with outdated prices is " + countOutdatedPrices);
            // AssertExpected can be used to stop.
        }
        log.info("---------------------------------------------------------\n");
    }
}