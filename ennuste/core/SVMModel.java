package ennuste.core;

/**
 *
 * Note: SVM core.Model Core
 */

import ennuste.common.InputFileConverter;
import ennuste.common.Utils;
import ennuste.exception.InvalidInputException;
import ennuste.exception.InvalidParamException;
import ennuste.exception.ModelException;
import libsvm.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

public class SVMModel extends Model {
    private Metrices me;
    private boolean reformatInputFile;
    private boolean scale;
    private svm_parameter param;        // parameters holder
    private svm_problem problem;           // problem wrapper for SVM
    private int inputDime;
    private String inputFileName;
    private String xxxFileName;
    private int crossValidation;     // 0 - no cross validation
    private int nFolder;              // folder size
    private svm_model model;
    private boolean saveModel;
    private double CV_SSE;             // for regression
    private double CV_RMSE;            // for regression
    private double CV_MSE;             // for regression
    private double CV_MAPE;            // for regression
    private double CV_SCC;             // for regression
    private double CV_MPE;             // for classification

    public SVMModel(){}

    public SVMModel(svm_problem problem, boolean scale, svm_parameter myparam,
                    int crossValidation, int nFolder, boolean saveModel, String xxxFileName) throws IOException, ModelException{
        this.problem = problem;
        this.scale = scale;
        this.param = myparam;
        this.crossValidation = crossValidation;
        this.inputFileName = inputFileName;
        this.nFolder = nFolder;
        this.saveModel = saveModel;
        this.xxxFileName = xxxFileName;
        initForOnFlyTrain();
    }

    public SVMModel(String inputFileName, boolean reformatInputFile, boolean scale, svm_parameter myparam,
                    int crossValidation, int nFolder, boolean saveModel) throws IOException, ModelException {
        this.reformatInputFile = reformatInputFile;
        this.scale = scale;
        this.param = myparam;
        this.crossValidation = crossValidation;
        this.inputFileName = inputFileName;
        this.nFolder = nFolder;
        this.saveModel = saveModel;
        init();
    }

    /**
     * Initialization of the model
     * @throws IOException
     * @throws ModelException
     */
    private void init() throws IOException, ModelException{
        // Check if the input file should be convert to libSVM format
        if(this.reformatInputFile){
            reformatInputFile();
            this.inputFileName = this.inputFileName + ".svm";
        }

        // Scale the training set
        if(this.scale){
            scale();
        }

        setProblem();
        checkParams();

        // Check if cross validation is needed
        if(this.crossValidation == 1){
            crossValidate();
        }
        // Goes to here only if you use SVMModel without project context
        else{
            train();
        }
    }

    /**
     * @throws IOException
     * @throws ModelException
     */
    private void initForOnFlyTrain() throws IOException, ModelException{
        // Scale the training set
        if(this.scale){
            problem = scaleOnFly();
        }

        //printProblem();

        // No problem setting is needed, precomputed kernel is not supported!
        checkParams();

        // Check if cross validation is needed
        if(this.crossValidation == 1){
            crossValidate();
        }
        // Goes to here only if you use SVMModel without project context
        else{
            train();
        }
    }

    /**
     * Convert non libSVM input file to libSVM input format
     * @throws IOException
     * @throws InvalidInputException
     */
    private void reformatInputFile() throws IOException, InvalidInputException{
        new InputFileConverter(this.inputFileName);
    }

    private void scale() throws InvalidParamException, IOException{
        String xxxFileName = inputFileName + ".xxx";
        Preprocess prep = new Preprocess(-1, 1, xxxFileName);
        prep.scaleForTrain(inputFileName);
    }

    private svm_problem scaleOnFly() throws InvalidParamException, IOException{
        Preprocess prep = new Preprocess(-1, 1, this.xxxFileName);
        prep.setProblem(problem);
        return prep.scaleForTrainOnFly();
    }

