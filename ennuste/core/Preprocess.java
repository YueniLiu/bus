
package ennuste.core;

import ennuste.exception.InvalidParamException;
import libsvm.svm_node;
import libsvm.svm_problem;

import java.io.*;
import java.util.Formatter;
import java.util.StringTokenizer;

/**
 * Normalization and scaling
 *
 * Note: this class created based on Dr Chih-Jen Lin's Java code scale example "svm_scale.java",
 * which can be found on his github: 'github.com/cjlin1/libsvm/blob/master/java/'
 * Some bugs are fixed. Scale on target vector is not provided in this class for this version
 */
public class Preprocess {
    private double lower = -1.0;
    private double upper = 1.0;
    private int max_index;
    private String line = null;
    private double[] feature_max;
    private double[] feature_min;
    private double y_max = -Double.MAX_VALUE;
    private double y_min = Double.MAX_VALUE;
    private boolean retrain;
    private String xxxFileName;
    private long num_nonzeros = 0;
    private long new_num_nonzeros = 0;
    private svm_problem problem;

    public Preprocess(double lower, double upper, String xxxFileName) throws InvalidParamException{
        this.lower = lower;
        this.upper = upper;
        this.retrain = retrain;
        this.xxxFileName = xxxFileName;
        validateParams();
    }

    public void setProblem(svm_problem problem){
        this.problem = problem;
    }

    private boolean validateParams() throws InvalidParamException{
        StringBuffer sb = new StringBuffer();
        sb.append("Invalid parameter setting! ");
        if(this.lower >= this.upper){
            sb.append("Upper > lower is required!");
            throw new InvalidParamException(sb.toString());
        }
        if(xxxFileName == null || xxxFileName.length() == 0){
            sb.append("XXX file for storing mins and maxs is not given!");
            throw new InvalidParamException(sb.toString());
        }

        return true;
    }

    /**
     * Read a line from file
     * @param reader
     * @return
     * @throws IOException
     */
    private String readline(BufferedReader reader) throws IOException {
        line = reader.readLine();
        return line;
    }

    /**
     * Intialize holders for finding min and max
     *
     * @param reader
     * @throws IOException
     */
    private void initHolders(BufferedReader reader) throws IOException{
        int index;
        while(readline(reader) != null){
            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
            st.nextToken();
            while(st.hasMoreTokens()) {
                index = Integer.parseInt(st.nextToken());
                max_index = Math.max(max_index, index);
                st.nextToken();
                num_nonzeros++;
            }
        }

        try {
            feature_max = new double[(max_index+1)];
            feature_min = new double[(max_index+1)];
        } catch(OutOfMemoryError e) {
            System.err.println("can't allocate enough memory");
            System.exit(1);
        }

        reader.close();
    }

    private void initHoldersGivenProblem(){
        max_index = problem.x[0].length;

        try {
            feature_max = new double[(max_index+1)];
            feature_min = new double[(max_index+1)];
        } catch(OutOfMemoryError e) {
            System.err.println("can't allocate enough memory");
            System.exit(1);
        }
    }

    /**
     * Find min and max for D=(X, y), X is n-dim feature inputs and y is the target vector
     *
     * @param reader
     * @throws IOException
     */
    private void findXXX(BufferedReader reader) throws IOException{
        int index;

        for(int i=0;i<=max_index;i++) {
            feature_max[i] = -Double.MAX_VALUE;
            feature_min[i] = Double.MAX_VALUE;
        }

        while(readline(reader) != null){
            int next_index = 1;
            double target;
            double value;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
            target = Double.parseDouble(st.nextToken());
            y_max = Math.max(y_max, target);
            y_min = Math.min(y_min, target);

            while (st.hasMoreTokens()){
                index = Integer.parseInt(st.nextToken());
                value = Double.parseDouble(st.nextToken());

                for (int i = next_index; i<index; i++) {
                    feature_max[i] = Math.max(feature_max[i], 0);
                    feature_min[i] = Math.min(feature_min[i], 0);
                }

                feature_max[index] = Math.max(feature_max[index], value);
                feature_min[index] = Math.min(feature_min[index], value);
                next_index = index + 1;
            }

            for(int i=next_index;i<=max_index;i++) {
                feature_max[i] = Math.max(feature_max[i], 0);
                feature_min[i] = Math.min(feature_min[i], 0);
            }
        }

        reader.close();
    }

