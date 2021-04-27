package cep.result;

import cep.dto.EventDTO;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllEventsResult implements UpdateListener {

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement epStatement, EPRuntime epRuntime) {
        if (newEvents == null || newEvents.length == 0) {
            log.info("No events arrived...");
            return;
        }

        log.info("-------CEP Analysis (AllEventsResult)-------");
        for (EventBean event : newEvents) {
            log.info("---------------------Event---------------------------");
            EventDTO currentEvent = (EventDTO) event.getUnderlying();
            log.info(currentEvent.toString());
        }
        log.info("---------------------------------------------------------\n");
    }
}
