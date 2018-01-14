package plannist.predictor;

import ennuste.common.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Vector;

public class RequestHandler {
    private Vector<double[]> df;

    public RequestHandler(){

    }

    public void loadDataFromFile(String statsFileName, int skipColumn){
        df = new Vector<>();
        int index=0;
        try {
            BufferedReader xr = new BufferedReader(new FileReader(statsFileName));
            xr.readLine(); // skip the first line
            while (true) {
                String line = xr.readLine();

                if (line == null) {
                    break;
                }

                StringTokenizer st = new StringTokenizer(line, ",");
                int n = st.countTokens() - skipColumn;

                for(int k=0; k < skipColumn; k ++){
                    st.nextToken();
                }
                int i = 0;
                double[] perInterval = new double[n];
                while (st.hasMoreTokens()) {
                    perInterval[i] = Double.parseDouble(st.nextToken());
                    i ++;
                }
                df.add(perInterval);
            }
            xr.close();
        }
        catch(FileNotFoundException fnfe){
            System.err.println("Stats file not existed!");
            System.exit(1);
        }
        catch(IOException ioe){
            System.err.println("File reading error");
            System.exit(1);
        }
    }

    public void loadDataFromDB(){
        // TODO - in case if partner does not provide local file to read, there exists possibility to
        // load data from remote database
    }

    public Vector<double[]> getAll(){
        return df;
    }

    public double[] getStatsAtOneInterval(int index){
        return df.get(index);
    }

    public double getStatsAtOneStop(int row, int col){
        /**
         * For tts: row - interval id,
         *          col - stopId, if stopId is 1, the index is actually 0 in df
         *
         * For last30min: row - stats method index
         *                col - stopId, if stopId is 1, the index is actually 0 in df
         */
        return df.get(row)[col-1];
    }

    /**
     *
     * @param depTime
     * @param interval in minutes
     * @param stopId
     * @return
     */
    public double getStatsAtOneStop(Calendar depTime, int interval, int stopId) throws ParseException{
        int index = Utils.getIntervalIndex(depTime, interval);
        return df.get(index)[stopId-1];
    }

    public int size(){
        return df.size();
    }

    public void printOneRow(double[] row){
        int stopN = row.length;
        for(int j = 0; j < stopN; j ++){
            System.out.print(row[j] + ",");
        }
        System.out.println();
    }

    public void printAll(){
        int stopN;
        for(int i = 0; i < size(); i ++){
            double[] perInterval = df.get(i);
            stopN = perInterval.length;
            for(int j = 0; j < stopN; j ++){
                System.out.print(perInterval[j] + ",");
            }
            System.out.println();
        }
    }
}
