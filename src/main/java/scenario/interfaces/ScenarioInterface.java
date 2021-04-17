package scenario.interfaces;

import cep.CEP;

import java.util.ArrayList;

public interface ScenarioInterface {
    String ScenarioInitEvent = "INITIALIZE_SCENARIO";
    String ScenarioExecuteEvent = "EXECUTE_SCENARIO";
    void registerCEPQueries(ArrayList<String> args, CEP cep);
    void initScenario(ArrayList<String> args);
    void execute(ArrayList<String> args);
}
