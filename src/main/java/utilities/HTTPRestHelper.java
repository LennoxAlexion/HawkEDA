package utilities;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.List;

public class HTTPRestHelper {
    public static void HTTPGet(String url){
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("HTTP Get " + url + " Status Code: " + statusCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void HTTPPost(String url, String requestBody, List<NameValuePair> headerParams){
//        List<NameValuePair> urlParameters = new ArrayList<>();
//        urlParameters.add(new BasicNameValuePair("username", "abc"));
//        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpPost httpPost = new HttpPost(url);
        HttpEntity stringEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        for (NameValuePair headerParam : headerParams) {
            httpPost.addHeader(headerParam.getName(), headerParam.getValue());
        }

        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("HTTP Post " + url + " Status Code: " + statusCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void HTTPPut(String url, String requestBody){

        HttpPut httpPut = new HttpPut(url);
        HttpEntity stringEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("HTTP Put " + url + " Status Code: " + statusCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
