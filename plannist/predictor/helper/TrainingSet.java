package plannist.predictor.helper;

import ennuste.common.Utils;
import libsvm.svm_node;

import java.util.Calendar;
import java.util.Vector;

/**
 * Created by acer on 2016/3/26.
 */
public class TrainingSet {
    private Calendar depTime;
    private Vector<svm_node[]> X;
    private Vector<Double> y;
    private int size;

    public TrainingSet() {
        X = new Vector<svm_node[]>();
        y = new Vector<Double>();
    }

    /**
     * Build training set for prediction
     */
    public void add(Calendar depTime, double stats, double stats_earlyTerm, double val){
        int dimN = 4;
        svm_node[] x = new svm_node[dimN];

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

        X.add(x);
        y.add(val);
        size ++;
    }

    public int getSize(){
        return size;
    }

    public svm_node[] getFeature(int i){
        return X.get(i);
    }

    public double getTarget(int i){
        return y.get(i);
    }

    public Vector<svm_node[]> getFeatures(){
        return this.X;
    }

    public Vector<Double> getTargets(){
        return this.y;
    }

}
