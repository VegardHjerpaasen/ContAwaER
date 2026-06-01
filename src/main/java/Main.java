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
                System.out.println("starting local ollama\n");
            }

            String mention = "007";

            String context =
                    "fictional agent";

            String generatedQuery = llm.generate(
                    """
                    You are an Entity Retrieval Assistant.
            
                    Your task is to generate a retrieval plan for Elasticsearch.
            
                    You MUST ONLY use the following commands:
            
                    mention(value)
                    mention(value, boost)
                    optionalMention(value)
                    optionalMention(value, boost)
            
                    type(value)
                    type(value, boost)
            
                    strong(value)
                    strong(value, boost)
                    weak(value)
                    weak(value, boost)
            
                    context(value)
                    context(value, boost)
                    description(value)
                    description(value, boost)
            
                    exclude(...)
            
                    confidence(...)
            
                    size(...)
            
                    Definitions:
            
                    mention(value)
                    - The exact surface form found in text.
                    - This is only an anchor, not the full retrieval intent.
                    - Use the original mention, even when it is ambiguous.
                    - For ambiguous mentions, use a low boost around 1-3.
                    - For specific names, use a higher boost around 5-10.
            
                    optionalMention(value)
                    - The mention may be incorrect or misspelled.
                    - Use when uncertain.
            
                    type(value)
                    - Entity type hint.
                    - Can appear multiple times.
                    - Examples:
                      company
                      person
                      place
                      animal
                      character
                      vehicle
                      game
                      movie
                      product
            
                    strong(value)
                    - Strong evidence that should heavily influence retrieval.
                    - Can appear multiple times.
            
                    weak(value)
                    - Weak evidence that may help retrieval.
                    - Can appear multiple times.
            
                    context(value)
                    - A short summary of the context.
            
                    description(value)
                    - Description-like information.
            
                    exclude(value)
                    - Information that should reduce matching.
                    - Can appear multiple times.
            
                    confidence(value)
                    - One of:
                      low
                      medium
                      high
            
                    size(number)
                    - Number of candidates to retrieve.

                    Boosts:

                    - Boosts are optional numbers from 0.1 to 20.
                    - Higher boost means stronger ranking influence.
                    - Use high boosts for disambiguating context, not just exact mention text.
                    - Use lower mention boosts when the mention is short, numeric, generic, or highly ambiguous.
                    - Recommended ranges:
                      mention: 1-3 when ambiguous, 5-10 when specific
                      context: 5-10
                      strong: 6-12
                      weak: 1-4
                      type: 2-6
                      description: 3-8
            
                    Rules:
            
                    - Output ONLY commands.
                    - One command per line.
                    - No explanations.
                    - No markdown.
                    - No JSON.
                    - No code blocks.
                    - No extra text.
                    - Do not invent commands.
                    - Build a soft-filtered query: mention anchors candidates, while context, type, strong, weak, and exclude disambiguate ranking.
                    - Always add context(value) using the user's context when it contains useful information.
                    - For ambiguous mentions, add multiple strong(value) clues that describe the intended entity sense.
                    - For ambiguous mentions, add exclude(value) clues for likely wrong senses.
                    - Do not put the mention itself inside strong(value) or weak(value).
                    - Prefer semantic clues over exact-name repetition.
            
                    Example:
            
                    mention(Jaguar, 2)
                    type(company, 5)
                    type(vehicle, 3)
                    context(luxury car manufacturer producing electric vehicles, 8)
            
                    strong(car manufacturer, 10)
                    strong(electric vehicles, 8)
            
                    weak(luxury, 3)
            
                    exclude(animal)
                    exclude(sports team)
            
                    confidence(high)
            
                    size(10)
            
                    Task:
            
                    Mention:
                    %s
            
                    Context:
                    %s
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
                    QueryType.LLM_TEMPLATE
            );

            System.out.println("Raw Result:\n"+result);

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
                "Total Pipeline: %.2f ms%n",
                (totalEnd - totalStart) / 1_000_000.0
        );
        System.out.printf(
                "Total Elastic Search Time: %.2f ms%n",
                (EStime)
        );
        System.out.printf(
                "Total LLM Prompt to Answer Time: %.2f ms%n",
                (LLMqTime)
        );
    }
}

