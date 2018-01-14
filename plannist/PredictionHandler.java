package plannist;

import ennuste.common.Utils;
import ennuste.core.Preprocess;
import libsvm.svm_model;
import libsvm.svm_problem;
import plannist.predictor.*;
import plannist.predictor.helper.StatsDataParser;
import plannist.predictor.helper.TestSet;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Vector;


/**
 * TODO - 1. k->k+1 tts given a time
 * TODO - 2. k->k+1 ar given a time
 * TODO - 3. k->k+1 al given a time
 * TODO - 4. parse all-in-one to both training and stats
 *           - 0->k train wrap[done]
 *           - 0->k stats save [50%]
 *           - k->k+1 stats save [30%]
 */


public class PredictionHandler {

    private static int intervalDur = Config.intervalDur; // min

    /**
     * Generate training file name according to the following params
     * @param routeID
     * @param stopFrom
     * @param stopTo
     * @return
     */
    public static String getTTSTrainingFileName(String routeID, int stopFrom, int stopTo){
        return Config.tts_trainDir + routeID + "\\" +
                String.valueOf(stopFrom) + "-" + String.valueOf(stopTo) + ".csv";
    }

    /**
     * @param routeID
     * @param stopID
     * @param type
     * @return
     */
    public static String getDemandTrainingFileName(String routeID, int stopID, String type){
        String dir = null;
        if(type.equals("AR")){ // arrival
            dir = Config.arrival_trainDir;
        }
        else if(type.equals("AL")) { // alighting
            dir = Config.alighting_trainDir;
        }
        return dir +  routeID + "\\" + String.valueOf(stopID) + ".csv";
    }

    /**
     * @param routeID
     * @param stopFrom
     * @param stopTo
     * @return
     */
    public static String getTTSPModelFileName(String routeID, int stopFrom, int stopTo){
        return Config.tts_modelDir + routeID + "\\" +
                String.valueOf(stopFrom) + "-" + String.valueOf(stopTo) + ".mdl";
    }

    /**
     * @param routeID
     * @param stopID
     * @return
     */
    public static String getDemandModelFileName(String routeID, int stopID, String type){
        String dir = null;
        if(type.equals("AR")){ // arrival
            dir = Config.arrival_modelDir;
        }
        else if(type.equals("AL")) { // alighting
            dir = Config.alighting_modelDir;
        }
        return dir +  routeID + "\\" + String.valueOf(stopID) + ".mdl";
    }

    /**
     * @param routeID
     * @return
     */
    public static String getTTS_FULL_ModelFileName_X(String routeID){
        return Config.x_tts_modelDir + routeID + "\\" + "full.mdl";
    }

    /**
     * @param target
     * @param routeID
     * @param stopID
     * @return
     */
    public static String getDemandModelFileName_X(String target, String routeID, int stopID){
        String dir = null;
        if(target.equals("AR")){ // arrival
            dir = Config.x_arrival_modelDir;
        }
        else if(target.equals("AL")) { // alighting
            dir = Config.x_alighting_modelDir;
        }
        return dir +  routeID + "\\" + String.valueOf(stopID) + ".mdl";
    }

    /**
     * @param routeID
     * @return
     */
    public static String getTTS_FULL_XXXFileName_X(String routeID){
        return Config.x_tts_modelDir + routeID + "\\" + "full.xxx";
    }

    /**
     * @param target
     * @param routeID
     * @param stopID
     * @return
     */
    public static String getDemandXXXFileName_X(String target, String routeID, int stopID){
        String dir = null;
        if(target.equals("AR")){ // arrival
            dir = Config.x_arrival_modelDir;
        }
        else if(target.equals("AL")) { // alighting
            dir = Config.x_alighting_modelDir;
        }
        return dir +  routeID + "\\" + String.valueOf(stopID) + ".xxx";
    }


    /**
     * 创建文件夹
     * @param f
     */
    public static void createDir(File f){
        try{
            boolean res = f.mkdir();
            if(res) {
                System.out.print("...route directory is created.\n");
            }
            else{
                System.out.print("...fail to create route directory!\n");
                System.exit(1);
            }
        }
        catch(SecurityException se){
            System.err.println(se);
            System.exit(1);
        }
    }

