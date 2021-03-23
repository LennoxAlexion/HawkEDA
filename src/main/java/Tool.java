import cep.CEP;
import messaging.RabbitMQConnector;
import scenario.implementations.CheckoutWhilePriceUpdateScenario;
import scenario.implementations.ConcurrentCheckoutScenario;
import scenario.implementations.ConcurrentOutOfStockCheckoutScenario;
import scenario.implementations.TestCatalogService;
import scenario.interfaces.ScenarioInterface;

@lombok.extern.slf4j.Slf4j
public class Tool {
    private CEP cep;

    public static void main(String[] args) {
        System.out.println("Started at: " + new java.util.Date().toString());
        Tool tool = new Tool();

        RabbitMQConnector rabbitMQConnector = RabbitMQConnector.getInstance();
        rabbitMQConnector.listenToeShopEvents();

        tool.initCEP();

        int scenario = 3;
        ScenarioInterface scenarioInterface;

        // TODO: Update with a list of scenarios.
        switch (scenario) {
            case 1:
                scenarioInterface = new ConcurrentCheckoutScenario();
                break;
            case 2:
                scenarioInterface = new ConcurrentOutOfStockCheckoutScenario();
                break;
            case 3:
                scenarioInterface = new CheckoutWhilePriceUpdateScenario();
                break;
            default:
                scenarioInterface = new TestCatalogService();
        }

        RabbitMQConnector.getInstance().publishEvent(ScenarioInterface.ScenarioInitEvent);
        scenarioInterface.registerCEPQueries(tool.cep);
        scenarioInterface.initScenario();
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        scenarioInterface.execute();
    }

    private void initCEP() {
        cep = CEP.getInstance();
        cep.getCEPConfig();
        cep.getEPRuntime();
    }
}