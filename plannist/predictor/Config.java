package plannist.predictor;

public class Config {
    // --- I/O DIR settings ---
    public static final String trainDir = "data\\trainingset\\";
    public static final String modelDir = "data\\model\\";
    public static final String statsDir = "data\\stats\\";
    public static final String tts_trainDir = trainDir + "tts\\";
    public static final String arrival_trainDir = trainDir + "arrival\\";
    public static final String alighting_trainDir = trainDir + "alighting\\";
    public static final String tts_modelDir = modelDir + "tts\\";
    public static final String arrival_modelDir = modelDir + "arrival\\";
    public static final String alighting_modelDir = modelDir + "alighting\\";
    public static final String tts_statsDir = statsDir + "tts\\";
    public static final String arrival_statsDir = statsDir + "arrival\\";
    public static final String alighting_statsDir = statsDir + "alighting\\";

    // --- I/O All-in-one chunkize test ---
    public static final String x_modelDir = "dataX\\model\\";
    public static final String x_statsDir = "dataX\\stats\\";
    public static final String x_tts_modelDir = x_modelDir + "tts\\";
    public static final String x_arrival_modelDir = x_modelDir + "arrival\\";
    public static final String x_alighting_modelDir = x_modelDir + "alighting\\";
    public static final String x_tts_statsDir = x_statsDir + "tts\\";
    public static final String x_arrival_statsDir = x_statsDir + "arrival\\";
    public static final String x_alighting_statsDir = x_statsDir + "alighting\\";
    public static final String x_tts_k2K_prefix = "tts_k2K_";
    public static final String x_tts_full_prefix = "tts_full_";

    // --- Important parameter settings ---
    public static final int intervalDur = 15; // in minutes: 5min, 10min, 15min, 20min, 30min
    public static final double percentile_TTS_full = 0.8;  // percentile for stats
    public static final double percentile_AR = 0.5;
    public static final double percentile_AL = 0.5;
}
