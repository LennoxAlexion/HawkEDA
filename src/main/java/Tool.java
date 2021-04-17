import cep.CEP;
import messaging.RabbitMQConnector;
import scenario.implementations.CheckoutWhilePriceUpdateScenario;
import scenario.implementations.SingleBasketMultipleCheckoutScenario;
import scenario.implementations.OutOfStockCheckoutScenario;
import scenario.implementations.TestCatalogService;
import scenario.interfaces.ScenarioInterface;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

@lombok.extern.slf4j.Slf4j
public class Tool {
    private CEP cep;

    public static void main(String[] args) {
        System.out.println("Started at: " + new java.util.Date());
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

        Tool tool = new Tool();

        // Initialize RabbitMQ connector for eShopOnContainers.
        // TODO: Replace this with a more generic one.
        RabbitMQConnector rabbitMQConnector = RabbitMQConnector.getInstance();
        rabbitMQConnector.listenToeShopEvents();

        tool.initCEP();

        RabbitMQConnector.getInstance().publishEvent(ScenarioInterface.ScenarioInitEvent);
        scenarioInterface.registerCEPQueries(parameters, tool.cep);
        scenarioInterface.initScenario(parameters);

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

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
}