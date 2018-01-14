package ennuste.core;

import ennuste.exception.ModelException;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import java.io.IOException;
import java.util.Vector;

/**
 * Grid Search for model selection, CV is used
 */
public class GridSearch {
    private svm_problem problem;
    private Vector<svm_parameter> params;
    private String trainingFile;
    private String xxxFileName;
    private boolean reformatInputFile;
    private boolean scale;
    private Vector<Double> scores;
    private final int crossValidation = 1;
    private int nFolder;
    private boolean trainAfterCV;
    private SVMModel bestModel;

    public GridSearch(Vector<svm_parameter> params, svm_problem problem, boolean scale,
                      int nFolder, String xxxFileName) throws IOException, ModelException{
        this.params = params;
        this.problem = problem;
        this.scale = scale;
        this.nFolder = nFolder;
        this.xxxFileName = xxxFileName;
        runOnFly();
    }

    public GridSearch(Vector<svm_parameter> params, String trainingFile, boolean reformatInputFile,boolean scale,
                      int nFolder) throws IOException, ModelException{
        this.params = params;
        this.trainingFile = trainingFile;
        this.reformatInputFile = reformatInputFile;
        this.scale = scale;
        this.nFolder = nFolder;
        run();
    }

    private void run() throws IOException, ModelException{
        int CV_validCount = 0;
        int n = this.params.size();
        this.scores = new Vector<Double>();
        double currentBestScore = -1;
        SVMModel currentBestModel = null;
        SVMModel m = null;
        boolean perSave = false;
        for(int i = 0; i < n; i ++) {
            m = new SVMModel(this.trainingFile, this.reformatInputFile, this.scale, this.params.get(i),
                              this.crossValidation, this.nFolder, perSave);
            double CV_RMSE = m.getCV_scores()[2]; // 0 - SSE, 1 - MSE, 2 - RMSE, 3 - MAPE

            if(currentBestScore == -1){
                currentBestScore = CV_RMSE;
                currentBestModel = m;
            }
            else{
                if(CV_RMSE < currentBestScore){
                    currentBestScore = CV_RMSE;
                    currentBestModel = m;
                }
            }

            // Recording CV result
            scores.add(i, CV_RMSE);

        }

        this.bestModel = currentBestModel;

        System.out.println();
        System.out.println("Best model is >>>> ");
        this.bestModel.printParam();
        System.out.println("RMSE:" + currentBestScore);
    }

    private void runOnFly() throws IOException, ModelException{
        int CV_validCount = 0;
        int n = this.params.size();
        this.scores = new Vector<Double>();
        double currentBestScore = -1;
        SVMModel currentBestModel = null;
        SVMModel m = null;
        boolean perSave = false;
        for(int i = 0; i < n; i ++) {
            m = new SVMModel(this.problem, this.scale, this.params.get(i),
                    this.crossValidation, this.nFolder, perSave, xxxFileName);
            double CV_RMSE = m.getCV_scores()[2]; // 0 - SSE, 1 - MSE, 2 - RMSE, 3 - MAPE

            if(currentBestScore == -1){
                currentBestScore = CV_RMSE;
                currentBestModel = m;
            }
            else{
                if(CV_RMSE < currentBestScore){
                    currentBestScore = CV_RMSE;
                    currentBestModel = m;
                }
            }

            // Recording CV result
            scores.add(i, CV_RMSE);

        }

        this.bestModel = currentBestModel;

        System.out.println();
        System.out.println("Best model is >>>> ");
        this.bestModel.printParam();
        System.out.println("RMSE:" + currentBestScore);
    }

    public SVMModel getBestModel(){
        return this.bestModel;
    }
}