    /**
     * Read up data from input file and setup D=(X,y) for learning
     * Problem is actually the data structure for D
     *
     * @throws IOException
     * @throws InvalidInputException
     */
    private void setProblem() throws IOException, InvalidInputException{
        String finalInputFileName;
        if(scale){
            finalInputFileName = inputFileName + ".scaled";
        }
        else{
            finalInputFileName = inputFileName;
        }
        
        // Read data from the input file
        BufferedReader reader = new BufferedReader(new FileReader(finalInputFileName));
        if(reader.toString().length() == 0){
            throw new InvalidInputException("Invalid input file! File is empty!");
        }

        // Prepare holders for dataset D=(X,y)
        Vector<svm_node[]> X = new Vector<svm_node[]>();
        Vector<Double> y = new Vector<Double>();
        int maxIndex = 0;

        // Load data from file to holders
        while(true){
            String line = reader.readLine();

            if(line == null){
                break;
            }

            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            y.addElement(Utils.toDouble(st.nextToken()));
            int m = st.countTokens()/2;
            svm_node[] x = new svm_node[m];
            for(int i = 0; i < m; i++) {
                x[i] = new svm_node();
                x[i].index = Utils.toInt(st.nextToken());
                x[i].value = Utils.toDouble(st.nextToken());
            }
            if(m > 0){
                maxIndex = Math.max(maxIndex, x[m-1].index);
            }
            X.addElement(x);
        }

        this.problem = new svm_problem();
        this.problem.l = y.size();
        this.inputDime = maxIndex + 1;

        // Wrap up multi-dimensional input vector X
        this.problem.x = new svm_node[this.problem.l][];
        for(int i=0;i<this.problem.l;i++)
            this.problem.x[i] = X.elementAt(i);

        // Wrap up 1-dimensional input vector y
        this.problem.y = new double[this.problem.l];
        for(int i=0;i<this.problem.l;i++)
            this.problem.y[i] = y.elementAt(i);
        
////////////////////???
        // Verify the gamma setting according to the maxIndex
        if(param.gamma == 0 && maxIndex > 0)
            param.gamma = 1.0/maxIndex;

        // Dealing with pre-computed kernel
        if(param.kernel_type == svm_parameter.PRECOMPUTED){
            for(int i = 0; i < this.problem.l; i++) {
                if (this.problem.x[i][0].index != 0) {
                    String msg = "Invalid kernel matrix! First column must be 0:sample_serial_number.";
                    throw new InvalidInputException(msg);
                }
                if ((int)this.problem.x[i][0].value <= 0 || (int)this.problem.x[i][0].value > maxIndex){
                    String msg = "Invalid kernel matrix! Sample_serial_number out of range.";
                    throw new InvalidInputException(msg);
                }
            }
        }

        reader.close();
    }

    /**
     * This method check if parameters setting is correct for SVM learning
     */
    private void checkParams() throws InvalidParamException{
        String error = svm.svm_check_parameter(this.problem, this.param);
        if(error != null){
            throw new InvalidParamException("Invalid parameter setting!" + error);
        }
    }

    private void crossValidate(){
        int i;
        int total_correct = 0;
        double total_error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
        double[] target = new double[this.problem.l];

        svm.svm_cross_validation(this.problem, this.param, this.nFolder, target);
        this.me = new Metrices(target, this.problem.y, this.problem.l);
        if(this.param.svm_type == svm_parameter.EPSILON_SVR || this.param.svm_type == svm_parameter.NU_SVR){
            for(i = 0; i < this.problem.l; i++) {
                double y = this.problem.y[i];
                double v = target[i];
                sumv += v;
                sumy += y;
                sumvv += v*v;
                sumyy += y*y;
                sumvy += v*y;
            }
            this.CV_SSE = me.getSSE();
            this.CV_MSE = me.getMSE();
            this.CV_RMSE = me.getRMSE();
            this.CV_MAPE = me.getMAPE();
            //this.CV_SCC = ((this.problem.l * sumvy - sumv * sumy) * (this.problem.l * sumvy - sumv * sumy))/
            //        ((this.problem.l * sumvv - sumv * sumv) * (this.problem.l * sumyy - sumy * sumy));
            System.out.println();
            System.out.println("Cross Validation Mean Squared Error(MSE): " + Utils.format(this.CV_MSE));
            System.out.println("Cross Validation Root Mean Squared Error(RMSE): " + Utils.format(this.CV_RMSE));
            System.out.println("Cross Validation Mean Absolute Percentage Error(MAPE): " + Utils.format(this.CV_MAPE) + "%");
            //System.out.println("Cross Validation Squared Correlation Coefficient(R^2): "+ Utils.format(this.CV_SCC));
        }
        else {
            this.CV_MPE = me.getMPE();
            System.out.println("Cross Validation Accuracy = " + this.CV_MPE + "%");
        }
    }

    public void train()throws IOException{
        String modelFileName = "C:\\Users\\acer\\IdeaProjects\\NiCoLearner\\data\\model.mdl"; ////////////////need to change
        this.model = svm.svm_train(this.problem, this.param);
        if(saveModel){
            svm.svm_save_model(modelFileName, this.model);
        }
    }

    public void train(boolean save, String modelFileName) throws IOException{
        this.model = svm.svm_train(this.problem, this.param);
        if(save){
            svm.svm_save_model(modelFileName, this.model);
        }
    }

    private svm_model loadModel(String modelFileName) throws IOException{
        svm_model model = svm.svm_load_model(modelFileName);
        return model;
    }

///////////////////read file -> svm.svm_predict -> Metrices
    /**
     * predict on test set, test set is read from a CSV file
     * @param testFileName
     * @throws IOException
     * @throws InvalidInputException
     */
    public void predict(String testFileName) throws IOException, InvalidInputException{
        BufferedReader reader = new BufferedReader(new FileReader(testFileName));
        if(reader.toString().length() == 0){
            throw new InvalidInputException("Invalid input file! File is empty!");
        }

        int index = 0;
        Vector<Double> actuals = new Vector<>();
        Vector<Double> predicts = new Vector<>();
        while(true) {
            String line = reader.readLine();
            if (line == null)
                break;

            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            double target = Utils.toDouble(st.nextToken());
            int m = st.countTokens() / 2;
            svm_node[] x = new svm_node[m];
            for (int j = 0; j < m; j++) {
                x[j] = new svm_node();
                x[j].index = Utils.toInt(st.nextToken());
                x[j].value = Utils.toDouble(st.nextToken());
            }
            double v;
            v = svm.svm_predict(this.model, x);

            actuals.add(index, target);
            predicts.add(index, v);

            System.out.println("[" + index + "] Prediction:" + v + ", actual:" + target);
            index += 1;
        }
        Metrices me = new Metrices(Utils.toDoubleArray(predicts), Utils.toDoubleArray(actuals), predicts.size());
        System.out.println("RMSE: " + me.getRMSE());
    }

