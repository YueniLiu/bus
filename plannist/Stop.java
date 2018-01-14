
package plannist;

import java.util.Calendar;

public class Stop {
	int stopId=-1;
	double arrivalRate=0.5;
	double alightingRate=0.5;//proportion of bus load alighting at stop k
	double dwellTime;// bus dwell time at stop k
	int headWay=0;// headway time at stop k
	int load=10;
	Calendar departureTime;
	double objective1=0;
	double objective2=0;
	

	public Stop(int stopId) {
		super();
		this.stopId = stopId;
	}
	
	
	
	public double getObjective1() {
		return objective1;
	}



	public void setObjective1(double objective1) {
		this.objective1 = objective1;
	}



	public double getObjective2() {
		return objective2;
	}



	public void setObjective2(double objective2) {
		this.objective2 = objective2;
	}


	public int getStopId() {
		return stopId;
	}



	public void setStopId(int stopId) {
		this.stopId = stopId;
	}



	public void setArrivalRate(double arrivalRate) {
		this.arrivalRate = arrivalRate;
	}



	public double getArrivalRate() {
		return arrivalRate;
	}
	public double getAlightingRate() {
		return alightingRate;
	}
	public void setAlightingRate(double alightingRate) {
		this.alightingRate = alightingRate;
	}
	public double getDwellTime() {
		return dwellTime;
	}
	public void setDwellTime(double dwellTime) {
		this.dwellTime = dwellTime;
	}
	public int getHeadWay() {
		return headWay;
	}
	public void setHeadWay(int headWay) {
		this.headWay = headWay;
	}
	public int getLoad() {
		return load;
	}
	public void setLoad(int load) {
		this.load = load;
	}
	public Calendar getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(Calendar departureTime) {
		this.departureTime = departureTime;
	}
	@Override
	public String toString() {
		return "Stop [stopId=" + stopId + ", arrivalRate=" + arrivalRate+", alightingRate=" + alightingRate
				+ ", dwellTime=" + dwellTime + ", headWay=" + headWay
				+ ", load=" + load + ", departureTime=" + calendarToString(departureTime) + "]\n";
	}
	
	public Stop clone() {
//		Stop s=new Stop(stopId, arrivalRate,alightingRate, dwellTime, headWay, load,departureTime);
		Stop s=new Stop(stopId);
		s.stopId = this.stopId;
		s.alightingRate = this.alightingRate;
		s.dwellTime = this.dwellTime;
		s.headWay = this.headWay;
		s.load = this.load;
		s.departureTime = this.departureTime;
		return s;
	}
	
	public  String calendarToString(Calendar c) {

		return "(" + c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1)
				+ "-" + c.get(Calendar.DATE) + "|"
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE)
				+ ")";

	}
	
	
}
