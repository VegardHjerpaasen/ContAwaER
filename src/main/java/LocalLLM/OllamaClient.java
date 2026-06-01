package LocalLLM;

//temp solution

//install OLLAMA
//powershell: ollama pull qwen3:8b (5.2GB ish)

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OllamaClient {

    private final HttpClient client;
    private final ObjectMapper mapper;
    public static double timer;

    public OllamaClient() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String generate(String prompt)
            throws IOException, InterruptedException {
        System.out.println("Preparing request...");
        long start = System.nanoTime();
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode requestBody =
                mapper.createObjectNode();

        requestBody.put("model", "qwen3:8b");
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        String body =
                mapper.writeValueAsString(requestBody);

        System.out.println(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        System.out.println("HTTP response received!");
        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        JsonNode root = mapper.readTree(response.body());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: [" + response.body() + "]");
        long end = System.nanoTime();
        timer = (end - start) / 1_000_000.0;
        return root.path("response").asText();
    }
}