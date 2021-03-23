package cep;

import cep.dto.EventDTO;
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.DeploymentOptions;
import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPRuntimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CEP {

    private static final Logger log = LoggerFactory.getLogger(CEP.class);

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

    public EPRuntime setupEPRuntime(){
        Configuration config = getCEPConfig();
        log.info("Setting up CEP runtime");
        runtime = EPRuntimeProvider.getRuntime("CEP", config);
        runtime.initialize();
        return runtime;
    }

    public EPRuntime getEPRuntime(){
        if(runtime == null){
            setupEPRuntime();
        }
        return runtime;
    }

    private EPCompiled compileEPL(Configuration configuration, String stmt) {
        log.info("Compiling EPL");
        EPCompiled compiled;
        try {
            compiled = EPCompilerProvider.getCompiler().compile( stmt, new CompilerArguments(configuration));
        } catch (EPCompileException ex) {
            throw new RuntimeException(ex);
        }
        return compiled;
    }

    private void deploy(EPRuntime runtime, EPCompiled compiled) {
        try {
            runtime.getDeploymentService().deploy(compiled, new DeploymentOptions().setDeploymentId("cep"));
        } catch (EPDeployException ex) {
            throw new RuntimeException(ex);
        }
    }
}