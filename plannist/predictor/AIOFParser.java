package plannist.predictor;

import ennuste.common.Utils;
import libsvm.svm_node;
import libsvm.svm_problem;
import plannist.predictor.helper.DoWStatsHolder;
import plannist.predictor.helper.StatsDataParser;

import java.io.*;
import java.text.ParseException;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 *
 * Note:
 * Work flow description:
 *   step 1 - all-in-one large data file ready
 *
 *   step 2 - create problem for arrival-rate, alighting ratio, without loading their corresponding stats
 *
 *   step 3 - save stats to local file given interval (minute), num_of_interval=24*(60/interval):
 *            1) kth stop arrival-rate stats, one file per day [d!]
 *            2) kth stop alighting-ratio stats, one file per day [d!]
 *            3) kth->k+1th stop stats, one file per day, one route segment per row [d!]
 *            4) 0->last stop (full length trip) tts stats, one file per route
 *
 *   step 4 - find and update problem with stats for arrival-rate and alighting-ratio
 *
 *   At the end of the parsing process, the ready-for-train problems (ndarray) are generated, which can be retrieved
 *   using
 */

public class AIOFParser {
    private String fileName;
    private String routeID;
    private int stopFeatureDim;
    private int stopCount;
    private int intervalDurInMin;
    private int intervalN;
    private int size;
    private svm_problem probs_TTS_full;
    private svm_problem[] probs_AR;
    private svm_problem[] probs_AL;
    //private DoWStatsHolder[] holder_tts_02k;
    private DoWStatsHolder[] holder_AR;
    private DoWStatsHolder[] holder_AL;
    private DoWStatsHolder[] holder_TTS_k2K;
    private DoWStatsHolder holder_TTS_full;
    private final int statsIndex = 2;
    private final int trainFeatureDim = 3;

    public AIOFParser(String fileName, int stopFeatureDim, int intervalDurInMin) throws IOException, ParseException{
        this.fileName = fileName;
        this.stopFeatureDim = stopFeatureDim;
        this.intervalDurInMin = intervalDurInMin;
        this.intervalN = 24 * (60 / intervalDurInMin);
        long t0 = Calendar.getInstance().getTime().getTime();
        parse();
        saveStatsToFile_k();
        saveStatsToFile_k2K();
        saveStatsToFile_TTS_full();
        updateProblemWithStats();
        long t1 = Calendar.getInstance().getTime().getTime();
        System.out.println("Entire process completed! Total time used [" + (t1 - t0) / 1000.0 + "sec]");
    }

    /**
     * For tts, the index=0 holders the start yard data, the data will not be used
     */
    private void initProblems(){
        // init svm problems for tts full
        probs_TTS_full = new svm_problem();
        probs_TTS_full.l = size;
        probs_TTS_full.x = new svm_node[size][];
        probs_TTS_full.y = new double[size];

        // init svm problems for arrival rate and alighting ratio
        probs_AR = new svm_problem[stopCount];
        probs_AL = new svm_problem[stopCount];

        for(int i = 0; i < stopCount; i ++){
            // for tts
            //probs_tts[i] = new svm_problem();
            //probs_tts[i].l = size;
            //probs_tts[i].x = new svm_node[size][];
            //probs_tts[i].y = new double[size];

            // for AR
            probs_AR[i] = new svm_problem();
            probs_AR[i].l = size;
            probs_AR[i].x = new svm_node[size][];
            probs_AR[i].y = new double[size];

            // AL
            probs_AL[i] = new svm_problem();
            probs_AL[i].l = size;
            probs_AL[i].x = new svm_node[size][];
            probs_AL[i].y = new double[size];
        }
    }

    /**
     * For tts, the index=0 holders the start yard data, the data will not be used
     */
    private void initDowStatsHolders(){
        //holder_tts_02k = new DoWStatsHolder[stopCount];
        holder_AR = new DoWStatsHolder[stopCount];
        holder_AL = new DoWStatsHolder[stopCount];
        holder_TTS_k2K = new DoWStatsHolder[stopCount];
        for(int i = 0; i < stopCount; i ++){
            //holder_tts_02k[i] = new DoWStatsHolder(intervalDurInMin);
            holder_AR[i] = new DoWStatsHolder(intervalDurInMin);
            holder_AL[i] = new DoWStatsHolder(intervalDurInMin);
            holder_TTS_k2K[i] = new DoWStatsHolder(intervalDurInMin);
        }

        holder_TTS_full = new DoWStatsHolder(intervalDurInMin);
    }

