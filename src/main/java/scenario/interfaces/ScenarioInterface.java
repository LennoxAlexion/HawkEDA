package scenario.interfaces;

import cep.CEP;

public interface ScenarioInterface {
    public final String ScenarioInitEvent = "INITIALIZE_SCENARIO";
    public final String ScenarioExecuteEvent = "EXECUTE_SCENARIO";
    public void registerCEPQueries(CEP cep);
    public void initScenario();
    public void execute();
}
