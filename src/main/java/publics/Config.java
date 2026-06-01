package publics;

public class Config {

    //remember to setx these in you powershell etc.
    public static final String TOKEN = System.getenv("ELASTIC_TOKEN");
    public static final String INDEX = System.getenv("ELASTIC_INDEX");
    public static final String ES_URL = System.getenv("ELASTIC_URL");

    public enum PrintStyle {
        JSON,
        SUMMARY,
        THESIS
    }
    public static final PrintStyle PRINT_STYLE =
            PrintStyle.SUMMARY;

    //SUMMARY - print info relevant to testing
    //JSON - print the entire json, but formatted


    //Ollama settings
    public static final boolean LocalLLM = true; //set true to run local client
    public static final String OllamaURL = System.getenv("OLLAMA_URL"); //setx OLLAMA_URL if you have an external connection (and set LocalLLM to false to use it)
}