    /**
     * Parse to training set
     * @param depTime
     * @param stats
     * @param stats_earlyTerm
     * @return
     */
    private svm_node[] parse_x(Calendar depTime, double dow, double stats, double stats_earlyTerm){
        svm_node[] x = new svm_node[trainFeatureDim];

        /*
        boolean isFirstDaySunday = (depTime.getFirstDayOfWeek() == Calendar.SUNDAY);
        int dow = depTime.get(Calendar.DAY_OF_WEEK);
        if(isFirstDaySunday){
            dow = dow - 1;
            if(dow == 0){
                dow = 7;
            }
        }

        dow -= 1;
        */

        // dep time
        x[0] = new svm_node();
        x[0].index = 0;
        //x[0].value = Utils.parseDateToToD(depTime);
        x[0].value = Utils.parseDateToHM(depTime);

        // dep day-of-week (capture weekly pattern) (monday 1 - sunday 7)
        x[1] = new svm_node();
        x[1].index = 1;
        x[1].value = dow;

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
        //x[3] = new svm_node();
        //x[3].index = 3;
        //x[3].value = stats_earlyTerm;

        return x;
    }

    /**
     * add stats to data holder (0->k)
     * @param target
     * @param stopIndex
     * @param day
     * @param interval
     * @param val
     */
    private void addToHolders_k(String target, int stopIndex, int day, int interval, double val){
        if(target.equals("AR")){
            holder_AR[stopIndex].add(day, interval, val);
            //System.out.println("[A]stop:" + stopIndex + ",day:" + day + ",int:" + interval + " added: " + val + " actual:" + holder_AR[stopIndex].get(day, interval));
        }
        else if(target.equals("AL")){
            holder_AL[stopIndex].add(day, interval, val);
            //System.out.println(interval + " added: " + val + " actual:" + holder_AL[stopIndex].get(day, interval));
        }
    }

    /**
     * add stats to data holder (k->k+1)
     * @param stopIndex
     * @param day
     * @param interval
     * @param val
     */
    private void addToHolders_TTS_k2K(int stopIndex, int day, int interval, double val){
        holder_TTS_k2K[stopIndex].add(day, interval, val);
    }

    /**
     * add stats to data holder (full length trip)
     * @param day
     * @param interval
     * @param val
     */
    private void addToHolder_TTS_full(int day, int interval, double val){
        holder_TTS_full.add(day, interval, val);
    }