    /**
     * 训练行驶时间预测模型
     * @param routeID
     * @param trip
     * @return
     */
    public static boolean trainTTSPModel(String routeID, Vector<Stop> trip) {
        Predictor pred;
        boolean reformatInputFile = true;
        boolean scale = true;
        int N = trip.size();
        int idFrom = 0;
        for(int i = 0; i < N; i ++){
            Stop s = trip.get(i);
            int idTo = i + 1;

            String trainingInputFileName = getTTSTrainingFileName(routeID, idFrom, idTo);
            if(!reformatInputFile){
                trainingInputFileName = trainingInputFileName + ".svm";
            }
            String modelFileName = getTTSPModelFileName(routeID, idFrom, idTo);
            File f1 = new File(trainingInputFileName);

            if(f1.exists() && !f1.isDirectory()) {
                pred = PredictorFactory.getPredictor("SVM");
                pred.setModelFileName(modelFileName);
                return pred.train(trainingInputFileName, reformatInputFile, scale, 10, true);
            }
            else{
                return false;
            }
        }

        return false;
    }

    /**
     * 训练文件不落地模式，训练全程行驶时间预测模型
     * @param routeID
     * @param trip
     * @param parser
     * @return
     */
    public static boolean trainTTSP_FULL_ModelOnFly(String routeID, Vector<Stop> trip, AIOFParser parser){
        boolean scale = true;
        svm_problem problems = parser.getProbs_TTS_full();
        PredictorSVM pred = (PredictorSVM)PredictorFactory.getPredictor("SVM");
        String modelFileName = getTTS_FULL_ModelFileName_X(routeID);
        String xxxFileName = getTTS_FULL_XXXFileName_X(routeID);
        pred.setModelFileName(modelFileName);
        return pred.trainOnFly(problems, scale, 10, true, xxxFileName);
    }

    /**
     * 训练文件不落地模式，训练客源到达率预测模型
     * @param routeID
     * @param trip
     * @param parser
     * @return
     */
    public static boolean trainARPModelOnFly(String routeID, Vector<Stop> trip, AIOFParser parser){
        String target = "AR";
        boolean scale = true;
        svm_problem[] problems = parser.getProbs_AR();
        int N = problems.length;
        PredictorSVM pred;
        for(int i = 0; i < N; i ++) {
            String modelFileName = getDemandModelFileName_X(target, routeID, i);
            String xxxFileName = getDemandXXXFileName_X(target, routeID, i);
            pred = (PredictorSVM)PredictorFactory.getPredictor("SVM");
            pred.setModelFileName(modelFileName);
            boolean res = pred.trainOnFly(problems[i], scale, 10, true, xxxFileName);
            if(res == false){
                return false;
            }
        }
        return true;
    }

    /**
     * 训练文件不落地模式，训练下客率预测模型
     * @param routeID
     * @param trip
     * @param parser
     * @return
     */
    public static boolean trainALPModelOnFly(String routeID, Vector<Stop> trip, AIOFParser parser){
        String target = "AL";
        boolean scale = true;
        svm_problem[] problems = parser.getProbs_AL();
        int N = problems.length;
        PredictorSVM pred;
        for(int i = 0; i < N; i ++) {
            String modelFileName = getDemandModelFileName_X(target, routeID, i);
            String xxxFileName = getDemandXXXFileName_X(target, routeID, i);
            pred = (PredictorSVM)PredictorFactory.getPredictor("SVM");
            pred.setModelFileName(modelFileName);
            boolean res = pred.trainOnFly(problems[i], scale, 10, true, xxxFileName);
            if(res == false){
                return false;
            }
        }
        return true;
    }

    /**
     * 训练客源到达率预测模型
     * @param routeID
     * @param trip
     * @return
     */
    public static boolean trainARPModel(String routeID, Vector<Stop> trip) {
        String target = "AR";
        Predictor pred;
        boolean reformatInputFile = true;
        boolean scale = true;
        int N = trip.size();
        for(int i = 1; i < N; i ++){
            Stop s = trip.get(i);
            int id = i;

            String trainingInputFileName = getDemandTrainingFileName(routeID, id, target);
            if(!reformatInputFile){
                trainingInputFileName = trainingInputFileName + ".svm";
            }
            String modelFileName = getDemandModelFileName(routeID, id, target);
            File f1 = new File(trainingInputFileName);
            if(f1.exists() && !f1.isDirectory()) {
                pred = PredictorFactory.getPredictor("SVM");
                pred.setModelFileName(modelFileName);
                return pred.train(trainingInputFileName, reformatInputFile, scale, 10, true);
            }
            else{
                return false;
            }
        }

        return false;
    }

