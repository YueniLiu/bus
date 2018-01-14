package plannist.predictor;

import ennuste.common.Utils;
import libsvm.svm_node;
import libsvm.svm_problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.StringTokenizer;

public class DataSetParser {
    private String fileName;
    private int stopFeatureDim;
    private int stopCount;
    private int size;
    private svm_problem[] probs_tts;
    private svm_problem[] probs_ar;
    private svm_problem[] probs_al;

    public DataSetParser(String fileName, int stopFeatureDim) throws IOException, ParseException {
        this.fileName = fileName;
        this.stopFeatureDim = stopFeatureDim;
        parse();
    }

    private double[] getStatsForTTS(int routeID, Calendar depTime, int stopID) throws ParseException{
        RequestHandler rhTTS = new RequestHandler();
        String tts_statsFileName = Config.x_tts_statsDir + routeID + "\\tts.csv";
        rhTTS.loadDataFromFile(tts_statsFileName, 3);

        RequestHandler rhTTS_last30min = new RequestHandler();
        String tts_last30minFileName = Config.x_tts_statsDir + routeID + "\\last30min.csv";
        rhTTS_last30min.loadDataFromFile(tts_last30minFileName, 1);

        double stats = rhTTS.getStatsAtOneStop(depTime, 5, stopID);
        // 0 - mean, 1 - median, 2 - 90th percentile
        double stats_earlyTerm = rhTTS_last30min.getStatsAtOneStop(0, stopID);

        return new double[]{stats, stats_earlyTerm};
    }

    private double[] getStatsForAR(int routeID, Calendar depTime, int stopID) throws ParseException{
        RequestHandler rhAR= new RequestHandler();
        String arrival_statsFileName = Config.x_arrival_statsDir + routeID + "\\rates.csv";
        rhAR.loadDataFromFile(arrival_statsFileName, 3);

        RequestHandler rhAR_last30min= new RequestHandler();
        String arrival_last30minFileName = Config.x_arrival_statsDir + routeID + "\\last30min.csv";
        rhAR_last30min.loadDataFromFile(arrival_last30minFileName, 1);

        double stats = rhAR.getStatsAtOneStop(depTime, 5, stopID);
        // 0 - mean, 1 - median, 2 - 90th percentile
        double stats_earlyTerm = rhAR_last30min.getStatsAtOneStop(0, stopID);

        return new double[]{stats, stats_earlyTerm};
    }

    private double[] getStatsForAL(int routeID, Calendar depTime, int stopID) throws ParseException{
        RequestHandler rhAL = new RequestHandler();
        String alighting_statsFileName = Config.x_alighting_statsDir + routeID + "\\rates.csv";
        rhAL.loadDataFromFile(alighting_statsFileName, 3);

        RequestHandler rhAL_last30min = new RequestHandler();
        String alighting__last30minFileName = Config.x_alighting_statsDir + routeID + "\\last30min.csv";
        rhAL_last30min.loadDataFromFile(alighting__last30minFileName, 1);

        double stats = rhAL.getStatsAtOneStop(depTime, 5, stopID);
        // 0 - mean, 1 - median, 2 - 90th percentile
        double stats_earlyTerm = rhAL_last30min.getStatsAtOneStop(0, stopID);

        return new double[]{stats, stats_earlyTerm};
    }

    private void initProblems(){
        // for tts
        probs_tts = new svm_problem[stopCount];
        for(int i = 0; i < stopCount; i ++){
            probs_tts[i] = new svm_problem();
            probs_tts[i].l = size;
            probs_tts[i].x = new svm_node[size][];
            probs_tts[i].y = new double[size];
        }

        // ar
        probs_ar = new svm_problem[stopCount];
        for(int i = 0; i < stopCount; i ++){
            probs_ar[i] = new svm_problem();
            probs_ar[i].l = size;
            probs_ar[i].x = new svm_node[size][];
            probs_ar[i].y = new double[size];
        }

        // al
        probs_al = new svm_problem[stopCount];
        for(int i = 0; i < stopCount; i ++){
            probs_al[i] = new svm_problem();
            probs_al[i].l = size;
            probs_al[i].x = new svm_node[size][];
            probs_al[i].y = new double[size];
        }
    }

    private svm_node[] parse_x(Calendar depTime, double stats, double stats_earlyTerm){
        svm_node[] x = new svm_node[stopFeatureDim];

        // dep time
        x[0] = new svm_node();
        x[0].index = 0;
        x[0].value = Utils.parseDateToToD(depTime);

        // dep day-of-week (capture weekly pattern)
        x[1] = new svm_node();
        x[1].index = 1;
        x[1].value = depTime.get(Calendar.DAY_OF_WEEK);

        // dep month (capture seasonal pattern)
        //this.x[2] = new svm_node();
        //this.x[2].index = 2;
        //this.x[2].value = depTime.get(Calendar.MONTH) + 1;

        // dep year (capture yearly diff)
        //this.x[3] = new svm_node();
        //this.x[3].index = 5;
        //this.x[3].value = depTime.get(Calendar.YEAR);

        // current day last 30min tts stats
        x[2] = new svm_node();
        x[2].index = 2;
        x[2].value = stats;

        // historical tts stats
        x[3] = new svm_node();
        x[3].index = 3;
        x[3].value = stats_earlyTerm;

        return x;
    }