    /**
     * 1st round parse: parse data to training set without stats value
     * @throws IOException
     * @throws ParseException
     */
    private void parse() throws IOException, ParseException {
        System.out.println("Start parsing file" + fileName + "-------------->");
        long t0 = Calendar.getInstance().getTime().getTime();
        //???
        BufferedReader xr = new BufferedReader(new FileReader(fileName));
        size = (int) xr.lines().count() - 1; // skip header
        xr.close();

        xr = new BufferedReader(new FileReader(fileName));
        xr.readLine(); // skip the header line
        int idx = 0;
        while (true) {
            String line = xr.readLine();

            if (line == null) {
                break;
            }

            StringTokenizer st = new StringTokenizer(line, ",");
            //String index0 = st.nextToken(); // remove it for product version
            String index = st.nextToken();
            routeID = st.nextToken();
            int busID = Integer.parseInt(st.nextToken());
            String startDate = st.nextToken();
            String startTime = st.nextToken(); //??? has problem
            int dow = Integer.parseInt(st.nextToken()) - 1;
            int month = Integer.parseInt(st.nextToken());
            int year = Integer.parseInt(st.nextToken());
            String endTime = st.nextToken();
            Calendar startDT = null;
            Calendar endDT = null;
            int interval = -1;

            // Get feature-friendly start time and travel time(in seconds)
            //double start_time = Utils.parseDateToToD(startDT);

            // Now, extract the information for each stop
            int n = st.countTokens();
            int stopN = n / stopFeatureDim;
            stopCount = stopN;

            if (probs_AR == null || probs_AL == null) {
                initProblems();
                initDowStatsHolders();
            }

            Calendar lastStopLeaveTime = null;
            for (int i = 0; i < stopN; i++) {
                //Stop enter time
                String stopEnterTimeStr = startDate + " " + st.nextToken();
                double stopEnterTime = Utils.parseDateToToD(Utils.parseDateStrtoDate(stopEnterTimeStr));
                // temp save stop enter time, last one will be the trip end time
                endDT = Utils.parseDateStrtoDate(stopEnterTimeStr);

                // Stop leave time
                String stopLeaveTimeStr = startDate + " " + st.nextToken();
                Calendar stopLeaveTime = Utils.parseDateStrtoDate(stopLeaveTimeStr);

                if(startDT == null){
                    // the leave time of the first stop is the trip start time
                    startDT = Utils.parseDateStrtoDate(stopLeaveTimeStr);
                    interval = Utils.getIntervalIndex(startDT, intervalDurInMin);
                }
                //System.out.println(Utils.parseDateToHM(startDT) + ":" + interval);

                // Dwell time
                Double dwellTimeStr = Double.parseDouble(st.nextToken());

                // get 0->kth stop tts
                //double stopTTS_02k = (stopLeaveTime.getTime().getTime() - startDT.getTime().getTime()) / 1000;

                // get k->k+1 stop tts
                double stopTTS_k2K = 0.0;
                if (lastStopLeaveTime == null) {
                    stopTTS_k2K = (stopLeaveTime.getTime().getTime() - startDT.getTime().getTime()) / 1000;
                } else {
                    stopTTS_k2K = (stopLeaveTime.getTime().getTime() - lastStopLeaveTime.getTime().getTime()) / 1000;
                }

                addToHolders_TTS_k2K(i, dow, interval, stopTTS_k2K);
                //addToHolders_k("tts", i, dow, interval, stopTTS_02k);
                //probs_tts[i].x[idx] = parse_x(startDT, Double.NaN, Double.NaN);
                //probs_tts[i].y[idx] = stopTTS_02k;

                // get kth stop arrival rate
                double arrivalRate = Double.parseDouble(st.nextToken());
                //System.out.println(Utils.parseDateToHM(startDT) + " Stop:" + i + ",Day:" + dow + ", interval:" + interval + ",ar=" + arrivalRate);
                addToHolders_k("AR", i, dow, interval, arrivalRate);
                probs_AR[i].x[idx] = parse_x(startDT, dow, interval, Double.NaN);
                probs_AR[i].y[idx] = arrivalRate;

                // get kth stop alighting ratio
                double alightingRatio = Double.parseDouble(st.nextToken());
                addToHolders_k("AL", i, dow, interval, alightingRatio);
                probs_AL[i].x[idx] = parse_x(startDT, dow, interval, Double.NaN);
                probs_AL[i].y[idx] = alightingRatio;

                //lastStopLeaveTime = stopLeaveTime;
            }

            double tts_full = (endDT.getTime().getTime() - startDT.getTime().getTime()) / 1000;
            addToHolder_TTS_full(dow, interval, tts_full);
            probs_TTS_full.x[idx] = parse_x(startDT, dow, interval, Double.NaN);
            probs_TTS_full.y[idx] = tts_full;

            /*System.out.println("Day:" + dow + ",interval:" + interval + " - parse row [" + idx + "] - Found stops:" + stopN
                    + ", start time: " + Utils.parseDateToToDStr(startDT)
                    + ", end time: " + Utils.parseDateToToDStr(endDT)
                    + ", travel time: " + tts_full); */

            idx++;
        }
        xr.close();
        long t1 = Calendar.getInstance().getTime().getTime();
        System.out.println("Parsing end! [" + (t1 - t0) / 1000.0 + "sec] <--------------\n");
    }

    /**
     *
     * @param target
     * @param day
     * @return
     */
    private String getStatsFileName(String target, String day){
        if (target.equals("AR")) {
            return Config.x_arrival_statsDir + routeID + "\\" + day + ".csv";
        } else if (target.equals("AL")) {
            return Config.x_alighting_statsDir + routeID + "\\" + day + ".csv";
        }
        else if(target.equals("TTS.k2K")){
            return Config.x_tts_statsDir + routeID + "\\" + Config.x_tts_k2K_prefix + day + ".csv";
        }
        else if(target.equals("TTS.full")){
            return Config.x_tts_statsDir + routeID + "\\" + Config.x_tts_full_prefix + ".csv";
        }
        return null;
    }

