package utilities;

import cep.CEP;
import lombok.extern.slf4j.Slf4j;
import messaging.RabbitMQConnector;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class StopToolExecution {
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

        long delay = 240000L; // 4 minutes
        timer.schedule(task, delay);
    }
}
