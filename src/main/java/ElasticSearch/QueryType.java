package ElasticSearch;

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
    };

    public abstract String buildBody(String input);
}