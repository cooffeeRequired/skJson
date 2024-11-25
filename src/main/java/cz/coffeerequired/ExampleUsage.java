package cz.coffeerequired;

import com.google.gson.JsonObject;
import cz.coffeerequired.api.http.MimeMultipartData;
import cz.coffeerequired.api.http.RequestClient;

import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Map;

public class ExampleUsage {

    public static void main(String[] args) {
        try {
            // Initialize the RequestClient
            // https://webhook.site/a09fe39b-5630-466b-904f-783baa2f8a60";
            RequestClient client = new RequestClient();
            String httpbinUrl = "https://httpbin.org/post";
            httpbinUrl = "https://webhook.site/a09fe39b-5630-466b-904f-783baa2f8a60?user=1&and=false";

            // Example 1: POST Request with JSON Body
            JsonObject postBody = new JsonObject();
            postBody.addProperty("action", "Test JSON Post");
            postBody.addProperty("status", "Success");

            HttpResponse<String> postResponse = client
                    .setUri(httpbinUrl)
                    .method("POST")
                    .setJsonBody(postBody)
                    .addHeaders(Map.of("Content-Type", "application/json"))
                    .send();
            printResponse("POST JSON", postResponse);

            // Example 2: Multipart Form Data
            MimeMultipartData multipartData = MimeMultipartData.newBuilder()
                    .addText("key", "value")
                    .addText("description", "Testing Multipart Upload")
                    .addFile("img", Path.of("IMG_1286.JPEG"), MimeMultipartData.FileType.AUTOMATIC)
                    .addContent("{\"hello there\": false}")
                    .build();

            HttpResponse<String> multipartResponse = client
                    .setUri(httpbinUrl)
                    .setBodyPublisher(multipartData.getBodyPublisher())
                    .addHeaders(Map.of("Content-Type", multipartData.getContentType()))
                    .method("POST")
                    .sendAsync()
                    .get(); // Using async request with a blocking get for simplicity
            printResponse("Multipart POST", multipartResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printResponse(String requestType, HttpResponse<String> response) {
        SkJson.logger().info(String.format("&5=== %s Response ===%n", requestType));
        SkJson.logger().info("&5Status Code: " + response.statusCode());
        SkJson.logger().info("&5Headers: " + response.headers());
        SkJson.logger().info("B&5ody: " + response.body());
        SkJson.logger().info("&5=======================");
    }
}
