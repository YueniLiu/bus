package plannist.predictor.helper;

import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by zx12785 on 3/29/2016.
 */
public class DoWStatsHolder {
    private static final int dayN = 7;
    Vector[] innerholders;

    public DoWStatsHolder(int interval){
        int iPH= 60 / interval; // num of intervals per hour
        int iPD = 24 * iPH;     // num of intervals per day
        innerholders = new Vector[dayN];
        for(int i = 0; i < dayN; i ++){
            innerholders[i] = new Vector();
            for(int j = 0; j < iPD; j ++){
                innerholders[i].add(j, new ConcurrentSkipListMap<Integer, Double>());
            }
        }
    }

    /**
     * @param day : 0 - monday, 6 - sunday
     * @param val
     */
    public void add(int day, int interval, double val){
        ConcurrentSkipListMap<Integer, Double> leafHolder =
                (ConcurrentSkipListMap<Integer, Double>)innerholders[day].get(interval);
        int index = leafHolder.size();
        leafHolder.put(index, val);
        innerholders[day].set(interval, leafHolder);
    }

    /**
     * @param day : 0 - monday, 6 - sunday
     * @return
     */
    public Vector get(int day){
        return innerholders[day];
    }

    public ConcurrentSkipListMap<Integer, Double> get(int day, int interval){
        return (ConcurrentSkipListMap<Integer, Double>)innerholders[day].get(interval);
    }

    public Vector[] getAll(){
        return innerholders;
    }
}