    /**
     * Find xxx given problem
     */
    private void findXXXGivenProblem(){
        int index;
        for(int i=0;i<max_index;i++) {
            feature_max[i] = -Double.MAX_VALUE;
            feature_min[i] = Double.MAX_VALUE;
        }

        svm_node[][] X = problem.x;
        double[] y = problem.y;
        int N = problem.l;
        int dimN;
        for(int i = 0; i < N; i ++){
            int next_index = 1;
            double target;
            double value;

            target = y[i];
            svm_node[] x = X[i];
            y_max = Math.max(y_max, target);
            y_min = Math.min(y_min, target);
            dimN = x.length;
            for(int j = 0; j < dimN; j ++){
                index = x[j].index;
                value = x[j].value;

                for (int k = next_index; k<index; k++) {
                    feature_max[k] = Math.max(feature_max[k], 0);
                    feature_min[k] = Math.min(feature_min[k], 0);
                }

                feature_max[index] = Math.max(feature_max[index], value);
                feature_min[index] = Math.min(feature_min[index], value);
                next_index = index + 1;
            }
        }
    }

    /**
     * Save or update xxx file with newly found mins and maxs
     * @throws IOException
     */
    private void saveXXXtoFile() throws IOException{
        // First time training
        BufferedWriter writer = new BufferedWriter(new FileWriter(xxxFileName));
        Formatter formatter = new Formatter(new StringBuilder());
        formatter.format("x\n");
        formatter.format("%.3f %.3f\n", lower, upper);
        for (int i = 0; i < max_index; i++) {
            //if (feature_min[i] != feature_max[i])
            formatter.format("%d %.3f %.3f\n", i, feature_min[i], feature_max[i]);
        }
        writer.write(formatter.toString());
        writer.close();
    }

    private void readXXXFile() throws FileNotFoundException, IOException{
        BufferedReader xr = new BufferedReader(new FileReader(xxxFileName));
        if(xr.read() == 'x') {
            xr.readLine();
            StringTokenizer st = new StringTokenizer(xr.readLine());
            lower = Double.parseDouble(st.nextToken());
            upper = Double.parseDouble(st.nextToken());
            String restore_line = null;
            int id;
            double min, max;
            while((restore_line = xr.readLine())!=null){
                StringTokenizer st2 = new StringTokenizer(restore_line);
                id = Integer.parseInt(st2.nextToken());
                min = Double.parseDouble(st2.nextToken());
                max = Double.parseDouble(st2.nextToken());
                if (id <= max_index){
                    feature_min[id] = min;
                    feature_max[id] = max;
                }
            }
        }
        xr.close();
    }

    /**
     * Transform original value to scaled one
     * this method should be called within scale(...)
     * @param index
     * @param value
     */
    private double transform(int index, double value) {
		/* skip single-valued attribute */
        if(feature_max[index] == feature_min[index])
            return value;

        if(value == feature_min[index]){
            value = lower;
        }
        else if(value == feature_max[index]) {
            value = upper;
        }
        else {
            value = lower + (upper - lower) *
                    (value - feature_min[index]) /
                    (feature_max[index] - feature_min[index]);
        }

        if(value != 0)
        {
            //System.out.print(index + ":" + value + " ");
            new_num_nonzeros++;
        }

        return value;
    }