    /**
     * 训练下客率预测模型
     * @param routeID
     * @param trip
     * @return
     */
    public static boolean trainALPModel(String routeID, Vector<Stop> trip) {
        String target = "AL";
        Predictor pred;
        boolean reformatInputFile = true;
        boolean scale = true;
        int N = trip.size();
        for(int i = 1; i < N; i ++){
            Stop s = trip.get(i);
            int id = i;

            File f0 = new File(Config.tts_trainDir + routeID);
            File fm = new File(Config.tts_modelDir + routeID);
            if(!f0.exists()) {
                createDir(f0);
            }
            if(!fm.exists()) {
                createDir(fm);
            }
            String trainingInputFileName = getDemandTrainingFileName(routeID, id, target);
            if(!reformatInputFile){
                trainingInputFileName = trainingInputFileName + ".svm";
            }
            String modelFileName = getDemandModelFileName(routeID, id, target);
            File f1 = new File(trainingInputFileName);

            if(f1.exists() && !f1.isDirectory()) {
                pred = PredictorFactory.getPredictor("SVM");
                pred.setModelFileName(modelFileName);
                return pred.train(trainingInputFileName, reformatInputFile, scale, 10, true);
            }
            else{
                return false;
            }
        }

        return false;
    }

    /**
     * 预测全程行驶时间
     * @param depTime
     * @param pred
     * @param model
     * @param prep
     * @param stats
     * @return
     * @throws ParseException
     * @throws IOException
     */
    private static double predictTTS_full(Calendar depTime, Predictor pred, svm_model model,
                                             Preprocess prep, StatsDataParser stats) throws ParseException, IOException{
        int dow = Utils.getDayOfWeek(depTime);
        int interval = Utils.getIntervalIndex(depTime, intervalDur);

        TestSet testset = new TestSet();
        double ttsStats = stats.getStatsForSegmentAtInterval(dow, interval);
        // Build test set
        testset.build_TTS(depTime, dow, ttsStats, Double.NaN);
        // Scale test set
        testset.setTestSet(prep.scale(testset.getTestSet()));
        double tts = pred.predict(model, testset);

        return Utils.format(tts);
    }

    /**
     * 预测客源到达率
     * @param depTime
     * @param stopId
     * @param pred
     * @param model
     * @param prep
     * @param stats
     * @return
     * @throws ParseException
     * @throws IOException
     */
    private static double predictArrivalRate(Calendar depTime, int stopId, Predictor pred, svm_model model,
                                             Preprocess prep, StatsDataParser stats) throws ParseException, IOException{
        int dow = Utils.getDayOfWeek(depTime);
        int interval = Utils.getIntervalIndex(depTime, intervalDur);

        TestSet testset = new TestSet();
        double arStat = stats.getStatsForSegmentAtInterval(stopId, interval);
        // Build test set
        testset.build_AR(depTime, dow, arStat, Double.NaN);
        // Scale test set
        testset.setTestSet(prep.scale(testset.getTestSet()));
        double ar = pred.predict(model, testset);

        //System.out.println("Time:" + Utils.parseDateToToDStr(depTime) + ",stop:" + stopId + "arrival rate:" + ar);

        return Utils.format(ar);
    }

    /**
     * 预测下客率
     * @param depTime
     * @param stopId
     * @param pred
     * @param model
     * @param prep
     * @param stats
     * @return
     * @throws ParseException
     * @throws IOException
     */
    private static double predictAlightingRatio(Calendar depTime, int stopId, Predictor pred, svm_model model,
                                             Preprocess prep, StatsDataParser stats) throws ParseException, IOException{
        int dow = Utils.getDayOfWeek(depTime);
        int interval = Utils.getIntervalIndex(depTime, intervalDur);

        TestSet testset = new TestSet();
        double alStat = stats.getStatsForSegmentAtInterval(stopId, interval);
        // Build test set
        testset.build_AL(depTime, dow, alStat, Double.NaN);
        // Scale test set
        testset.setTestSet(prep.scale(testset.getTestSet()));
        double al = pred.predict(model, testset);

        //System.out.println("Time:" + Utils.parseDateToToDStr(depTime) + ",stop:" + stopId + "alighting rate:" + al);

        return Utils.format(al);
    }

