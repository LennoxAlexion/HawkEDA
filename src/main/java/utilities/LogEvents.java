package utilities;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LogEvents {
    private static final Logger log = LoggerFactory.getLogger(LogEvents.class);

    private static BufferedWriter bufferedWriter;
    private static JSONArray jsonArray;

    private static void getWriter() throws IOException {
//        if (bufferedWriter == null) {
            bufferedWriter = new BufferedWriter(new FileWriter("./src/main/java/logs/All_Event_Log.json", false));
//        }
    }

    public static void appendToJsonArray(JSONObject jsonObject) throws IOException {
        getWriter();
        if(jsonArray == null){
            jsonArray = new JSONArray();
        }
        jsonArray.put(jsonObject);
        bufferedWriter.write(jsonArray.toString());
        // TODO: flush only after closing connection.
        bufferedWriter.flush();
    }

    public static void flushLog() throws IOException {
        if(bufferedWriter != null){
            bufferedWriter.flush();
        }
    }

    public static void logString(String content) throws IOException {
        getWriter();
        bufferedWriter.write(content);
        bufferedWriter.flush();
        log.info("Successfully updated events to the log file...");
    }
}
