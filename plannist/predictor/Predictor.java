package plannist.predictor;

import libsvm.svm_model;
import plannist.predictor.helper.TestSet;

import java.io.IOException;

public abstract class Predictor {

    public abstract void setModelFileName(String modelFileName);

    public abstract boolean train(String inputFileName, boolean reformatInputFile, boolean scale,
                                  int nFolder, boolean saveModel);

    public abstract double predict(String modelFileName, TestSet testset);

    public abstract double predict(svm_model model, TestSet testset);

    public abstract svm_model getModelFromFile(String modelFileName) throws IOException;
}