    /**
     * 预测行驶时间、客源到达率，下客率
     * @param routeID
     * @param nouse
     * @param trip
     * @return
     */
    public static Vector<Stop> updateTrip(String routeID, Calendar nouse, Vector<Stop> trip, Session ses, boolean usePFPModels) {
        int N = trip.size();
        Vector<Stop> newTrip = new Vector<>(N);
        newTrip.add(0, trip.get(0));
        try {
            //long t0 = Calendar.getInstance().getTime().getTime();
            //long t1,t2,t3,t4=0;
            Predictor pred = PredictorFactory.getPredictor("SVM");
            Calendar depTime = trip.get(0).getDepartureTime();
            int dow = Utils.getDayOfWeek(depTime);

            // 从session中提取模型和数据容器
            /*
            // --- 预测行驶时间 ---
            svm_model[] tts_models = (svm_model[])ses.get("models-tts-" + routeID);
            Preprocess[] tts_preps = (Preprocess[])ses.get("xxx-tts-" + routeID);
            RequestHandler rhTTS = (RequestHandler)ses.get("stats-tts-" + routeID);
            RequestHandler rhTTS_last30min = (RequestHandler)ses.get("stats_Last30Min-tts-" + routeID);
            */
            svm_model[] models_AR = null;
            Preprocess[] preps_AR = null;
            StatsDataParser[] parsers_AR = null;
            svm_model[] models_AL = null;
            Preprocess[] preps_AL = null;
            StatsDataParser[] parsers_AL = null;
            if(usePFPModels == true) {
                // --- 预测客源到达率 ---
                models_AR = (svm_model[]) ses.get("models_AR." + routeID);
                preps_AR = (Preprocess[]) ses.get("scalers_AR." + routeID);
                parsers_AR = (StatsDataParser[]) ses.get("stats_AR." + routeID);

                // --- 预测下客比率 ---
                models_AL = (svm_model[]) ses.get("models_AL." + routeID);
                preps_AL = (Preprocess[]) ses.get("scalers_AL." + routeID);
                parsers_AL = (StatsDataParser[]) ses.get("stats_AL." + routeID);
            }
            else{
                parsers_AR = (StatsDataParser[]) ses.get("stats_AR." + routeID);
                parsers_AL = (StatsDataParser[]) ses.get("stats_AL." + routeID);
            }

            // 预测并更新stop相关字段
            Calendar _depTime = null;
            for (int i = 1; i < N; i++) {
                _depTime = (Calendar) depTime.clone();//!
                Stop s = trip.get(i);
                int idFrom = 0;
                int idTo = i;

                /*
                // Step 1 >>> 预测行驶时间
                //t1 = Calendar.getInstance().getTime().getTime();
                int predTTSInSec = predictTTS(_depTime, idTo, pred,
                                                rhTTS, rhTTS_last30min, tts_models[i-1], tts_preps[i-1]);
                if(predTTSInSec != -1) {
                    _depTime.add(Calendar.SECOND, predTTSInSec);
                    s.setDepartureTime(_depTime);
                }
                else{
                    System.exit(1);
                }
                //t2 = Calendar.getInstance().getTime().getTime();
                //System.out.println("Time for TTS prediction:" + (t2-t1) + "sec");
                */

                // Step 2 >>> 预测会到达率
                _depTime = (Calendar) depTime.clone();
                double arrivalRate = Double.NaN;
                if(usePFPModels == true) {
                    arrivalRate = predictArrivalRate(_depTime, idTo, pred, models_AR[i], preps_AR[i], parsers_AR[dow]);
                }
                else{
                    int interval = Utils.getIntervalIndex(_depTime, intervalDur);
                    arrivalRate = parsers_AR[dow].getStatsForSegmentAtInterval(idTo, interval);
                }
                if (arrivalRate != Double.NaN && arrivalRate != -1) {
                    s.setArrivalRate(arrivalRate);
                } else {
                    System.exit(1);
                }
                //t3 = Calendar.getInstance().getTime().getTime();
                //System.out.println("Time for TTS prediction:" + (t3-t2) + "sec");

                // Step 3 >>> 预测下客比率
                _depTime = (Calendar) depTime.clone();
                double alightingRatio = Double.NaN;
                if(usePFPModels == true) {
                    alightingRatio = predictArrivalRate(_depTime, idTo, pred, models_AL[i], preps_AL[i], parsers_AL[dow]);
                }
                else {
                    int interval = Utils.getIntervalIndex(_depTime, intervalDur);
                    alightingRatio = parsers_AL[dow].getStatsForSegmentAtInterval(idTo, interval);
                }
                if (alightingRatio != Double.NaN && alightingRatio != -1) {
                    s.setAlightingRate(alightingRatio);
                } else {
                    System.exit(1);
                }

                // 更新字段
                newTrip.add(i, s);
                //t4 = Calendar.getInstance().getTime().getTime();
                //System.out.println("Time for TTS prediction:" + (t4-t3) + "sec");
            }
            //System.out.println("Time for all prediction:" + (t4-t0) + "sec");
        }
        catch(Exception e){
            System.exit(1);
        }

        return newTrip;
    }