    public double[] predict(svm_node[][] X){
        int n = X.length;
        if(n > 0) {
            double[] predicts = new double[n];
            for (int i = 0; i < n; i++) {
                svm_node[] x = X[i];
                predicts[i] = svm.svm_predict(model, x);
            }
            return predicts;
        }
        else{
            return null;
        }
    }

    public double predict(String modelFileName, svm_node[] x) throws IOException{
        svm_model model = loadModel(modelFileName);
        if (model == null){
            System.err.print("Fail to open model file "+ modelFileName +"\n");
            System.exit(1);
        }
        return svm.svm_predict(model, x);
    }

    public double[] predict(String modelFileName, svm_node[][] X) throws IOException{
        svm_model model = loadModel(modelFileName);
        if (model == null){
            System.err.print("Fail to open model file "+ modelFileName +"\n");
            System.exit(1);
        }

        return predict(X);
    }

    public double predict(svm_model model, svm_node[] x) throws IOException{
        if (model == null){
            System.exit(1);
        }
        return svm.svm_predict(model, x);
    }

    public double[] predict(svm_model model, svm_node[][] X) throws IOException{
        if (model == null){
            System.exit(1);
        }
        return predict(X);
    }

    /**
     * Return the name model type name
     * @param idx
     * @return
     */
    private String getTypeName(int idx){
        String[] types = {"C-SVC","nu-SVC","one-class SVM","epsilon-SVR","nu-SVR"};
        return types[idx];
    }

    /**
     * Return the kernel name
     * @param idx
     * @return
     */
    private String getKernelName(int idx){
        String[] kernels = {"Line","Poly","RBF","Sigmoid","Precomputed kernel"};
        return kernels[idx];
    }

    /**
     * Return model parameter
     * @return model parameter as svm_parameter object
     */
    public svm_parameter getParam(){
        return this.param;
    }

    /**
     * Print parameter
     */
    public void printParam(){
        StringBuffer sb = new StringBuffer();
        sb.append("SVM Type:" + getTypeName(this.param.svm_type) + "\n")
                .append("Kernel:" + getKernelName(this.param.kernel_type) + "\n")
                .append("Degree:" + this.param.degree + "\n")
                .append("Gamma:" + this.param.gamma + "\n")
                .append("Coef0:" + this.param.coef0 + "\n")
                .append("NU:" + this.param.nu + "\n")
                .append("Cache Size:" + this.param.cache_size + "\n")
                .append("C:" + this.param.C + "\n")
                .append("Epsilon:" + this.param.eps + "\n")
                .append("P:" + this.param.p + "\n")
                .append("Use shrinking heuristics: " + (this.param.shrinking == 1?"True":"False") + "\n")
                .append("Probability estimates:" + (this.param.probability==1?"True":"False"))
                .toString();

        System.out.println("============= core.Model Parameters ==============");
        System.out.println(sb);
        System.out.println("=============================================");
    }

    /**
     * Return problem
     * @return as svm_problem
     */
    public svm_problem getProblem(){
        return this.problem;
    }

    /**
     * Print D=(X,y)
     */
    public void printProblem(){
        System.out.println("Observation Size:" + this.problem.l + ", Input Dimension:" + this.inputDime);
        System.out.println("(X,y)=[");
        int index = 0;
        for(svm_node[] xs: this.problem.x){
            double y = this.problem.y[index];
            for(int i = 0;i < Math.min(5, xs.length); i++){
                System.out.print("(" + xs[i].value + ")  ");
            }
            System.out.println(" <"+ y + ">");
            index += 1;
        }
        System.out.println("]");
    }

    public svm_model getModel(){
        return this.model;
    }

    public double[] getCV_scores(){
        double[] scores;
        if(this.param.svm_type == svm_parameter.EPSILON_SVR || this.param.svm_type == svm_parameter.NU_SVR) {
            scores = new double[4];
            scores[0] = this.CV_SSE;
            scores[1] = this.CV_MSE;
            scores[2] = this.CV_RMSE;
            scores[3] = this.CV_MAPE;
        }
        else{
            scores = new double[1];
            scores[0] = this.CV_MPE;
        }
        return scores;
    }

    public svm_model getModelFromFile(String modelFileName) throws IOException{
        return loadModel(modelFileName);
    }
}
