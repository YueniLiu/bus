package ennuste.core;

public class Metrices {
    private double[] predict;
    private double[] actual;
    private int n;
    private double totalError = 0;
    private double totalAbsError = 0;
    private double totalAbsPercentError = 0;

    public Metrices(double[] predict, double[] actual, int n){
        this.predict = predict;
        this.actual = actual;
        this.n = n;
        init();
    }

    /**
     * Squared sum error
     * @return
     */
    public void init(){
        this.totalError = 0;
        this.totalAbsError = 0;
        for(int i = 0; i < this.n; i++) {
            double y = this.actual[i];
            double v = this.predict[i];
            this.totalError += (v-y)*(v-y);
            this.totalAbsError += Math.abs(v-y);
            this.totalAbsPercentError += Math.abs((v-y) * 1.0 / y);
        }
    }

    /**
     * get squared sum error
     * @return
     */
    public double getSSE(){
        return this.totalError;
    }

    /**
     * get mean squared error
     * @return
     */
    public double getMSE(){
        return this.totalError * 1.0 / this.n;
    }

    /**
     * get root mean sequared error
     * @return
     */
    public double getRMSE(){
        return Math.sqrt(this.totalError * 1.0 / this.n);
    }

    /**
     * get mean absolute error
     * @return
     */
    public double getMAE(){
        return this.totalAbsError * 1.0 / this.n;
    }

    /**
     * get mean absolute percentage error
     * @return
     */
    public double getMAPE(){
        return this.totalAbsPercentError * 100.0 / this.n;
    }

    /**
     * get mean percentage error (classification)
     * @return
     */
    public double getMPE(){
        int totalAccuracy = 0;
        for(int i = 0; i < this.n; i++) {
            if(this.predict[i] == this.actual[i]){
                totalAccuracy ++;
            }
        }
        return 100.0 * totalAccuracy / this.n;
    }

}
