package scenario.implementations;

import cep.CEP;
import cep.result.AllEventsResult;
import cep.statement.AllEventsStmt;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import scenario.interfaces.ScenarioInterface;

import java.io.IOException;
import java.util.ArrayList;

public class TestCatalogService implements ScenarioInterface {

    @Override
    public void registerCEPQueries(ArrayList<String> args, CEP cep){
        AllEventsStmt allEventsStmt = new AllEventsStmt(cep.getEPRuntime().getDeploymentService(), cep.getCEPConfig());
        AllEventsResult allEventsResult = new AllEventsResult();
        allEventsStmt.addListener(allEventsResult);
    }

    @Override
    public void initScenario(ArrayList<String> args) {
        final String URL = "http://localhost:5101/api/v1/Catalog/items";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpUriRequest httpUriRequest = new HttpGet(URL);
        try {
            HttpResponse response = httpClient.execute(httpUriRequest);
            System.out.println("Response: " + response);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("Status code: " + statusCode);
            assert statusCode == HttpStatus.SC_OK;
            String responseStr = EntityUtils.toString(response.getEntity());
            JSONObject jsonResponse = new JSONObject(responseStr);
            System.out.println(jsonResponse.getJSONArray("data").getJSONObject(0).get("description"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(ArrayList<String> args) {
    }
}
