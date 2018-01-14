package plannist.predictor.helper;

import java.util.Calendar;

/**
 * @author Xia Li
 * @version 0.1
 */
public class TravelTimeStats {
    private int tripID;
    private int busID;
    private int stopFrom;
    private int stopTo;
    private Calendar depTime;
    private double travelTimeMean;
    private double travelTimeMedian;
    private double travelTimeMin;
    private double travelTimeMax;

    public TravelTimeStats(int tripID, int busID, int stopFrom, int stopTo, Calendar depTime,
                           double travelTimeMean, double travelTimeMedian, double travelTimeMin, double travelTimeMax){
        this.tripID = tripID;
        this.busID = busID;
        this.stopFrom = stopFrom;
        this.stopTo = stopTo;
        this.depTime = depTime;
        this.travelTimeMean = travelTimeMean;
        this.travelTimeMedian = travelTimeMedian;
        this.travelTimeMin = travelTimeMin;
        this.travelTimeMax = travelTimeMax;
    }

    public int getTripID() {
        return tripID;
    }

    public int getBusID() {
        return busID;
    }

    public int getStopFrom() {
        return stopFrom;
    }

    public int getStopTo() {
        return stopTo;
    }

    public Calendar getDepTime(){
        return depTime;
    }

    public double getTravelTimeMean() {
        return travelTimeMean;
    }

    public double getTravelTimeMedian() {
        return travelTimeMedian;
    }

    public double getTravelTimeMin() {
        return travelTimeMin;
    }

    public double getTravelTimeMax() {
        return travelTimeMax;
    }
}
