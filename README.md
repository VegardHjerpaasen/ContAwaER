# ContAwaER (or something)

Context-Aware Entity Retrieval experiments

Prerequisites
* Java 21+
* Maven
* Access to the Elasticsearch API
* Elasticsearch index name
* Bearer authentication token


# Configuration

All project settings are located in `Config.java`.

Example:

```java
public class Config {

    public static final String TOKEN =
            System.getenv("ELASTIC_TOKEN");

    public static final String INDEX =
            System.getenv("ELASTIC_INDEX");

    public static final String ES_URL =
            System.getenv("ES_URL");

    public static final PrintStyle PRINT_STYLE =
            PrintStyle.SUMMARY;
}
```

To change how results are displayed, modify:

```java
Config.PRINT_STYLE
```

---

# Running Queries

Create an Elasticsearch client:

```java
import ElasticSearch.ElasticSearch;

ElasticSearch es = new ElasticSearch();
```

Queries are executed using:

```java
es.query(query, queryType);
```

The first parameter contains the query.

The second parameter determines how the query should be interpreted.

---

# SIMPLE Queries

A SIMPLE query automatically generates an Elasticsearch `multi_match` query.

Example:

```java
import ElasticSearch.QueryType;

String result = es.query(
        "Rome",
        QueryType.SIMPLE
);
```

Another example:

```java
import ElasticSearch.QueryType;

String result = es.query(
        "Realm of the Mad God hardest dungeon",
        QueryType.SIMPLE
);
```

SIMPLE queries search across:

* label
* labels
* aliases
* context_string

and are recommended for quick testing.

---

# RAW Queries

RAW queries give full control over the Elasticsearch request body.

The provided string is sent directly to Elasticsearch.

Example:

```java
import ElasticSearch.QueryType;

String result = es.query(
        """
                {
                  "query": {
                    "match_phrase": {
                      "label": "Realm of the Mad God"
                    }
                  },
                  "size": 10
                }
                """,
        QueryType.RAW
);
```

Use RAW queries when experimenting with:

* custom Elasticsearch queries
* filters
* boosting
* fuzzy search
* context-aware retrieval strategies

---

# Printing Results

Results are displayed using:

```java
ResultPrinter.print(
        result,
        Config.PRINT_STYLE
);
```

Available print styles:

```java
PrintStyle.JSON
PrintStyle.SUMMARY
PrintStyle.THESIS
```

### JSON

Prints the full Elasticsearch response.

### SUMMARY

Prints a human-readable overview of retrieved entities.

### THESIS

Prints a compact ranking table useful for retrieval experiments and evaluation.

---

# Example

```java
import ElasticSearch.ElasticSearch;

public static void main(String[] args) {

    ElasticSearch es = new ElasticSearch();

    try {

        String result = es.query(
                "Realm of the Mad God hardest dungeon",
                QueryType.SIMPLE
        );

        ResultPrinter.print(
                result,
                Config.PRINT_STYLE
        );

    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

