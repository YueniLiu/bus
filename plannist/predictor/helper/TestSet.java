package plannist.predictor.helper;

import ennuste.common.Utils;
import libsvm.svm_node;

import java.util.Calendar;

/**
 * @author Xia Li
 * @version 0.1
 *
 * Note:
 *    - X for testset
 *      - 0: departure time (decimal, e.g., 08:15=8+1/4=8.25)
 *      - 1: departure day-of-week (sun - sat)
 *      - 2: departure month (Jan 1 - 12 Dec)
 *      - 3: departure year (yyyy)
 *      - 4. historical tts stats at this departure ToD
 *
 *      - y. tts: default is 0 (must specify if write to file)
 */
public class TestSet {
    private Calendar depTime;
    private svm_node[] x;

    public TestSet() {}

    /**
     * Build test set for k->k+1
     */
    public void build_TTS(Calendar depTime, int dow, double ttsStats, double tts_earlyTerm){
        int dimN = 3;
        this.x = new svm_node[dimN];

        // dep time
        this.x[0] = new svm_node();
        this.x[0].index = 0;
        this.x[0].value = Utils.parseDateToToD(depTime);

        // dep day-of-week (capture weekly pattern)
        this.x[1] = new svm_node();
        this.x[1].index = 1;
        this.x[1].value = dow;

        // dep month (capture seasonal pattern)
        //this.x[2] = new svm_node();
        //this.x[2].index = 2;
        //this.x[2].value = depTime.get(Calendar.MONTH) + 1;

        // dep year (capture yearly diff)
        //this.x[3] = new svm_node();
        //this.x[3].index = 5;
        //this.x[3].value = depTime.get(Calendar.YEAR);

        // current day last 30min tts stats
        //this.x[2] = new svm_node();
        //this.x[2].index = 2;
        //this.x[2].value = tts_earlyTerm;

        // historical tts stats
        this.x[2] = new svm_node();
        this.x[2].index = 2;
        this.x[2].value = ttsStats;

    }

    public void build_AR(Calendar depTime, int dow, double ARStats, double AR_earlyTerm){
        int dimN = 3;
        this.x = new svm_node[dimN];

        // dep time
        this.x[0] = new svm_node();
        this.x[0].index = 0;
        this.x[0].value = Utils.parseDateToToD(depTime);

        // dep day-of-week (capture weekly pattern)
        this.x[1] = new svm_node();
        this.x[1].index = 1;
        this.x[1].value = dow;

        // AR historical stats
        this.x[2] = new svm_node();
        this.x[2].index = 2;
        this.x[2].value = ARStats;

        // AR stats from early term current day
        //this.x[3] = new svm_node();
        //this.x[3].index = 3;
        //this.x[3].value = AR_earlyTerm;
    }

    public void build_AL(Calendar depTime, int dow, double ALStats, double AL_earlyTerm){
        int dimN = 3;
        this.x = new svm_node[dimN];

        // dep time
        this.x[0] = new svm_node();
        this.x[0].index = 0;
        this.x[0].value = Utils.parseDateToToD(depTime);

        // dep day-of-week (capture weekly pattern)
        this.x[1] = new svm_node();
        this.x[1].index = 1;
        this.x[1].value = dow;

        // AR historical stats
        this.x[2] = new svm_node();
        this.x[2].index = 2;
        this.x[2].value = ALStats;

        // AR stats from early term current day
        //this.x[3] = new svm_node();
        //this.x[3].index = 3;
        //this.x[3].value = AL_earlyTerm;
    }
    
    public int getDimSize() {
        return this.x.length;
    }

    public void setTestSet(svm_node[] x){
        this.x = x;
    }

    public svm_node[] getTestSet(){
        return this.x;
    }

    public void printTestSet(){
        for(int i = 0; i < getDimSize(); i ++){
            svm_node node = this.x[i];
            System.out.print(node.index + ":" + node.value + ", ");
        }
    }
}
