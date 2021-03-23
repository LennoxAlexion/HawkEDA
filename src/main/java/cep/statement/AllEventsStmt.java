package cep.statement;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.EPDeploymentService;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;

public class AllEventsStmt {
    private EPStatement epStatement;

    public AllEventsStmt(EPDeploymentService epDeploymentService, Configuration configuration){

        String stmt = "select * from EventDTO";
        CompilerArguments compilerArguments = new CompilerArguments(configuration);
//        compilerArguments.getOptions().setAccessModifierEventType(env -> NameAccessModifier.PUBLIC);

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
