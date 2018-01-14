package plannist.predictor;

public class PredictorFactory {

    public static Predictor getPredictor(String predictorName){
        if(predictorName.equals("SVM")){
            return new PredictorSVM();
        }
        else{
            return null;
        }
    }
}
