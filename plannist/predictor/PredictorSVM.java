package plannist.predictor;

import ennuste.common.Utils;
import ennuste.core.GridSearch;
import ennuste.core.SVMModel;
import ennuste.exception.ModelException;
import libsvm.svm_model;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import plannist.predictor.helper.TestSet;

import java.io.IOException;
import java.util.Vector;

public class PredictorSVM extends Predictor{
    private String modelFileName;

    public PredictorSVM(){}

    @Override
    public void setModelFileName(String modelFileName) {
        this.modelFileName = modelFileName;
    }

    /**
     * Grid search of CV for model selection.
     * After best model is selected, training is performed
     *
     * @param nFolder
     * @param saveModel
     * @return
     */

    public boolean trainOnFly(svm_problem problem, boolean scale, int nFolder, boolean saveModel, String xxxFileName){
        try {
            GridSearch searcher = new GridSearch(setParams(), problem, scale, nFolder, xxxFileName);
            SVMModel svm = searcher.getBestModel();

            svm.train(saveModel, this.modelFileName);
            return true;
        }
        catch(IOException ioe){
            System.err.println(ioe);
            return false;
        }
        catch(ModelException me){
            System.err.println(me);
            return false;
        }
    }

    public boolean train(String inputFileName, boolean reformatInputFile,
                         boolean scale, int nFolder, boolean saveModel){

        try {
            // Model selection (model fitting)
            GridSearch searcher = new GridSearch(setParams(), inputFileName, reformatInputFile, scale, nFolder);
            SVMModel svm = searcher.getBestModel();

            // Training
            svm.train(saveModel, this.modelFileName);
            return true;
        }
        catch(IOException ioe){
            System.err.println(ioe);
            return false;
        }
        catch(ModelException me){
            System.err.println(me);
            return false;
        }
    }

    private Vector<svm_parameter> setParams(){
        int modelType = svm_parameter.EPSILON_SVR;
        int[] kernel_types = {svm_parameter.LINEAR, svm_parameter.POLY, svm_parameter.RBF, svm_parameter.SIGMOID};
        double[] Cs = Utils.arangeDouble(0.1, 4.0, 2.0);
        double[] gammas = Utils.arangeDouble(0.0, 5.0, 0.5);
        double[] epss = {1e-4, 1e-3, 1e-2, 1e-1, 1, 10, 100};
        int[] degrees = Utils.arangeInt(1, 5, 1);
        int cache_size = 10;
        int shrinking = 1;
        int prob = 0;
        double p = 0.1;
        double nu = 0.5;
        double coef = 0.0;

        Vector<svm_parameter> params = new Vector<>();
        int index = 0;

        for(int i = 0; i < kernel_types.length; i ++) {
            if(kernel_types[i] == svm_parameter.POLY){
                for(int j = 0; j < degrees.length; j ++){
                    for(int k = 0; k < Cs.length; k ++){
                        for(int l = 0; l < gammas.length; l ++){
                            for(int m = 0; m < epss.length; m ++){
                                svm_parameter param = new svm_parameter();
                                param.svm_type = modelType;
                                param.kernel_type = kernel_types[i];
                                param.cache_size = cache_size;
                                param.shrinking = shrinking;
                                param.probability = prob;
                                param.p = p;
                                param.nu = nu;
                                param.coef0 = coef;
                                param.degree = degrees[j];
                                param.C = Cs[k];
                                param.gamma = gammas[l];
                                param.eps = epss[m];
                                params.add(index, param);
                                index ++;
                            }
                        }
                    }
                }
            }
            else{
                for(int k = 0; k < Cs.length; k ++){
                    for(int l = 0; l < gammas.length; l ++){
                        for(int m = 0; m < epss.length; m ++){
                            svm_parameter param = new svm_parameter();
                            param.svm_type = modelType;
                            param.kernel_type = kernel_types[i];
                            param.cache_size = cache_size;
                            param.shrinking = shrinking;
                            param.probability = prob;
                            param.p = p;
                            param.nu = nu;
                            param.coef0 = coef;
                            param.C = Cs[k];
                            param.gamma = gammas[l];
                            param.eps = epss[m];
                            params.add(index, param);
                            index ++;
                        }
                    }
                }
            }
        }
        return params;
    }

    public double predict(String modelFileName, TestSet testset){
        SVMModel svm = new SVMModel();
        double predict = 0.0;
        try {
            predict = svm.predict(modelFileName, testset.getTestSet());
        }
        catch(IOException ioe){
            System.exit(1);
        }
        return predict;
    }

    public double predict(svm_model model, TestSet testset){
        SVMModel svm = new SVMModel();
        double predict = 0.0;
        try {
            predict = svm.predict(model, testset.getTestSet());
        }
        catch(IOException ioe){
            System.exit(1);
        }
        return predict;
    }

    public svm_model getModelFromFile(String modelFileName) throws IOException{
        return new SVMModel().getModelFromFile(modelFileName);
    }
}
