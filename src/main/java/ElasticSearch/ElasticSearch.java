package ElasticSearch;

import publics.Config;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ElasticSearch {
    private final HttpClient client;
    public static double timer;
    public ElasticSearch() {
        this.client = HttpClient.newHttpClient();
    }

    public String query(
            String query,
            QueryType type)
            throws IOException, InterruptedException {
        long start = System.nanoTime();

        String body = type.buildBody(query);

        String url = Config.ES_URL
                + "/debug/elasticsearch/"
                + Config.INDEX
                + "/_search";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(
                        "Authorization",
                        "Bearer " + Config.TOKEN
                )
                .header(
                        "Content-Type",
                        "application/json"
                )
                .POST(
                        HttpRequest.BodyPublishers.ofString(body)
                )
                .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );
        long end = System.nanoTime();
        timer = (end - start) / 1_000_000.0;
        return response.body();
    }
}