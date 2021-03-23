package utilities;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;

public class ConvertJSONToCSV {
    public static void main(String[] args) throws IOException {
        convert("./src/main/java/logs/All_Event_Log.json");
    }
    public static void convert(String jsonPath) throws IOException {
        JsonNode jsonTree = new ObjectMapper().readTree(new File(jsonPath));

        CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
        JsonNode firstObject = jsonTree.elements().next();
        firstObject.fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);
        CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();

        CsvMapper csvMapper = new CsvMapper();
        csvMapper.writerFor(JsonNode.class)
                .with(csvSchema)
                .writeValue(new File("./src/main/java/logs/All_Event_Log.csv"), jsonTree);
    }
}