    private void saveStatsToFile_k() throws IOException{
        System.out.print("Saving arrival-rates and alighting ratio stats files ......");
        long t0 = Calendar.getInstance().getTime().getTime();
        int dayN = 7;
        String statsFileName = null;
        BufferedWriter writer;

        for(int i = 0; i < dayN; i ++){

            /* --- do not remove! ---
            // for tts
            statsFileName = getStatsFileName("02k", "tts", Integer.toString(i));
            writer = new BufferedWriter(new FileWriter(statsFileName));
            for(int j = 0; j < stopCount; j ++){
                holder = holder_tts_02k[j];
                StringBuffer line = new StringBuffer();
                line.append(j).append(",");
                for(int k = 0; k < intervalN; k ++){
                    // get tts stats
                    double val = Utils.computePercentile(holder.get(i, k), 0.8);
                    line.append(val).append(",");
                }
                writer.write(line.toString().substring(0, line.length()-1) + "\n");
            }
            writer.close();
            */

            // for ar
            statsFileName = getStatsFileName("AR", Integer.toString(i));
            writer = new BufferedWriter(new FileWriter(statsFileName));
            for(int j = 0; j < stopCount; j ++){
                StringBuffer line = new StringBuffer();
                line.append("stop" + j).append(",");
                for(int k = 0; k < intervalN; k ++){
                    // get tts stats
                    double val = Utils.computePercentile(holder_AR[j].get(i, k), Config.percentile_AR);
                    //System.out.println("Stop:" + j + ",day:" + (i) + ",int:" + k
                    //       +",size=" + holder_AR[j].get(i, k).size() + ",val=" + val);
                    line.append(val).append(",");
                }
                writer.write(line.toString().substring(0, line.length()-1) + "\n");
            }
            writer.flush();
            writer.close();

            // for al
            statsFileName = getStatsFileName("AL", Integer.toString(i));
            writer = new BufferedWriter(new FileWriter(statsFileName));
            for(int j = 0; j < stopCount; j ++){
                StringBuffer line = new StringBuffer();
                line.append("stop" + j).append(",");
                for(int k = 0; k < intervalN; k ++){
                    // get tts stats
                    double val = Utils.computePercentile(holder_AL[j].get(i, k), Config.percentile_AL);
                    line.append(val).append(",");
                }
                writer.write(line.toString().substring(0, line.length()-1) + "\n");
            }
            writer.flush();
            writer.close();
        }
        long t1 = Calendar.getInstance().getTime().getTime();
        System.out.println("Saved! [" + (t1 - t0) / 1000.0 + "sec]\n");
    }

    private void saveStatsToFile_k2K() throws IOException{
        System.out.print("Saving k to k+1 travel times stats files ......");
        long t0 = Calendar.getInstance().getTime().getTime();
        int dayN = 7;
        String statsFileName = null;
        DoWStatsHolder holder;
        BufferedWriter writer;

        for(int i = 0; i < dayN; i ++){
            // tts
            statsFileName = getStatsFileName("TTS.k2K", Integer.toString(i));
            writer = new BufferedWriter(new FileWriter(statsFileName));
            for(int j = 0; j < stopCount; j ++){
                holder = holder_TTS_k2K[j];
                StringBuffer line = new StringBuffer();
                line.append(j + "-" + (j+1)).append(",");
                for(int k = 0; k < intervalN; k ++){
                    // get tts stats
                    double val = Utils.computePercentile(holder.get(i, k), Config.percentile_TTS_full);
                    line.append(val).append(",");
                }
                writer.write(line.toString().substring(0, line.length()-1) + "\n");
            }
            writer.flush();
            writer.close();
        }
        long t1 = Calendar.getInstance().getTime().getTime();
        System.out.println("...... saved! [" + (t1 - t0) / 1000.0 + "sec]\n");
    }

    private void saveStatsToFile_TTS_full() throws IOException{
        System.out.print("Saving full length travel times stats files ......");
        long t0 = Calendar.getInstance().getTime().getTime();
        int dayN = 7;
        DoWStatsHolder holder = holder_TTS_full;
        String statsFileName = getStatsFileName("TTS.full", null);
        BufferedWriter writer = new BufferedWriter(new FileWriter(statsFileName));
        for(int i = 0; i < dayN; i ++){
            StringBuffer line = new StringBuffer();
            line.append("day " + i).append(",");
            for(int k = 0; k < intervalN; k ++){
                // get tts stats
                double val = Utils.computePercentile(holder.get(i, k), Config.percentile_TTS_full);
                line.append(val).append(",");
            }
            writer.write(line.toString().substring(0, line.length()-1) + "\n");
        }
        writer.close();
        long t1 = Calendar.getInstance().getTime().getTime();
        System.out.println("...... saved! [" + (t1 - t0) / 1000.0 + "sec]\n");
    }

