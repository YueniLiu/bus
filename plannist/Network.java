
package plannist;

import java.util.Calendar;
import java.util.Random;
import java.util.Vector;
import java.math.*;

public class Network {
	static Vector<Bus> buses1 = new Vector<Bus>();
	static Vector<Bus> buses2 = new Vector<Bus>();
	static String tripID1;
	static String tripID2;
	static int miniRestTime;
	static Calendar startTime;
	static Calendar endTime;
	static double weight1;
	static double weight2;
	static double weight3;
	static Session ses;
	static double updateTimes = 0;

	/**
	 * @param buses
	 *            update trip information for each bus
	 * @return true: network update successfully
	 */

	public static String calendarToString(Calendar c) {

		return "(" + c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE) + "|"
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ")";

	}

	public static boolean update(Vector<Bus> buses, String tripId) {
		updateTimes++;

		for (int i = 0; i < buses.size(); i++) {
			Bus b = buses.get(i);
			// b.setTripStartTime(b.departureTime);
			Calendar start = b.getTripStartTime();
			Vector<Stop> trip = b.getTrip();
			Vector<Stop> utrip = PredictionHandler.updateTrip(tripId, start, trip, ses, false);
			Vector<Stop> ntrip = new Vector<Stop>();
			for (int j = 0; j < utrip.size(); j++) {
				ntrip.add(utrip.get(j).clone());
			}
			b.setTrip(ntrip);
		}

		// if(updateTimes==5){
		// for (int i = 0; i < buses.size(); i++) {
		// Bus b = buses.get(i);
		// System.out.println("bus id: "+b.busId+" - "+" shift: "+b.shift+"
		// "+b.getTrip().get(0));
		// }
		// }
		Random rnd = new Random();

		// update travel time using (k,k+1,P)
		for (int i = 0; i + 1 < buses.size(); i++) {
			Bus b1 = buses.get(i);
			Bus b2 = buses.get(i + 1);
			// if(updateTimes>5){
			// System.out.println(calendarToString(b1.getTrip().get(0).departureTime));
			// System.out.println(calendarToString(b2.getTrip().get(0).departureTime));
			// }
			Vector<Stop> trip1 = b1.getTrip();
			Vector<Stop> trip2 = b2.getTrip();
			for (int j = 0; j + 1 < trip1.size(); j++) {
				// s_tripId_stopId
				Stop s_1_1 = trip1.get(j);
				// System.out.println(calendarToString(trip1.get(0).departureTime));
				Stop s_2_1 = trip2.get(j);
				// System.out.println(calendarToString(trip2.get(0).departureTime));
				// System.out.println(s_1_1+" "+s_2_1);
				// System.out.println(calendarToString(s_1_1.departureTime));
				// System.out.println(calendarToString(s_2_1.departureTime));

				Calendar pTime = getPtime(s_1_1.departureTime, s_2_1.departureTime);
				// if(updateTimes>5){
				// System.out.println(calendarToString(pTime));
				// }
				// System.out.println(s_2_1.departureTime.HOUR_OF_DAY+"-"+s_2_1.departureTime.MINUTE+"-"+s_2_1.departureTime.SECOND);
				if (i == 0) {
					Stop s_1_2 = trip1.get(j + 1);
					Calendar departureTime_1_1 = (Calendar) s_1_1.getDepartureTime().clone();
					int travelTime = (int) PredictionHandler.getStats_TTS_k2K_(tripId, s_1_1.getStopId(),
							s_1_2.getStopId(), pTime, ses);
					// System.out.println(tripId+" "+s_1_1.getStopId()+"
					// "+s_1_2.getStopId()+" "+calendarToString(pTime));
					// System.out.println(travelTime);
					if (travelTime == 0) {
						// travelTime=120;
						travelTime = rnd.nextInt(120) % (120 - 60 + 1) + 60;
					}
					departureTime_1_1.add(Calendar.SECOND, travelTime);
					// System.out.println("=========="+(int)
					// PredictionHandler.getStats_TTS_k2K_(tripId,
					// s_1_1.getStopId(), s_1_2.getStopId(),
					// pTime, ses));
					s_1_2.setDepartureTime(departureTime_1_1);
					Stop s_2_2 = trip2.get(j + 1);
					Calendar departureTime_2_1 = (Calendar) s_2_1.getDepartureTime().clone();
					int travelTime2 = (int) PredictionHandler.getStats_TTS_k2K_(tripId, s_2_1.getStopId(),
							s_2_2.getStopId(), pTime, ses);
					// System.out.println(calendarToString(pTime));
					// System.out.println(s_2_1.getStopId()+" "+
					// s_2_2.getStopId()+" "+travelTime2);
					if (travelTime2 == 0) {
						// travelTime2=120;
						travelTime2 = rnd.nextInt(120) % (120 - 60 + 1) + 60;
					}
					departureTime_2_1.add(Calendar.SECOND, travelTime2);
					s_2_2.setDepartureTime(departureTime_2_1);
				} else {
					// if(updateTimes>5){
					// System.out.println("here!");
					// }
					Stop s_2_2 = trip2.get(j + 1);
					Calendar departureTime_2_1 = (Calendar) s_2_1.getDepartureTime().clone();
					int travelTime3 = (int) PredictionHandler.getStats_TTS_k2K_(tripId, s_2_1.getStopId(),
							s_2_2.getStopId(), pTime, ses);
					// System.out.println(s_2_1.getStopId()+" "+
					// s_2_2.getStopId()+" "+travelTime3+"
					// "+pTime.HOUR_OF_DAY+"-"+pTime.MINUTE+"-"+pTime.SECOND);
					if (travelTime3 == 0) {
						// travelTime3=120;
						travelTime3 = rnd.nextInt(120) % (120 - 60 + 1) + 60;
					}
					departureTime_2_1.add(Calendar.SECOND, travelTime3);
					s_2_2.setDepartureTime(departureTime_2_1);
				}
			}
		}

		// long t1 = Calendar.getInstance().getTime().getTime();
		// System.out.println("Time for updateTrip:" + (t1-t0) + "sec");
		// check minRestTime constraint
		// if (satisfyMinRestTime(buses) == false) {
		// return false;
		// }

		// update headways
		for (int i = 0; i + 1 < buses.size(); i++) {
			Bus b = buses.get(i);
			Vector<Stop> trip = b.getTrip();
			Bus b1 = buses.get(i + 1);
			Vector<Stop> trip1 = b1.getTrip();
			if (i == 0) {
				for (int j = 0; j < trip1.size(); j++) {
					int headway = (int) ((trip1.get(j).getDepartureTime().getTimeInMillis()
							- trip.get(j).getDepartureTime().getTimeInMillis()) / 1000);// s

					trip1.get(j).setHeadWay(headway);
					trip.get(j).setHeadWay(headway);
				}
			} else {
				for (int j = 0; j < trip1.size(); j++) {
					int headway = (int) ((trip1.get(j).getDepartureTime().getTimeInMillis()
							- trip.get(j).getDepartureTime().getTimeInMillis()) / 1000);// s

					trip1.get(j).setHeadWay(headway);
				}
			}
		}

		// long t2 = Calendar.getInstance().getTime().getTime();
		// System.out.println("Time for update headways:" + (t2-t1) + "sec");
		// update loads
		for (int i = 0; i < buses.size(); i++) {
			Bus b = buses.get(i);
			Vector<Stop> trip = b.getTrip();
			for (int j = 0; j + 1 < trip.size(); j++) {
				Stop s1 = trip.get(j);// k-1
				Stop s2 = trip.get(j + 1);// to be updated k

				int load = (int) (s2.getHeadWay() * s2.getArrivalRate() + s1.getLoad()
						- s2.getHeadWay() * s2.getAlightingRate());
//				if(updateTimes>5){
//				System.out.println(load);
//				}
				if (load < 0) {
					load = 0;
				}
				s2.setLoad(load);
			}

		}

		// long t3 = Calendar.getInstance().getTime().getTime();
		// System.out.println("Time for update loads:" + (t3-t2) + "sec");
		// System.out.println("Time for update all:" + (t3-t0) + "sec");

		// Bus bus=null;
		// for(int i=0;i<buses.size();i++){
		// System.out.println(buses.get(i));
		// bus=buses.get(i);
		// Vector<Stop> stops=new Vector<Stop>();
		// for(int j=0;j<stops.size();j++){
		// System.out.println(stops.get(j));
		//
		// }
		// }

		return true;

	}

	// return: true-satisfy false-not..
	private static boolean satisfyMinRestTime(Vector<Bus> buses) {
		int firstShiftIndex = 1;//
		boolean satisfyMiniRestTime = true;
		// abuses list: basic buses without duplicating by shifts
		Vector<Bus> abuses = new Vector<Bus>();
		for (int i = 0; i < buses.size(); i++) {
			Bus b = buses.get(i);
			// System.out.println(b.busId+" "+b.shift);
			if (b.getShift() == firstShiftIndex) {
				abuses.add(b);
			}
		}

		// for (int i = 0; i < abuses.size(); i++) {
		// Bus b=abuses.get(i);
		// System.out.println(b.busId+" "+b.shift);
		// }

		// sbuses list: classify buses by shifts
		Vector<Vector<Bus>> sbuses = new Vector<Vector<Bus>>();
		for (int i = 0; i < abuses.size(); i++) {
			Vector<Bus> tbuses = new Vector<Bus>();
			Bus b = abuses.get(i);
			String busId = b.getBusId();
			tbuses.add(b);
			for (int j = 0; j < buses.size(); j++) {
				Bus b1 = buses.get(j);
				String busId1 = b1.getBusId();
				int shift = b1.getShift();
				if (busId.equals(busId1) && shift > firstShiftIndex) {
					tbuses.add(b1);
				}
			}
			if (tbuses.size() > 1) {// a bus work for only 1 shift do not have
									// to consider miniRestTime
				sbuses.add(tbuses);
			}
		}

		// for(int i=0;i<sbuses.size();i++){
		// Vector<Bus> thebuses=sbuses.get(i);
		// for(int j=0;j<thebuses.size();j++){
		// Bus b=thebuses.get(j);
		// System.out.print("|"+b.busId+" "+b.shift);
		// }
		// System.out.println();
		// }

		// verify miniRestTime Constraint
		for (int i = 0; i < sbuses.size(); i++) {
			Vector<Bus> asbuses = sbuses.get(i);
			for (int j = 0; j + 1 < asbuses.size(); j++) {
				Bus b1 = asbuses.get(j);
				Bus b2 = asbuses.get(j + 1);
				// System.out.println(b1.busId+" shift "+b1.shift+" "+b2.busId+"
				// shift "+b2.shift);
				// System.out.println(b1.getTrip().get(0)+"
				// "+b2.getTrip().get(0));
				// System.out.println(b1.getTrip().get(35)+"
				// "+b2.getTrip().get(35));

				Calendar formerEnd = b1.getTripEndTime();
				Calendar latterStart = b2.getTripStartTime();
				int plannedRestTime = (int) ((latterStart.getTimeInMillis() - formerEnd.getTimeInMillis()) / 1000);
				// System.out.println(plannedRestTime);
				if (plannedRestTime < miniRestTime) {
					satisfyMiniRestTime = false;// check!
					break;
				}
			}
		}

		return satisfyMiniRestTime;
	}

	/**
	 * @param buses1
	 * @param buses2
	 * @return both vectors have to be updated
	 */
	public double getObjectiveValue(Vector<Bus> buses1, Vector<Bus> buses2) {
		return 0;
	}

	/**
	 * @param buses
	 * @return only one vector has to be updated
	 */
	public static double getObjectiveValue1(Vector<Bus> buses) {// Tianxiang
		double O1 = 0.0;

		for (int i = 0; i < buses.size(); i++) {
			// for(Vector<Bus>::iterator it= buses.begin(); it != buses.end();
			// it++ )
			Bus b = buses.get(i);
			int d = b.getDirection();
			if (d == 1) {
				Vector<Stop> stop = b.getTrip();
				for (int k = 0; k < stop.size(); k++) {
					Stop st = stop.get(k);
					double ar = st.getArrivalRate();
					// System.out.println(ar);
					int g = st.getHeadWay();
					if (b.getTripStartTime().before(endTime)) {// check!//////////
						st.setObjective1(ar * g * g / 2);
						O1 = O1 + ar * g * g / 2;
					} else {
						st.setObjective1(ar * g * g / 2);
					}
				}
			}
			if (d == 2) {
				Vector<Stop> stop = b.getTrip();
				for (int s = 0; s < stop.size(); s++) {
					Stop st = stop.get(s);
					double ar = st.getArrivalRate();
					int g = st.getHeadWay();
					if (b.getTripStartTime().before(endTime)) {
						st.setObjective1(ar * g * g / 2);
						O1 = O1 + ar * g * g / 2;
					} else {
						st.setObjective1(ar * g * g / 2);
					}
				}
			}

		}

		return O1;

	}

	public static double getObjectiveValue2(Vector<Bus> buses) {
		double O2 = 0.0;
		for (int i = 0; i < buses.size(); i++) {
			Bus b = buses.get(i);
			int d = b.getDirection();
			if (d == 1) {
				Vector<Stop> stop = b.getTrip();
				for (int k = 0; k + 1 < stop.size(); k++) {
					Stop st = stop.get(k);
					Stop stAfter = stop.get(k + 1);
					Calendar t = st.getDepartureTime();
					Calendar tAfter = stAfter.getDepartureTime();
					double TripTime = (tAfter.getTimeInMillis() - t.getTimeInMillis()) / 1000;
					double ar = st.getArrivalRate();
					double al = st.getAlightingRate();
					int c = b.getCapacity();
					int l = st.getLoad();
					int g = st.getHeadWay();
					double TempCompare = ar * g + (1 - al) * l - c;
					// System.out.println(TempCompare);
					if (TempCompare >= 0 && b.getTripStartTime().before(endTime)) {
						// if (TempCompare >= 0) {
						st.setObjective2(TripTime * TempCompare);
						O2 = O2 + TripTime * TempCompare;
					} else {
						st.setObjective2(TripTime * TempCompare);
					}

				}
			}

			if (d == 2) {
				Vector<Stop> stop = b.getTrip();
				for (int s = 0; s + 1 < stop.size(); s++) {
					Stop st = stop.get(s);
					Stop stAfter = stop.get(s + 1);
					Calendar t = st.getDepartureTime();
					Calendar tAfter = stAfter.getDepartureTime();
					double TripTime = (tAfter.getTimeInMillis() - t.getTimeInMillis()) / 1000;
					double ar = st.getArrivalRate();
					double al = st.getAlightingRate();
					int c = b.getCapacity();
					int l = st.getLoad();
					int g = st.getHeadWay();
					double TempCompare = ar * g + (1 - al) * l - c;
					if (TempCompare >= 0 && b.getTripStartTime().before(endTime)) {
						st.setObjective2(TripTime * TempCompare);
						O2 = O2 + TripTime * TempCompare;
					} else {
						st.setObjective2(TripTime * TempCompare);
					}

				}
			}
		}

		return O2;
	}

	public static double getObjectiveValue3(Vector<Bus> buses) {
		double O3 = 0.0;
		for (int i = 0; i < buses.size(); i++) {
			Bus b = buses.get(i);
			int d = b.getDirection();
			if (d == 1) {
				Vector<Stop> stop = b.getTrip();
				for (int k = 0; k < stop.size(); k++) {
					double fee = b.getCost();////////////////////cost is the fee of single stop?
					if (b.getTripStartTime().before(endTime)) {
						O3 = O3 + fee;
					}
				}
			}

			if (d == 2) {
				Vector<Stop> stop = b.getTrip();
				for (int s = 0; s < stop.size(); s++) {
					double fee = b.getCost();
					if (b.getTripStartTime().before(endTime)) {
						O3 = O3 + fee;
					}
				}
			}
		}

		return O3;

	}

	public static Solution getObjectiveValue(int[] gaps, Vector<Bus> buses, String tripId) {
		Vector<Bus> updatedbuses = gapsToBuses(gaps, buses);
		boolean feasible = update(updatedbuses, tripId);
		double TotalObj = 0.0;
		double w1 = weight1;
		double w2 = weight2;
		double w3 = weight3;
		// check satisfyMinRestTime +penalty
		double o1 = getObjectiveValue1(buses);////////////////////why buses, not updatedbuses?
		double o2 = getObjectiveValue2(buses);
		double o3 = getObjectiveValue3(buses);
		TotalObj = w1 * o1 + w2 * o2 + w3 * o3;
		// add penalty if some constraints are not satisfied.
		if (!feasible) {
			TotalObj += 1000000;
		}
		Solution s = new Solution(o1, o2, o3, TotalObj, updatedbuses);
		// System.out.println("TotalObj:"+TotalObj);
		return s;
	}

	public static double getObjectiveValue(int[] gaps) {
		return 0;

	}

	/**
	 * @param gaps
	 *            gaps maintained by GA.
	 * @param buses
	 *            bus list to be updated
	 * @return updated buses list
	 */
	public static Vector<Bus> gapsToBuses(int[] gaps, Vector<Bus> buses) {
		// create a new gaps2, first element is set to 0
		int[] gaps2 = new int[gaps.length + 1];
		gaps2[0] = 0;
		for (int i = 0; i < gaps.length; i++) {
			gaps2[i + 1] = gaps[i];
		}
		// update buses according to gaps
		int gapSum = 0;
		// for (int i = 0; i < buses.size(); i++) {
		for (int i = 0; i < gaps2.length; i++) {
			gapSum += gaps2[i];
			Bus b = buses.get(i);
			Calendar c = (Calendar) startTime.clone();
			c.add(Calendar.MINUTE, gapSum);
			b.setTripStartTime(c);
		}

		return buses;
	}

	public static Calendar getPtime(Calendar c1, Calendar c2) {
		Calendar c = (Calendar) c1.clone();
		int gap = (int) ((c2.getTimeInMillis() - c1.getTimeInMillis()) / 1000);
		c.add(Calendar.SECOND, gap / 2);
		return c;
	}

}
