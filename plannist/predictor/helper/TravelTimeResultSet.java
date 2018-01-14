package plannist.predictor.helper;

import java.util.Calendar;
import java.util.Vector;

/**
 * @author Xia Li
 * @version 0.1
 */

public class TravelTimeResultSet{
    Vector<TravelTimeStats> resultSet;

    public TravelTimeResultSet(){
        resultSet = new Vector<>();
    }

    public void add(TravelTimeStats stats){
        resultSet.add(stats);
    }

    public Vector<TravelTimeStats> getResultSet(){
        return resultSet;
    }

    public TravelTimeStats find(int tripID, int busID, int stopFrom, int stopTo, Calendar depTime){
        for(int i = 0; i < getSize(); i ++){
            TravelTimeStats current = this.resultSet.get(i);
            if(current.getTripID() == tripID && current.getBusID() == busID
                    && current.getStopFrom() == stopFrom && current.getStopTo() == stopTo
                    && current.getDepTime().compareTo(depTime)==0){
                return current;
            }
        }
        return null;
    }

    public int getSize(){
        return resultSet.size();
    }
}



