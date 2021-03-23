package scenario.interfaces;

import cep.CEP;

public interface ScenarioInterface {
    String ScenarioInitEvent = "INITIALIZE_SCENARIO";
    String ScenarioExecuteEvent = "EXECUTE_SCENARIO";
    void registerCEPQueries(CEP cep);
    void initScenario();
    void execute();
}