    /**
     * 预测全程行驶时间
     * @param routeID
     * @param depTime
     * @param ses
     * @param usePrediction
     * @return
     */
    public static double getTravelTime(String routeID, Calendar depTime, Session ses, boolean usePrediction){
        double tts = Double.NaN;
        try {
            Predictor pred = PredictorFactory.getPredictor("SVM");

            // 从session中提取模型和数据容器
            svm_model model_TTS_full = (svm_model) ses.get("model_TTS_full." + routeID);
            Preprocess prep_TTS_full = (Preprocess) ses.get("scaler_TTS_full." + routeID);
            StatsDataParser parsers_TTS_full = (StatsDataParser) ses.get("stats_TTS_full." + routeID);


            if (usePrediction) {
                tts = predictTTS_full(depTime, pred, model_TTS_full, prep_TTS_full, parsers_TTS_full);
            } else {
                tts = parsers_TTS_full.getStatsForSegmentAtInterval(Utils.getDayOfWeek(depTime),
                                                                    Utils.getIntervalIndex(depTime, intervalDur));
            }
        }
        catch(Exception e){
            System.out.println(e);
            System.exit(1);
        }

        //System.out.println("Time:" + Utils.parseDateToToDStr(depTime) + ",full tts:" + tts);
        return tts;
    }

    public static double getStats_TTS_k2K_(String routeID, int stopFrom, int stopTo, Calendar depTime, Session ses){
        double ttsStats = Double.NaN;
        try {
            int dow = Utils.getDayOfWeek(depTime);
            int interval = Utils.getIntervalIndex(depTime, intervalDur);
            StatsDataParser parser_TTS_k2K = ((StatsDataParser[]) ses.get("stats_TTS_k2K." + routeID))[dow];
            ttsStats = parser_TTS_k2K.getStatsForSegmentAtInterval(stopTo - 1, interval);
        }
        catch(ParseException pe){
            System.out.println(pe);
            System.exit(1);
        }
        //System.out.println("Time:" + Utils.parseDateToToDStr(depTime) + ",stop:" + stopFrom + "-" + stopTo + ", tts:" + ttsStats);
        return ttsStats;
    }
    /**
     * 创建session object
     * @return
     */
    public static Session createSession(){
        return new Session();
    }

