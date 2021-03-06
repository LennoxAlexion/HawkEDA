import cep.CEP;
import lombok.extern.slf4j.Slf4j;
import messaging.RabbitMQConnector;
import scenario.interfaces.ScenarioInterface;
import logger.WriteLog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class HawkEDA {
    private CEP cep;

    public static void main(String[] args) {
        Date startTime = new java.util.Date();
        System.out.println("Started at: " + startTime);
        ScenarioInterface scenarioInterface = null;

        // Get the scenario implementation for the passed args.
        try {
            scenarioInterface = getScenario(args);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if(scenarioInterface == null){
            return; //terminate if scenario does not exist.
        }

        // TODO: Parse the parameters and convert to Map instead of arraylist later.
        ArrayList<String> parameters = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
        log.info("Executing Scenario: " + scenarioInterface.getClass().getName() + " with parameters:"
                + parameters);

        HawkEDA hawkEDA = new HawkEDA();
        hawkEDA.setupLogger(scenarioInterface, startTime, parameters);

        // Initialize RabbitMQ connector for eShopOnContainers.
        // TODO: Replace this with a more generic one.
        RabbitMQConnector rabbitMQConnector = RabbitMQConnector.getInstance();
        rabbitMQConnector.listenToeShopEvents();

        hawkEDA.initCEP();

        RabbitMQConnector.getInstance().publishEvent(ScenarioInterface.ScenarioInitEvent);
        scenarioInterface.registerCEPQueries(parameters, hawkEDA.cep);
        scenarioInterface.initScenario(parameters);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RabbitMQConnector.getInstance().publishEvent(ScenarioInterface.ScenarioExecuteEvent);
        scenarioInterface.execute(parameters);
    }

    private static ScenarioInterface getScenario(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(args.length == 0){
            log.warn("Exiting: No arguments passed to execute any scenarios");
            return null;
        }
        // It is expected that the first argument is the scenario class name present in path scenario.implementations.*
        return (ScenarioInterface) Class.forName("scenario.implementations."+args[0]).getConstructor().newInstance();
    }

    private void initCEP() {
        cep = CEP.getInstance();
        cep.getCEPConfig();
        cep.getEPRuntime();
    }

    private void setupLogger(ScenarioInterface scenarioInterface, Date startDate, ArrayList<String> params){
        WriteLog.scenarioName = scenarioInterface.getClass().getSimpleName();
        WriteLog.parameters = String.join(".", params);
        WriteLog.startDate = startDate.toString().replaceAll(" ",
                "_").replaceAll(":",".");
    }
}