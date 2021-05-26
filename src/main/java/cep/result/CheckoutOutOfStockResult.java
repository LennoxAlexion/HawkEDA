package cep.result;

import cep.dto.EventDTO;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import logger.WriteLog;
import scenario.implementations.EShopHelper;
import utilities.StopHawkEDAExecution;

import java.util.HashSet;

@Slf4j
public class CheckoutOutOfStockResult implements UpdateListener {
    private int countInvalidOrders = 0;
    final private HashSet<Integer> orderedItems = new HashSet<>();

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement epStatement, EPRuntime epRuntime) {
        if (newEvents == null || newEvents.length == 0) {
            log.info("No events arrived...");
            return;
        }

        log.info("-------CEP Analysis ("+ this.getClass().getName() + ")-------");
        for (EventBean event : newEvents) {
            log.info("---------------------Result---------------------------");
            log.info(event.getUnderlying().toString());

            EventDTO eventDTO = (EventDTO) event.getUnderlying();
            if ("UserCheckoutAcceptedIntegrationEvent".equals(eventDTO.getEventName())){
                writeResultLogAndScheduleStop();
                EShopHelper.updateCheckoutAcceptedStats();
            }
            else if ("OrderStatusChangedToPaidIntegrationEvent".equals(eventDTO.getEventName())) {
                JSONArray itemsJsonArray = eventDTO.getMessageBody().getJSONArray("OrderStockItems");

                // Count the orders created for the current item.
                for (int i = 0; i < itemsJsonArray.length(); i++) {
                    JSONObject item = itemsJsonArray.getJSONObject(i);
                    int productId = item.getInt("ProductId");
                    if (orderedItems.contains(productId)) {
                        countInvalidOrders++;
                        log.warn("Out of stock product ordered." +
                                " RequestId " + eventDTO.getMessageBody().getString("Id") +
                                " Product Id: " + productId);
                    }else{
                        orderedItems.add(productId);
                    }
                }
            } else {
                return;
            }
            System.out.println(eventDTO.getEventName() +
                    " Event. The current count of orders with out of stock items is "
                    + countInvalidOrders);
            writeResultLogAndScheduleStop();
        }
        log.info("---------------------------------------------------------\n");
    }
    private void writeResultLogAndScheduleStop(){
        JSONObject resultLogObj = new JSONObject();
        resultLogObj.put("ExecutionResult", countInvalidOrders);
        WriteLog.writeResultToLog(resultLogObj);
        StopHawkEDAExecution.getInstance().stopExecution();
    }
}