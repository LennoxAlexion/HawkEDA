package utilities;

import messaging.RabbitMQConnector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.Properties;

public class LogEvents {
    private static final Logger log = LoggerFactory.getLogger(LogEvents.class);

    private static JSONArray eventsLogJSONArray;

    public static String scenarioName = "";
    public static String parameters = "";
    public static String startDate = new java.util.Date().toString();

    public static void writeEventToLog(JSONObject jsonObject) {
        try {
            BufferedWriter bufferedWriter = getWriter("Events");
            appendToEventsLog(bufferedWriter, jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void writeResultToLog(JSONObject jsonObject) {
        try {
            BufferedWriter bufferedWriter = getWriter("Result");
            bufferedWriter.write(jsonObject.toString());
            // TODO: flush only after closing connection.
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedWriter getWriter(String eventLogFileName) throws IOException {

        String defaultLogPath = LogEvents.class.getResource("..") + "logs" + File.separator ;
        String logPath;

        try (InputStream input = LogEvents.class.getClassLoader().getResourceAsStream("tool.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            logPath = properties.getProperty("logger.path");
        } catch (IOException e) {
            e.printStackTrace();
            logPath = defaultLogPath;
        }

        String directoryPath = logPath.concat(scenarioName);
        File directory = new File(directoryPath);
        if (! directory.exists()){
            if(! directory.mkdir()){
                directoryPath = logPath;
            }
        }

        return new BufferedWriter(new FileWriter(
                directoryPath
                        + File.separator
                        + parameters
                        + "-"
                        + eventLogFileName
                        + "-"
                        + startDate
                        + ".json", false));
    }

    private static void appendToEventsLog(BufferedWriter bufferedWriter,
                                          JSONObject jsonObjectInput) throws IOException {
        if (eventsLogJSONArray == null) {
            eventsLogJSONArray = new JSONArray();
        }
        eventsLogJSONArray.put(jsonObjectInput);
        bufferedWriter.write(eventsLogJSONArray.toString());
        // TODO: flush only after closing connection.
        bufferedWriter.flush();
    }
}
