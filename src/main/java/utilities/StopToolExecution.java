package utilities;

import cep.CEP;
import messaging.RabbitMQConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class StopToolExecution {
    private static final Logger log = LoggerFactory.getLogger(StopToolExecution.class);
    private static StopToolExecution instance = null;
    private Timer timer;

    private StopToolExecution() {}

    public static StopToolExecution getInstance(){
        if(instance==null)
            instance = new StopToolExecution();

        return instance;
    }

    private void cancelTimer(){
        if(timer != null) {
            timer.cancel();
        }
    }
    public void stopExecution(){
        cancelTimer();
        timer = new Timer("StopTimer");
        TimerTask task = new TimerTask() {
            public void run() {
                log.info("Stopping the execution of the Tool. New events will NOT be processed...");
                try {
                    CEP.getInstance().getEPRuntime().destroy();
                    RabbitMQConnector.getInstance().closeConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timer.cancel();
            }
        };

        long delay = 120000L; // 2 minutes
        timer.schedule(task, delay);
    }
}
