package cep;

import cep.dto.EventDTO;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CEP {

    private static CEP instance = null;

    private Configuration configuration;
    private EPRuntime runtime;

    private CEP() {}

    public static CEP getInstance(){
        if(instance==null)
            instance = new CEP();

        return instance;
    }

    public Configuration getCEPConfig() {
        if(configuration == null) {
            configuration = new Configuration();
            // TODO: Move the adding of eventType to query statement.
            log.info("Adding event type EventDTO  to CEP configuration.");
            configuration.getCommon().addEventType(EventDTO.class);
        }
        return configuration;
    }

    public void setupEPRuntime(){
        Configuration config = getCEPConfig();
        log.info("Setting up CEP runtime");
        runtime = EPRuntimeProvider.getRuntime("CEP", config);
    }

    public EPRuntime getEPRuntime(){
        if(runtime == null){
            setupEPRuntime();
        }
        return runtime;
    }
}