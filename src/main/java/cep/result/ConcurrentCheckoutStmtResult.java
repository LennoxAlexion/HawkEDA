package cep.result;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import utilities.StopToolExecution;

@Slf4j
public class ConcurrentCheckoutStmtResult implements UpdateListener {

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement epStatement, EPRuntime epRuntime) {
        if (newEvents == null || newEvents.length == 0) {
            log.info("No events arrived...");
            return;
        }

        log.info("-------CEP Analysis ("+ this.getClass().getName() + ")-------");
        for (EventBean event : newEvents) {
            log.info("---------------------Result---------------------------");
            log.info(event.getUnderlying().toString());
            JSONObject eventJSONObj = new JSONObject(event.getUnderlying().toString().replace('=',':'));
            System.out.println("The current count for event "+ eventJSONObj.getString("eventName") + " is " + eventJSONObj.getInt("count"));
            // AssertExpected can be used to stop.
            StopToolExecution.getInstance().stopExecution();
        }
        log.info("---------------------------------------------------------\n");
    }
}