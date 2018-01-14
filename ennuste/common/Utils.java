package ennuste.common;

import libsvm.svm_node;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;

public class Utils {

    public static int getDayOfWeek(Calendar c){
        boolean isFirstDaySunday = (c.getFirstDayOfWeek() == Calendar.SUNDAY);
        int dow = c.get(Calendar.DAY_OF_WEEK);
        if(isFirstDaySunday){
            dow = dow - 1;
            if(dow == 0){
                dow = 7;
            }
        }

        return dow - 1;
    }

    public static double computePercentile(ConcurrentSkipListMap<Integer, Double> data, double p){
        int size = data.size();
        if(size > 0) {
            double sum = 0.0;
            for (int i = 0; i < size; i++) {
                sum += data.get(i);
            }

            double targetSum = sum * p;
            double actualSum = 0;

            SortedSet<Integer> keys = new TreeSet<Integer>(data.keySet());
            for (int key : keys) {
                actualSum += data.get(key);
                if (actualSum >= targetSum) {
                    return format(data.get(key));
                }
            }
        }
        return Double.NaN;
    }

    public static double sum(double[] vals){
        double sums = 0.0;
        int n = vals.length;
        for(int i = 0; i < n; i ++){
            sums += vals[i];
        }
        return sums;
    }

    public static double mean(double[] vals){
        int n = vals.length;
        return sum(vals) * 1.0 / n;
    }

    public static double std(double[] vals){
        int n = vals.length;
        double mean = mean(vals);
        double diff_sum = 0.0;
        for(int i = 0; i < n; i ++){
            diff_sum += Math.pow((vals[i] - mean), 2);
        }
        return Math.sqrt(diff_sum / (n - 1));
    }

    public static double stdScore(double v, double mean, double std){
        if(std == 0){
            return 0;
        }
        else {
            return (v - mean) * 1.0 / std;
        }
    }

    public static svm_node[][] normalize(svm_node[][] unnormX, int dim){
        int n = unnormX.length;
        double[][] columnized = new double[dim][];
        for(int i = 0; i < dim; i ++){
            columnized[i] = new double[n];
            for(int j = 0; j < n; j ++){
                columnized[i][j] = unnormX[j][i].value;
            }
        }

        svm_node[][] normedX = new svm_node[n][];
        for(int k = 0; k < n; k ++){
            normedX[k] = new svm_node[dim];
            for(int l = 0; l < dim; l ++){
                normedX[k][l] = new svm_node();
                normedX[k][l].index = unnormX[k][l].index;
                normedX[k][l].value = stdScore(unnormX[k][l].value, mean(columnized[l]), std(columnized[l]));
            }
        }

        return normedX;
    }

    public static double parseDateToToD(Calendar dt){
        int hour = dt.get(Calendar.HOUR_OF_DAY);
        int minute = dt.get(Calendar.MINUTE);
        return format(hour + (minute / 60.0));
    }

    public static int parseDateToHM(Calendar dt){
        int hour = dt.get(Calendar.HOUR_OF_DAY);
        int minute = dt.get(Calendar.MINUTE);
        return hour * 100 + minute;
    }

    public static String parseDateToToDStr(Calendar dt){
        return Integer.toString(dt.get(Calendar.HOUR_OF_DAY)) + ":" + Integer.toString(dt.get(Calendar.MINUTE));
    }

    public static Calendar parseDateStrtoDate(String str){
        Calendar t = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/m/d H:m:s");
        try {
            t.setTime(sdf.parse(str));
            return t;
        }
        catch(ParseException pe){
            System.err.println(pe);
        }
        return null;
    }

    public static double[] toDoubleArray(Vector<Double> vd){
        int n = vd.size();
        double[] newD = new double[n];
        for(int i = 0; i < n; i ++){
            newD[i] = vd.get(i).doubleValue();
        }
        return newD;
    }

    public static double[] toDoubleArray(Double[] d){
        int n = d.length;
        double[] newD = new double[n];
        for(int i = 0; i < n; i ++){
            newD[i] = d[i].doubleValue();
        }
        return newD;
    }

    public static double toDouble(String str){
        double d = Double.valueOf(str).doubleValue();
        if(Double.isNaN(d) || Double.isInfinite(d)){
            System.err.print("Found NaN or Infinity in input! Please replace them with estimated values first!\n");
            System.exit(1);
        }
        return(d);
    }

    public static int toInt(String str){
        return Integer.parseInt(str);
    }

    public static Double format(double d){
        NumberFormat formatter = new DecimalFormat("#0.000");
        return Double.parseDouble(formatter.format(d));
    }

    public static double[] arangeDouble(double start, double end, double step){
        int n = (int)Math.ceil((end - start) / step);
        double[] rangeValues = new double[n];
        int index = 0;
        for(double x = start; x < end; x += step){
            rangeValues[index] = x;
            index ++;
        }

        return rangeValues;
    }

    public static int[] arangeInt(int start, int end, int step){
        int n = (int)Math.ceil((end - start) / step);
        int[] rangeValues = new int[n+1];
        int index = 0;
        for(int x = start; x < end; x+= step){
            rangeValues[index] = x;
            index ++;
        }
        return rangeValues;
    }

    /**
     *
     * @param time
     * @param interval (in minutes)
     * @return
     */
    public static int getIntervalIndex(Calendar time, int interval) throws ParseException{
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int min = time.get(Calendar.MINUTE);

        int perMinSample = 60 / interval;
        int h=0, m_lo=0, m_up=interval;
        int index = 0;
        for(int i = 0; i < 24; i ++){
            for(int j = 0; j < perMinSample; j ++){
                if(hour == h && min >= m_lo && min < m_up){
                    return index;
                }
                m_lo += interval;
                m_up += interval;
                if(m_lo == 60){
                    m_lo = 0;
                    m_up = interval;
                    h += 1;
                }
                index ++;
            }
        }
        return -1;
    }

}