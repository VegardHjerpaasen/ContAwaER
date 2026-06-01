import LocalLLM.OllamaClient;
import ElasticSearch.*;
import publics.Config;

public class Main {

    public static OllamaClient llm;
    public static double EStime = 0.0;
    public static double LLMqTime = 0.0;

    public static void main(String[] args) {
        long totalStart = System.nanoTime();
        try {

            if (Config.LocalLLM) {
                llm = new OllamaClient();
                System.out.println("starting local ollama");
            }

            String mention = "Jaguar";

            String context =
                    "The company recently released a new luxury electric vehicle.";

            System.out.println("Sending prompt to Ollama...");

            String generatedQuery = llm.generate(
                    """
                    You are an entity retrieval assistant.

                    Generate a search query for Elasticsearch.

                    Mention:
                    %s

                    Context:
                    %s

                    Rules:
                    - Return ONLY the query.
                    - No explanations.
                    - No markdown.
                    - No quotes.
                    - One line only.

                    Example output:
                    Jaguar car manufacturer luxury electric vehicle
                    """
                            .formatted(
                                    mention,
                                    context
                            )
            );

            generatedQuery = generatedQuery.trim();

            System.out.println("Ollama returned!");
            System.out.println(
                    "Generated Query: "
                            + generatedQuery
            );

            LLMqTime = OllamaClient.timer;

            ElasticSearch es = new ElasticSearch();

            String result = es.query(
                    generatedQuery,
                    QueryType.SIMPLE
            );

            ResultPrinter.print(
                    result,
                    Config.PRINT_STYLE
            );
            EStime = ElasticSearch.timer;

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        long totalEnd = System.nanoTime();

        System.out.printf(
                "Total pipeline: %.2f ms%n",
                (totalEnd - totalStart) / 1_000_000.0
        );
        System.out.printf(
                "Total Elastic Search time: %.2f ms%n",
                (EStime)
        );
        System.out.printf(
                "Total LLM Query time: %.2f ms%n",
                (LLMqTime)
        );
    }
}