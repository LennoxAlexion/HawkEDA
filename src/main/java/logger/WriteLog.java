package logger;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.Properties;

@Slf4j
public class WriteLog {

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

        String defaultLogPath = System.getProperty("user.dir") + File.separator + "logs" + File.separator;
        String logPath;
        FileWriter fileWriter;

        try (InputStream input = WriteLog.class.getClassLoader().getResourceAsStream("HawkEDA.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            logPath = properties.getProperty("logger.path");
            fileWriter = createFileWriter(eventLogFileName, logPath);
        } catch (Exception e) {
            log.error("Unable to create log file in the specified path, trying default path.");
            fileWriter = createFileWriter(eventLogFileName, defaultLogPath);
        }
            return new BufferedWriter(fileWriter);
        }

    private static FileWriter createFileWriter(String eventLogFileName, String logPath) throws IOException {
        String directoryPath = logPath.concat(scenarioName);
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                directoryPath = logPath;
            }
        }
        return new FileWriter(directoryPath
                + File.separator
                + parameters
                + "-"
                + eventLogFileName
                + "-"
                + startDate
                + ".json", false);
    }

    private static void appendToEventsLog (BufferedWriter bufferedWriter,
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