    private void parse() throws IOException, ParseException{
        BufferedReader xr = new BufferedReader(new FileReader(fileName));
        size = (int)xr.lines().count() - 1; // skip header
        xr.close();

        xr = new BufferedReader(new FileReader(fileName));
        xr.readLine(); // skip the first line
        int idx = 0;
        while (true) {
            String line = xr.readLine();

            if (line == null) {
                break;
            }

            StringTokenizer st = new StringTokenizer(line, ",");
            int tripID = Integer.parseInt(st.nextToken());
            int routeID = Integer.parseInt(st.nextToken());
            int busID = Integer.parseInt(st.nextToken());
            String startDate = st.nextToken();
            String startTime = st.nextToken();
            int week = Integer.parseInt(st.nextToken());
            int month = Integer.parseInt(st.nextToken());
            int year = Integer.parseInt(st.nextToken());
            String endTime = st.nextToken();

            // Format start and end datetime
            Calendar startDT = Utils.parseDateStrtoDate(startDate + " " + startTime);
            Calendar endDT = Utils.parseDateStrtoDate(startDate + " " + endTime);

            // Get feature-friendly start time and travel time(in seconds)
            double start_time = Utils.parseDateToToD(startDT);
            double tts = (endDT.getTime().getTime() - startDT.getTime().getTime()) / 1000;

            // Now, extract the information for each stop
            int n = st.countTokens();
            int stopN = n / stopFeatureDim;
            stopCount = stopN;

            if(probs_tts == null || probs_ar == null || probs_al == null){
                initProblems();
            }

            for(int i = 0; i < stopN; i ++){
                //Stop enter time
                String stopEnterTimeStr = startDate + " " + st.nextToken();
                double stopEnterTime = Utils.parseDateToToD(Utils.parseDateStrtoDate(stopEnterTimeStr));

                // Stop leave time
                String stopLeaveTimeStr = startDate + " " + st.nextToken();
                Calendar stopLeaveTime = Utils.parseDateStrtoDate(stopLeaveTimeStr);
                double stopTTS = (stopLeaveTime.getTime().getTime() - startDT.getTime().getTime()) / 1000;

                // get stats for tts
                double[] stats = getStatsForTTS(routeID, startDT, i+1);

                probs_tts[i].x[idx] = parse_x(startDT, stats[0], stats[1]);
                probs_tts[i].y[idx] = stopTTS;

                // Stop arrival rate
                double arrivalRate = Double.parseDouble(st.nextToken());

                // Get stats for arrival rate
                double[] stats1 = getStatsForAR(routeID, startDT, i+1);

                probs_ar[i].x[idx] = parse_x(startDT, stats1[0], stats1[1]);
                probs_ar[i].y[idx] = arrivalRate;

                // Stop alighting ratio
                double alightingRatio = Double.parseDouble(st.nextToken());

                // Get stats for alighting rate
                double[] stats2 = getStatsForAL(routeID, startDT, i+1);

                probs_al[i].x[idx] = parse_x(startDT, stats2[0], stats2[1]);
                probs_al[i].y[idx] = arrivalRate;
            }

            idx ++;
        }
        xr.close();
    }

    public svm_problem[] getProbs_tts(){
        return probs_tts;
    }

    public svm_problem[] getProbs_ar(){
        return probs_ar;
    }

    public svm_problem[] getProbs_al(){
        return probs_al;
    }

    private void printProblem(svm_problem prob){
        System.out.println("Observation Size:" + prob.l + ", Input Dimension:" + stopFeatureDim);
        System.out.println("(X,y)=[");
        int index = 0;
        for(svm_node[] xs: prob.x){
            double y = prob.y[index];
            for(int i = 0;i < Math.min(5, xs.length); i++){
                System.out.print("(" + xs[i].value + ")  ");
            }
            System.out.println(" <"+ y + ">");
            index += 1;
        }
        System.out.println("]");
    }

    private void printProblems(svm_problem[] probs){
        int N = probs.length;
        for(int i = 0; i < N; i ++){
            printProblem(probs[i]);
        }
    }

    public void printProblemsByName(String problemName){
        if (problemName.equals("tts")) {
            printProblems(probs_tts);
        }
        else if(problemName.equals("ar")){
            printProblems(probs_ar);
        }
        else if(problemName.equals("al")){
            printProblems(probs_al);
        }
    }

}
