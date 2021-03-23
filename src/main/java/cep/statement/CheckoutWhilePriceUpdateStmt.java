package cep.statement;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.EPDeploymentService;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

public class CheckoutWhilePriceUpdateStmt {
    private EPStatement epStatement;

    public CheckoutWhilePriceUpdateStmt(EPDeploymentService epDeploymentService, Configuration configuration) {

//        String stmt = "select * from EventDTO where eventName = \"UserCheckoutAcceptedIntegrationEvent\" or eventName = \"ProductPriceChangedIntegrationEvent\""
        String stmt = "select priceUpdate2, checkout from pattern [priceUpdate1 = EventDTO(eventName =\"ProductPriceChangedIntegrationEvent\") -> every (priceUpdate2 = EventDTO(eventName = \"ProductPriceChangedIntegrationEvent\") or checkout = EventDTO(eventName = \"UserCheckoutAcceptedIntegrationEvent\"))]";
        CompilerArguments compilerArguments = new CompilerArguments(configuration);
        try {
            EPCompiled compiled = EPCompilerProvider.getCompiler().compile(stmt, compilerArguments);
            epStatement = epDeploymentService.deploy(compiled).getStatements()[0];
            epDeploymentService.deploy(compiled).getStatements();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addListener(UpdateListener listener) {
        epStatement.addListener(listener);
    }
}