    private double getStats(String statsFileName, int row, int col){
        StatsDataParser sdp = new StatsDataParser(statsFileName, 1, intervalN);
        return sdp.getStatsForSegmentAtInterval(row, col);
    }

    private void updateProblemWithStats(){
        System.out.print("Start updating stats in training set ......");
        long t0 = Calendar.getInstance().getTime().getTime();
        int stopN = probs_AR.length;

        // Update for AR and AL
        String statsFileName_AR;
        String statsFileName_AL;
        int day;
        for(int i = 0; i < stopN; i ++){
            int dataN = probs_AR[i].x.length;
            for(int j = 0; j < dataN; j ++){
                //System.out.print(probs_AR[i].x[j][0].value);
                day = (int)probs_AR[i].x[j][1].value;
                int in = (int)probs_AR[i].x[j][statsIndex].value;

                statsFileName_AR = getStatsFileName("AR", Integer.toString(day));
                //System.out.print(" Stop:" + i + ", day:" + day + ",interv:" + in + "[before: " + probs_AR[i].x[j][statsIndex].value);
                probs_AR[i].x[j][statsIndex].value = getStats(statsFileName_AR, i, in);
                //System.out.println(", after: " + probs_AR[i].x[j][statsIndex].value + "]");

                statsFileName_AL = getStatsFileName("AL", Integer.toString(day));
                //System.out.print("day " + day + " before: " + probs_AL[i].x[j][statsIndex].value);
                probs_AL[i].x[j][statsIndex].value = getStats(statsFileName_AL, i, (int)probs_AL[i].x[j][statsIndex].value);
                //System.out.println(", after: " + probs_AL[i].x[j][statsIndex].value);
            }
        }

        // --- update for tts full
        int N = probs_TTS_full.l;
        String statsFileName_TTS_full;
        int d = -1;
        for(int i = 0; i < N; i ++) {
            d = (int) probs_TTS_full.x[i][statsIndex - 1].value;
            statsFileName_TTS_full = getStatsFileName("TTS.full", null);
            //System.out.print("day " + d + " before: " + probs_TTS_full.x[i][statsIndex].value);
            probs_TTS_full.x[i][statsIndex].value = getStats(statsFileName_TTS_full, d,
                                                              (int) probs_TTS_full.x[i][statsIndex].value);
            //System.out.println(", after: " + probs_TTS_full.x[i][statsIndex].value);
        }

        long t1 = Calendar.getInstance().getTime().getTime();
        System.out.println("...... updating finished! [" + (t1 - t0) / 1000.0 + "sec]\n");
    }

    public svm_problem getProbs_TTS_full(){
        return probs_TTS_full;
    }

    public svm_problem[] getProbs_AR(){
        return probs_AR;
    }

    public svm_problem[] getProbs_AL(){
        return probs_AL;
    }

    public void printHolder(String target){
        DoWStatsHolder[] holder = null;
        if(target.equals("TTS.k2K")){
            holder = holder_TTS_k2K;
        }
        else if(target.equals("AR")){
            holder = holder_AR;
        }
        else if(target.equals("AL")){
            holder = holder_AL;
        }
        else holder = null;


        if(holder != null){
            int stopsN = 1;//holder.length;
            int iPD = 24 * (60 / intervalDurInMin);
            for(int i = 0; i < stopsN; i ++){
                System.out.println("Stop[" + i + "]:");
                int dayN = 7;
                DoWStatsHolder innerholders = holder[i];
                for(int j = 0; j < dayN; j ++){
                    System.out.print("   -day[" + j + "]:");
                    for(int k = 0; k < iPD; k ++){
                        ConcurrentSkipListMap<Integer, Double> data = innerholders.get(j, k);
                        double stat = Utils.computePercentile(data, 0.8);
                        if(!Double.toString(stat).equals("NaN")) {
                            int dataN = data.size();
                            System.out.print(k + ":" + stat + "(" + dataN +") ");
                        }
                        else{
                            System.out.print(k + ": ");
                        }

                    }
                    System.out.println();
                }
            }
        }
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

    public void printProblem_TTS_full(){
        printProblem(probs_TTS_full);
    }

    public void printProblem_AR(){
        printProblems(probs_AR);
    }

    public void printProblem_AL(){
        printProblems(probs_AL);
    }
}