    /**
     * Scale a data set (file based) to svm_node[][]
     * @param reader
     * @throws IOException
     */
    private void scaleAndSave(BufferedReader reader, String scaledFileName) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(scaledFileName));
        int index;
        String y;
        while(readline(reader) != null) {
            int next_index = 1;
            double value, scaledValue;

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            y = st.nextToken();
            writer.write(y + " ");

            while(st.hasMoreElements()) {
                index = Integer.parseInt(st.nextToken());
                value = Double.parseDouble(st.nextToken());
                for (int i = next_index; i<index; i++) {
                    transform(i, 0);
                }
                scaledValue = transform(index, value);
                //System.out.print(index + ":" + scaledValue + ",");
                writer.write(Integer.toString(index) + ":" + Double.toString(scaledValue) + " ");
                next_index = index + 1;
            }

            writer.write("\n");
            //System.out.print("\n");
        }
        if (new_num_nonzeros > num_nonzeros)
            System.err.print(
                    "WARNING: original #nonzeros " + num_nonzeros+"\n"
                            +"         new      #nonzeros " + new_num_nonzeros+"\n"
                            +"set lower=0 if many original feature values are zeros\n");

        reader.close();
        writer.close();

    }

    private svm_problem scaleOnFly(){
        svm_node[][] X = problem.x;
        double[] y = problem.y;
        int N = problem.l;
        int dimN =  max_index;

        // Init new problem
        svm_problem new_problem = new svm_problem();
        new_problem.l = dimN;
        new_problem.x = new svm_node[N][];
        new_problem.y = new double[N];

        int index;
        for(int i = 0; i < N; i ++) {
            new_problem.x[i] = new svm_node[max_index];
            int next_index = 1;
            double target;
            double value, scaledValue;

            target = y[i];
            svm_node[] x = X[i];
            for (int j = 0; j < x.length; j++) {
                index = x[j].index;
                value = x[j].value;
                for (int k = next_index; k < index; k++) {
                    transform(k, 0);
                }
                scaledValue = transform(index, value);
                // save to new problem
                new_problem.x[i][j] = new svm_node();
                new_problem.x[i][j].index = index;
                new_problem.x[i][j].value = scaledValue;
            }
            new_problem.y[i] = target;
        }

        return new_problem;
    }

    /**
     * Process 1: first train: find min+max -> save mins and maxs to xxx file ->scale the data
     *
     * Process 2: retrain (new data added):  find min+max -> update mins and maxs stored in xxx file -> rescale the data
     *
     * @param trainFileName
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void scaleForTrain(String trainFileName) throws FileNotFoundException, IOException{
        // Step 1 - Read training file and prepare feature_min, feature_max holders
        BufferedReader reader = new BufferedReader(new FileReader(trainFileName));
        initHolders(reader);

        // Step 2 - Find min and max for all the feature inputs and y
        reader = new BufferedReader(new FileReader(trainFileName));
        findXXX(reader);

        // Step 3 - Save (1st time) or update min and max
        saveXXXtoFile();

        // Step 4 - Scale
        reader = new BufferedReader(new FileReader(trainFileName));
        String scaledTrainFileName = trainFileName + ".scaled";
        scaleAndSave(reader, scaledTrainFileName);

    }

    public svm_problem scaleForTrainOnFly() throws IOException{
        initHoldersGivenProblem();
        findXXXGivenProblem();
        saveXXXtoFile();
        return scaleOnFly();
    }

    /**
     * Scale for one observation (function designed for per test prediction)
     * @param x
     * @throws FileNotFoundException
     * @throws IOException
     */
    public svm_node[] scale(svm_node[] x) throws FileNotFoundException, IOException{
        int N = x.length;
        if(feature_min == null || feature_max == null){
            feature_min = new double[N];
            feature_max = new double[N];
        }
        max_index = N - 1;

        readXXXFile();

        int id;
        double val;
        for(int i = 0; i < N; i ++){
            id = x[i].index;
            val = x[i].value;
            x[i].value = id;
            x[i].value = transform(id, val);
            //System.out.print(x[i].index + ":" + x[i].value + ",");
        }
//        System.out.println();

        return x;
    }

    public void printFeatureMin(){
        System.out.print("Min [");
        for(int i = 0; i < feature_min.length; i ++){
            System.out.print(i + ":" + feature_min[i] + ",");
        }
        System.out.print("]\n");
    }

    public void printFeatureMax(){
        System.out.print("Max [");
        for(int i = 0; i < feature_max.length; i ++){
            System.out.print(i + ":" + feature_max[i] + ",");
        }
        System.out.print("]\n");
    }

    public void normalizeForTrain(String trainFileName) throws FileNotFoundException, IOException{
        //TODO - We may need it, for now scale out-perform z-score based normalization
    }


}
