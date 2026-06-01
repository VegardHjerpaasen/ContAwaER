import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import publics.*;

public class ResultPrinter {

    private static final String RESET = "\u001B[0m";

    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String PURPLE = "\u001B[35m";

    private static final String BOLD = "\u001B[1m";

    public static void print(
            String json,
            Config.PrintStyle style) throws Exception {

        switch (style) {

            case JSON -> printJson(json);

            case SUMMARY -> printSummary(json);

            case THESIS -> printThesis(json);
        }
    }

    private static void printJson(String json)
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        Object obj =
                mapper.readValue(json, Object.class);

        System.out.println(
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(obj)
        );
    }

    private static void printSummary(String json)
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode hits = root.path("hits").path("hits");

        System.out.println();
        System.out.println(BOLD + BLUE +
                "======================================================" +
                RESET);

        System.out.println(BOLD + BLUE +
                "                    SEARCH SUMMARY" +
                RESET);

        System.out.println(BOLD + BLUE +
                "======================================================" +
                RESET);

        System.out.println();

        for (int i = 0; i < hits.size(); i++) {

            JsonNode hit = hits.get(i);

            String qid = hit.path("_id").asText();
            double score = hit.path("_score").asDouble();

            JsonNode source = hit.path("_source");

            String label = source.path("label").asText();
            String description = source.path("description").asText();
            String coarseType = source.path("coarse_type").asText();
            String fineType = source.path("fine_type").asText();

            String scoreColor =
                    score >= 50 ? GREEN :
                            score >= 25 ? YELLOW :
                            RESET;

            System.out.printf(
                    "#%d | %s | %sScore: %.2f%s%n",
                    i + 1,
                    qid,
                    scoreColor,
                    score,
                    RESET
            );

            System.out.println(
                    CYAN + "  Label       : " +
                            RESET + label
            );

            System.out.println(
                    CYAN + "  Coarse Type : " +
                            RESET + coarseType
            );

            System.out.println(
                    CYAN + "  Fine Type   : " +
                            RESET + fineType
            );

            System.out.println(
                    CYAN + "  Description : " +
                            RESET + description
            );

            System.out.println();
        }
    }

    private static void printThesis(String json)
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode hits = root.path("hits").path("hits");

        System.out.println();

        System.out.printf(
                PURPLE + "%-5s" + RESET + " " +
                        CYAN + "%-15s" + RESET + " " +
                        GREEN + "%-10s" + RESET + " %-40s%n",
                "Rank",
                "QID",
                "Score",
                "Label"
        );

        System.out.println(
                "--------------------------------------------------------------------------"
        );

        for (int i = 0; i < hits.size(); i++) {

            JsonNode hit = hits.get(i);

            String qid = hit.path("_id").asText();
            double score = hit.path("_score").asDouble();

            String label = hit.path("_source")
                    .path("label")
                    .asText();

            System.out.printf(
                    "%-5d %-15s %-10.2f %-40s%n",
                    i + 1,
                    qid,
                    score,
                    label
            );
        }

        System.out.println();
    }
}