    /**
     * 将训练好的模型，以及数据装载器存到session object里。这样做可以节省IO开销。
     * 请注意！不要在session中加载太多route的数据，如果不再使用了，请使用ses.remove(<K>)清除不需要的数据.
     * @param routeID
     * @param stopIDs
     * @param ses
     * @param loadPFPModel: 是否装载客流预测模型
     */
    public static void loadModels(String routeID, int[] stopIDs, Session ses, boolean loadPFPModel){
        int N = stopIDs.length;
        int intervalDur = 15; // min
        int intervalN = 24 * (60 / intervalDur);
        int skipCol = 1;

        int stopID;

        // 初始化预测模型容器
        svm_model model_TTS_full = null;
        svm_model[] models_AR = null;
        svm_model[] models_AL = null;

        // 初始化数据标准化处理器容器
        Preprocess prep_TTS_full = null;
        Preprocess[] prep_AR = null;
        Preprocess[] prep_AL = null;

        // 装载k>k+1 行驶时间统计数据， 按天
        // 装载 k站 客源到达率、下车比率 统计数据
        int dayN = 7;
        StatsDataParser[] parsers_TTS_k2K = new StatsDataParser[dayN];
        StatsDataParser[] parsers_AR = new StatsDataParser[dayN];
        StatsDataParser[] parsers_AL = new StatsDataParser[dayN];
        String statsFileName_TTS_k2K = null;
        String statsFileName_AR = null;
        String statsFileName_AL = null;
        for(int i = 0; i < dayN; i ++){
            // for k->k+1 tts
            statsFileName_TTS_k2K = Config.x_tts_statsDir + routeID + "\\" + Config.x_tts_k2K_prefix
                                     + Integer.toString(i) + ".csv";
            parsers_TTS_k2K[i] = new StatsDataParser(statsFileName_TTS_k2K, skipCol, intervalN);

            // for AR
            statsFileName_AR = Config.x_arrival_statsDir + routeID + "\\" + Integer.toString(i) + ".csv";
            parsers_AR[i] = new StatsDataParser(statsFileName_AR, skipCol, intervalN);

            // for AL
            statsFileName_AL = Config.x_alighting_statsDir + routeID + "\\" + Integer.toString(i) + ".csv";
            parsers_AL[i] = new StatsDataParser(statsFileName_AL, skipCol, intervalN);
        }

        // 装载全程行驶时间统计数据
        String statsFileName_TTS_full = Config.x_tts_statsDir + routeID + "\\" + Config.x_tts_full_prefix + ".csv";
        StatsDataParser parser_TTS_full = new StatsDataParser(statsFileName_TTS_full, skipCol, intervalN);

        // 装载全程行驶时间预测模型
        String modelFileName_TTS_full = Config.x_tts_modelDir + routeID + "\\full.mdl";
        String xxxFileName_TTS_full = Config.x_tts_modelDir + routeID + "\\full.xxx";
        try {
            model_TTS_full = PredictorFactory.getPredictor("SVM").getModelFromFile(modelFileName_TTS_full);
            prep_TTS_full = new Preprocess(-1, 1, xxxFileName_TTS_full);
        }
        catch(Exception e){
            System.out.println(e);
            System.exit(1);
        }

        if(loadPFPModel == true) {
            // 装载客源到达率、 下客率 预测模型
            models_AR = new svm_model[N];
            models_AL = new svm_model[N];
            prep_AR = new Preprocess[N];
            prep_AL = new Preprocess[N];
            String modelFileName_AR;
            String xxxFileName_AR;
            String modelFileName_AL;
            String xxxFileName_AL;
            try {
                for (int i = 0; i < N; i++) {
                    // for AR
                    modelFileName_AR = Config.x_arrival_modelDir + routeID + "\\" + Integer.toString(i) + ".mdl";
                    xxxFileName_AR = Config.x_arrival_modelDir + routeID + "\\" + Integer.toString(i) + ".xxx";
                    models_AR[i] = PredictorFactory.getPredictor("SVM").getModelFromFile(modelFileName_AR);
                    prep_AR[i] = new Preprocess(-1, 1, xxxFileName_AR);

                    // for AL
                    modelFileName_AL = Config.x_alighting_modelDir + routeID + "\\" + Integer.toString(i) + ".mdl";
                    xxxFileName_AL = Config.x_alighting_modelDir + routeID + "\\" + Integer.toString(i) + ".xxx";
                    models_AL[i] = PredictorFactory.getPredictor("SVM").getModelFromFile(modelFileName_AL);
                    prep_AL[i] = new Preprocess(-1, 1, xxxFileName_AL);
                }
            } catch (Exception e) {
                System.out.println(e);
                System.exit(1);
            }
        }

        // 将模型存储到session object中
        ses.add("stats_TTS_k2K." + routeID, parsers_TTS_k2K);
        ses.add("stats_TTS_full." + routeID, parser_TTS_full);

        ses.add("model_TTS_full." + routeID, model_TTS_full);
        ses.add("scaler_TTS_full." + routeID, prep_TTS_full);

        ses.add("stats_AR." + routeID, parsers_AR);
        ses.add("stats_AL." + routeID, parsers_AL);

        if(loadPFPModel == true) {
            ses.add("models_AR." + routeID, models_AR);
            ses.add("scalers_AR." + routeID, prep_AR);

            ses.add("models_AL." + routeID, models_AL);
            ses.add("scalers_AL." + routeID, prep_AL);
        }
    }
}
