package ElasticSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum QueryType {

    SIMPLE {
        @Override
        public String buildBody(String input) {
            return """
                {
                  "query": {
                    "multi_match": {
                      "query": "%s",
                      "fields": [
                        "label^4",
                        "labels^2",
                        "aliases",
                        "context_string"
                      ]
                    }
                  },
                  "size": 10
                }
                """.formatted(input);
        }
    },

    RAW {
        @Override
        public String buildBody(String input) {
            return input;
        }
    },





    /**
     * LLM returns a controlled DSL.
     *
     * Example:
     *
     * mention(Jaguar)
     * type(company)
     * must(vehicle manufacturer)
     * should(electric)
     * should(luxury)
     * not(animal)
     * size(10)
     */

    LLM_TEMPLATE {
        @Override
        public String buildBody(String input) {

            String mention = "";
            double mentionBoost = 10;
            String optionalMention = "";
            double optionalMentionBoost = 3;
            List<WeightedValue> types = new ArrayList<>();

            int size = 10;

            StringBuilder shouldClauses = new StringBuilder();
            StringBuilder mustNotClauses = new StringBuilder();
            StringBuilder filterClauses = new StringBuilder();

            String[] lines = input.split("\\R");

            for (String line : lines) {

                line = line.trim();

                if (line.startsWith("mention(")) {

                    WeightedValue weightedValue = weightedValue(line, 10);
                    mention = weightedValue.value();
                    mentionBoost = weightedValue.boost();

                } else if (line.startsWith("optionalMention(")) {

                    WeightedValue weightedValue = weightedValue(line, 3);
                    optionalMention = weightedValue.value();
                    optionalMentionBoost = weightedValue.boost();

                } else if (line.startsWith("type(")) {

                    WeightedValue type = weightedValue(line, 2);
                    if (!type.value().isBlank()) {
                        types.add(type);
                    }

                } else if (line.startsWith("strong(")) {

                    appendMatch(
                            shouldClauses,
                            "context_string",
                            weightedValue(line, 5)
                    );

                } else if (line.startsWith("weak(")) {

                    appendMatch(
                            shouldClauses,
                            "context_string",
                            weightedValue(line, 2)
                    );

                } else if (line.startsWith("context(")) {

                    appendMatch(
                            shouldClauses,
                            "context_string",
                            weightedValue(line, 4)
                    );

                } else if (line.startsWith("description(")) {

                    appendMatch(
                            shouldClauses,
                            "description",
                            weightedValue(line, 4)
                    );

                } else if (line.startsWith("exclude(")) {

                    mustNotClauses.append("""
                    {
                      "match": {
                        "context_string": "%s"
                      }
                    },
                    """.formatted(extract(line)));

                } else if (line.startsWith("size(")) {

                    try {
                        size = Integer.parseInt(extract(line));
                    }
                    catch (Exception ignored) {
                    }

                } else if (line.startsWith("confidence(")) {

                    // currently ignored
                }
            }

            if (!mention.isBlank()) {

                appendMentionFilter(filterClauses, mention);
                appendMentionMatch(
                        shouldClauses,
                        new WeightedValue(mention, mentionBoost)
                );
            }

            if (!optionalMention.isBlank()) {

                appendMentionMatch(
                        shouldClauses,
                        new WeightedValue(
                                optionalMention,
                                optionalMentionBoost
                        )
                );
            }

            for (WeightedValue type : types) {

                appendMatch(
                        shouldClauses,
                        "context_string",
                        type
                );
            }

            String shouldPart = shouldClauses.toString();
            while (shouldPart.endsWith(",") ||
                    shouldPart.endsWith("\n") ||
                    shouldPart.endsWith("\r")) {

                shouldPart = shouldPart.stripTrailing();

                if (shouldPart.endsWith(",")) {
                    shouldPart =
                            shouldPart.substring(
                                    0,
                                    shouldPart.length() - 1
                            );
                }
            }
            String notPart = mustNotClauses.toString();
            while (notPart.endsWith(",") ||
                    notPart.endsWith("\n") ||
                    notPart.endsWith("\r")) {

                notPart = notPart.stripTrailing();

                if (notPart.endsWith(",")) {
                    notPart =
                            notPart.substring(
                                    0,
                                    notPart.length() - 1
                            );
                }
            }
            String filterPart = filterClauses.toString();
            while (filterPart.endsWith(",") ||
                    filterPart.endsWith("\n") ||
                    filterPart.endsWith("\r")) {

                filterPart = filterPart.stripTrailing();

                if (filterPart.endsWith(",")) {
                    filterPart =
                            filterPart.substring(
                                    0,
                                    filterPart.length() - 1
                            );
                }
            }

            return """
        {
          "query": {
            "bool": {
              "filter": [
                %s
              ],
              "should": [
                %s
              ],
              "must_not": [
                %s
              ],
              "minimum_should_match": 1
            }
          },
          "size": %d
        }
        """
                    .formatted(
                            filterPart,
                            shouldPart,
                            notPart,
                            size
                    );
        }
    };

    public abstract String buildBody(String input);

    private record WeightedValue(String value, double boost) {
    }

    private static void appendMentionFilter(
            StringBuilder clauses,
            String mention) {

        clauses.append("""
        {
          "multi_match": {
            "query": "%s",
            "fields": [
              "label^4",
              "labels^2",
              "aliases"
            ]
          }
        },
        """.formatted(mention));
    }

    private static void appendMentionMatch(
            StringBuilder clauses,
            WeightedValue weightedValue) {

        if (weightedValue.value().isBlank()) {
            return;
        }

        clauses.append("""
        {
          "multi_match": {
            "query": "%s",
            "fields": [
              "label^4",
              "labels^2",
              "aliases"
            ],
            "boost": %s
          }
        },
        """.formatted(
                weightedValue.value(),
                formatBoost(weightedValue.boost())
        ));
    }

    private static void appendMatch(
            StringBuilder clauses,
            String field,
            WeightedValue weightedValue) {

        if (weightedValue.value().isBlank()) {
            return;
        }

        clauses.append("""
        {
          "match": {
            "%s": {
              "query": "%s",
              "boost": %s
            }
          }
        },
        """.formatted(
                field,
                weightedValue.value(),
                formatBoost(weightedValue.boost())
        ));
    }

    private static WeightedValue weightedValue(
            String line,
            double defaultBoost) {

        String value = extract(line).trim();
        double boost = defaultBoost;

        int comma = value.lastIndexOf(',');
        if (comma != -1 && comma < value.length() - 1) {
            String possibleBoost =
                    value.substring(comma + 1).trim();

            try {
                boost = Double.parseDouble(possibleBoost);
                value = value.substring(0, comma).trim();
            }
            catch (NumberFormatException ignored) {
            }
        }

        return new WeightedValue(
                value,
                clampBoost(boost)
        );
    }

    private static double clampBoost(double boost) {

        if (boost < 0.1) {
            return 0.1;
        }
        if (boost > 20) {
            return 20;
        }
        return boost;
    }

    private static String formatBoost(double boost) {

        return String.format(
                Locale.US,
                "%.2f",
                boost
        );
    }

    private static String extract(String line) {

        int start = line.indexOf('(');
        int end = line.lastIndexOf(')');

        if (start == -1 || end == -1 || end <= start) {
            return "";
        }

        return line.substring(start + 1, end);
    }
}
