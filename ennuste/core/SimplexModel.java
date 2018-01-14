package ennuste.core;

import plannist.predictor.helper.TravelTimeResultSet;
import plannist.predictor.helper.TravelTimeStats;

import java.util.Calendar;

public class SimplexModel extends Model {
    private String inputFile;
    private TravelTimeResultSet rs;

    public SimplexModel(String inputFile){
        this.inputFile = inputFile;
    }

    private void load(){
        // Todo - read input file and load descriptive stats
        // if the data is not very large, load to RAM
        // other wise, get per read op
        // noted! the training process is simply the process of calculating the mean, median, min, max
        // whether these calculations should be conducted here or in advanced in other program should
        // be discussed

        // For testing only --- add some dummies
        rs = new TravelTimeResultSet();
        rs.add(new TravelTimeStats(611, 965, 1, 2, Calendar.getInstance(), 123.0, 123.0, 100.0, 130.0));
        rs.add(new TravelTimeStats(611, 965, 2, 3, Calendar.getInstance(), 123.1, 123.1, 100.1, 130.1));
        rs.add(new TravelTimeStats(611, 965, 3, 4, Calendar.getInstance(), 123.2, 123.2, 100.2, 130.2));
    }

    /**
     * Historical statistic description of tts
     *
     * @param tripID
     * @param busID
     * @param stopFrom
     * @param stopTo
     * @param depTime
     * @return
     */
    public TravelTimeStats getStats(int tripID, int busID, int stopFrom, int stopTo, Calendar depTime){
        return this.rs.find(tripID, busID, stopFrom, stopTo, depTime);
    }

    public double getStats_Mean(int tripID, int busID, int stopFrom, int stopTo, Calendar depTime){
        return this.rs.find(tripID, busID, stopFrom, stopTo, depTime).getTravelTimeMean();
    }
}
