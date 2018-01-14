package plannist;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Vector;
import java.math.*;

public class Optimizer {
	Session ses;// session 对象，一次性把数据和预测模型加载到内存中
	Vector<Bus> buses1;// 车辆数组
	Vector<Stop> trip1;
	Vector<Stop> trip2;// 路径数组，单（环）线只存trip1，对发情况两个trip
	String tripID1;
	String tripID2;// 路径index，原理同上
	Calendar startTime;// 计划调度开始时间
	Calendar endTime;// 计划调度结束时间
	int gap1_min;// 从当前时间到计划调度结束（时段内）-最小发车间隔
	int gap1_max;// 从当前时间到计划调度结束（时段内）-最大发车间隔
	int tripType;// 线路类型 1： 单向调度 2：
	int miniRestTime;// 司机最短休息时间（分钟）
	double weight1;// 等待时间权重
	double weight2;// 拥挤情况权重
	double weight3;// 运营费用权重
	Vector<Bus> updatedBuses1;
	Vector<Bus> updatedBuses2;
	private Network nw;
	boolean usePrediction=false;

	public Optimizer(Session ses, Vector<Bus> buses1, Vector<Stop> trip1,
			String tripID1, Calendar startTime, Calendar endTime, int gap1_min,
			int gap1_max, int tripType, int miniRestTime, double weight1,
			double weight2, double weight3) {
		super();
		this.ses = ses;
		this.buses1 = buses1;
		this.trip1 = trip1;
		this.tripID1 = tripID1;
		this.startTime = startTime;
		this.endTime = endTime;
		this.gap1_min = gap1_min;
		this.gap1_max = gap1_max;
		this.tripType = tripType;
		this.miniRestTime = miniRestTime;
		this.weight1 = weight1;
		this.weight2 = weight2;
		this.weight3 = weight3;
	}

	public Optimizer(Session ses, Vector<Bus> buses1, Vector<Stop> trip1,
			Vector<Stop> trip2, String tripID1, String tripID2,
			Calendar startTime, Calendar endTime, int gap1_min, int gap1_max,
			int tripType, int miniRestTime, double weight1, double weight2,
			double weight3) {
		super();
		this.ses = ses;
		this.buses1 = buses1;
		this.trip1 = trip1;
		this.trip2 = trip2;
		this.tripID1 = tripID1;
		this.tripID2 = tripID2;
		this.startTime = startTime;
		this.endTime = endTime;
		this.gap1_min = gap1_min;
		this.gap1_max = gap1_max;
		this.tripType = tripType;
		this.miniRestTime = miniRestTime;
		this.weight1 = weight1;
		this.weight2 = weight2;
		this.weight3 = weight3;
	}

	public Vector<Bus> seegaps(Vector<Bus> editablebuses1) {
		Vector<Bus> starts = new Vector<Bus>();
		for (int i = 0; i < editablebuses1.size(); i++) {
			if (editablebuses1.elementAt(i).depotStatus == false) {
				starts.addElement(editablebuses1.elementAt(i));
			}

		}
		return starts;
	}


	public Vector<Bus> buses1copy(Vector<Bus> buses) {
		Vector<Bus> editablebuses1 = new Vector<Bus>();
		for (int i = 0; i < buses.size(); i++) {
			Bus bus = buses.get(i).clone();
			editablebuses1.addElement(bus);
		}
		return editablebuses1;
	}

	public Vector<Bus> initialbuses(Vector<Bus> buses, String tripID) {
		// construct the list of departed buses' departure time.
		Vector<Bus> busList = seegaps(buses);
		Vector<Bus> newbus = new Vector<Bus>();
		// sort those departure time;
		Collections.sort(busList);
		// find the nearest time from the data given time.
		Bus firstOne = busList.lastElement();
		// find the last bus leaves the depot for the data given time.
		// use this bus's departure time as a base line to generate the on depot
		// buses.
		Calendar departureTimeBase = firstOne.departureTime;
		Calendar BaseClone = (Calendar) departureTimeBase.clone();// bus.departuretime
		// initalising the departure time for the in depot buses
		int count = 1;
		for (int i = 0; i < buses.size(); i++) {
			Bus bus1 = buses.get(i);
			if (bus1.depotStatus == true) {
				Calendar c = (Calendar) BaseClone.clone();
				c.add(Calendar.MINUTE, count * gap1_min);
				bus1.setDepartureTime(c);
				count++;
			}
		}
		/*
		 * when the departure time is initialised, further process sometimes
		 * needed if the initialised time is still before the start time
		 */
		for (int i = 0; i < buses.size(); i++) {
			// get each bus'es departure time
			Calendar departureTime = buses.get(i).departureTime;
			// copy the departure time
			Calendar c = (Calendar) departureTime.clone();
			// calculate the time for one whole trip
			Stop finalStop1 = trip1.lastElement();
			int finalStopId1 = finalStop1.stopId;
			double period = PredictionHandler.getTravelTime(tripID,
					departureTime, ses,usePrediction);
			int timeSlot = (int) period + miniRestTime;
			// handle the process
			if (c.before(startTime)) {
				Calendar c1 = (Calendar) c.clone();
				c1.add(Calendar.SECOND, timeSlot);
				if (c1.after(endTime)) {
					buses.get(i).setDepartureTime(c1);
				} else if (c1.before(startTime)) {
					while (c1.after(startTime)) {
						c1.add(Calendar.SECOND, timeSlot);
						buses.get(i).setDepartureTime(c1);
						newbus.addElement(buses.get(i));
					}
				} else {
					buses.get(i).setDepartureTime(c1);
					newbus.addElement(buses.get(i));
				}
			} else if (c.after(endTime)) {
				buses.get(i).setDepartureTime(c);
			} else if ((c.after(startTime) || c.equals(startTime))
					&& c.before(endTime)) {
				newbus.addElement(buses.get(i));
			}
		}
		return newbus;
	}

	public Vector<Vector<Bus>> initialbuses2(Vector<Bus> buses1,
			Vector<Bus> buses2, String tripID1, String tripID2) {
		Vector<Bus> busList1 = seegaps(buses1);
		Vector<Vector<Bus>> newbus = new Vector<Vector<Bus>>();
		Vector<Bus> newbus1 = new Vector<Bus>();
		Vector<Bus> newbus2 = new Vector<Bus>();
		Collections.sort(busList1);
		Bus firstOne = busList1.lastElement();
		Calendar departureTimeBase = firstOne.departureTime;
		Calendar BaseClone = (Calendar) departureTimeBase.clone();
		int count = 1;
		for (int i = 0; i < buses1.size(); i++) {
			Bus bus1 = buses1.get(i);
			if (bus1.depotStatus == true) {
				Calendar c = (Calendar) BaseClone.clone();
				c.add(Calendar.MINUTE, count * gap1_min);
				bus1.setDepartureTime(c);
				count++;
			}
		}

		Vector<Bus> busList2 = seegaps(buses2);
		Collections.sort(busList2);
		Bus firstOne2 = busList2.lastElement();
		Calendar departureTimeBase2 = firstOne2.departureTime;
		Calendar BaseClone2 = (Calendar) departureTimeBase2.clone();
		int counte = 1;
		for (int i = 0; i < buses2.size(); i++) {
			Bus bus2 = buses2.get(i);
			if (bus2.depotStatus == true) {
				Calendar c1 = (Calendar) BaseClone2.clone();
				c1.add(Calendar.MINUTE, counte * gap1_min);
				bus2.setDepartureTime(c1);
				counte++;
			}
		}

		for (int i = 0; i < buses1.size(); i++) {
			Calendar departureTime = buses1.get(i).departureTime;
			Calendar c = (Calendar) departureTime.clone();
			Stop finalStop1 = trip1.lastElement();
			int finalStopId1 = finalStop1.stopId;
			Stop finalStop2 = trip2.lastElement();
			int finalStopId2 = finalStop2.stopId;
			double period1 = PredictionHandler.getTravelTime(tripID1,
					departureTime, ses,usePrediction);
			int timeSlot1 = (int) period1 + miniRestTime;
			Calendar c2 = (Calendar) departureTime.clone();
			c2.add(Calendar.SECOND, timeSlot1);
			double period2 = PredictionHandler.getTravelTime(tripID2,
					departureTime, ses,usePrediction);
			int timeSlot2 = (int) period2 + miniRestTime;
			/*
			 * counting is used for determine the bus' direction, so it can be
			 * added to the right depot with right departure time
			 */
			int counting = 1;
			if (c.before(startTime)) {
				c.add(Calendar.SECOND, timeSlot1);
				/*
				 * when the direction change, the trip change and the depot
				 * change, so the time should be reset
				 */
				buses1.get(i).setDepartureTime(c);
				buses1.get(i).setDirection(2);
				if (c.after(endTime)) {
					buses1.get(i).setDepartureTime(c);
				} else if (c.before(startTime)) {
					while (c.before(startTime)) {
						c.add(Calendar.SECOND, timeSlot2);
						buses1.get(i).setDepartureTime(c);
						buses1.get(i).setDirection(1);
						counting++;
						if (c.before(startTime)) {
							c.add(Calendar.SECOND, timeSlot1);
							buses1.get(i).setDepartureTime(c);
							buses1.get(i).setDirection(2);
							counting++;
						}

					}
					if (counting / 2 == 0) {
						/*
						 * if it is a whole journey and back to the beginning,
						 * set as depart from depot A
						 */
						newbus1.addElement(buses1.get(i));
					} else {
						newbus2.addElement(buses1.get(i));
					}
				} else {
					newbus2.addElement(buses1.get(i));
				}
			} else if (c.after(startTime) && c.before(endTime)) {
				newbus1.addElement(buses1.get(i));
			}
		}

		for (int i = 0; i < buses2.size(); i++) {
			Calendar departureTime = buses2.get(i).departureTime;
			Calendar c = (Calendar) departureTime.clone();
			Stop finalStop1 = trip1.lastElement();
			int finalStopId1 = finalStop1.stopId;
			Stop finalStop2 = trip2.lastElement();
			int finalStopId2 = finalStop2.stopId;
			double period1 = PredictionHandler.getTravelTime(tripID2,
					departureTime, ses,usePrediction);
			int timeSlot1 = (int) period1 + miniRestTime;
			Calendar c2 = (Calendar) departureTime.clone();
			c2.add(Calendar.SECOND, timeSlot1);
			double period2 = PredictionHandler.getTravelTime(tripID1,
					departureTime, ses,usePrediction);
			int timeSlot2 = (int) period2 + miniRestTime;
			int counting = 1;
			if (c.before(startTime)) {
				c.add(Calendar.SECOND, timeSlot1);
				buses2.get(i).setDepartureTime(c);
				buses2.get(i).setDirection(1);
				if (c.after(endTime)) {
					buses1.get(i).setDepartureTime(c);
				} else if (c.before(startTime)) {
					while (c.before(startTime)) {
						c.add(Calendar.SECOND, timeSlot2);
						buses2.get(i).setDepartureTime(c);
						buses2.get(i).setDirection(2);
						counting++;
						if (c.before(startTime)) {
							c.add(Calendar.SECOND, timeSlot1);
							buses2.get(i).setDepartureTime(c);
							buses2.get(i).setDirection(1);
							counting++;
						}

					}
					if (counting / 2 == 0) {
						newbus2.addElement(buses2.get(i));
					} else {
						newbus1.addElement(buses2.get(i));
					}
				} else {
					newbus1.addElement(buses2.get(i));
				}
			} else if ((c.after(startTime) || c.equals(startTime))
					&& c.before(endTime)) {
				newbus2.addElement(buses2.get(i));
			}
		}
		newbus.addElement(newbus1);
		newbus.addElement(newbus2);
		return newbus;
	}

	public Vector<Bus> initialbuses4(Vector<Bus> buses1, String tripID1,
			String tripID2) {
		Vector<Bus> busList = seegaps(buses1);
		Vector<Bus> newbus = new Vector<Bus>();
		Collections.sort(busList);
		Bus firstOne = busList.lastElement();
		Calendar departureTimeBase = firstOne.departureTime;
		Calendar BaseClone = (Calendar) departureTimeBase.clone();
		int count = 1;
		for (int i = 0; i < buses1.size(); i++) {
			Bus bus1 = buses1.get(i);
			if (bus1.depotStatus == true) {
				Calendar c = (Calendar) BaseClone.clone();
				c.add(Calendar.MINUTE, count * gap1_min);
				bus1.setDepartureTime(c);
				count++;
			}
		}

		for (int i = 0; i < buses1.size(); i++) {
			Calendar departureTime = buses1.get(i).departureTime;
			Calendar c = (Calendar) departureTime.clone();
			Stop finalStop1 = trip1.lastElement();
			int finalStopId1 = finalStop1.stopId;
			Stop finalStop2 = trip2.lastElement();
			int finalStopId2 = finalStop2.stopId;
			double period1 = PredictionHandler.getTravelTime(tripID1,
					departureTime, ses,usePrediction);
			int timeSlot1 = (int) period1 + miniRestTime;
			Calendar c1 = (Calendar) c.clone();
			c1.add(Calendar.SECOND, timeSlot1);
			double period2 = PredictionHandler.getTravelTime(tripID2,
					departureTime, ses,usePrediction);
			int timeSlot2 = (int) period2 + miniRestTime;
			int timeSlot = timeSlot1 + timeSlot2;
			if (c.before(startTime)) {
				c.add(Calendar.SECOND, timeSlot);
				if (c.after(endTime)) {
					buses1.get(i).setDepartureTime(c1);
				} else if (c.before(startTime)) {
					while (c.before(startTime)) {
						c.add(Calendar.SECOND, timeSlot);
						buses1.get(i).setDepartureTime(c);
						if (c.after(startTime)) {
							newbus.addElement(buses1.get(i));
						}
					}
				} else {
					buses1.get(i).setDepartureTime(c);
					newbus.addElement(buses1.get(i));
				}
			} else if ((c.after(startTime) || c.equals(startTime))
					&& c.before(endTime)) {
				newbus.addElement(buses1.get(i));
			}
		}

		return newbus;

	}

	public Vector<Vector<Bus>> splitdirection(Vector<Bus> buses) {
		Vector<Bus> newbuses1 = new Vector<Bus>();
		Vector<Bus> newbuses2 = new Vector<Bus>();
		Vector<Vector<Bus>> newbuses = new Vector<Vector<Bus>>();
		for (int i = 0; i < buses.size(); i++) {
			Bus bus = buses.get(i).clone();
			if (bus.direction == 1) {
				newbuses1.addElement(bus);

			} else {
				newbuses2.addElement(bus);

			}
		}
		newbuses.addElement(newbuses1);
		newbuses.addElement(newbuses2);

		return newbuses;
	}

	public Vector<Bus> splitbuses1(Vector<Vector<Bus>> vectorBuses) {
		Vector<Bus> newbuses1 = new Vector<Bus>();
		newbuses1 = vectorBuses.firstElement();
		return newbuses1;
	}

	public Vector<Bus> splitbuses2(Vector<Vector<Bus>> vectorBuses) {
		Vector<Bus> newbuses2 = new Vector<Bus>();
		newbuses2 = vectorBuses.lastElement();
		return newbuses2;
	}

	public void busHandler(Vector<Bus> initializedBus, Vector<Bus> Buses1,
			Vector<Bus> fBuses1, int finalStopId) {
		for (int i = 0; i < initializedBus.size(); i++) {
			Bus bus = initializedBus.get(i).clone();
			int period = (int) PredictionHandler.getTravelTime(tripID1,
					bus.departureTime, ses,usePrediction) + miniRestTime;
			int count = 1;
			for (Calendar c1 = (Calendar) bus.departureTime.clone(); c1
					.before(endTime); c1.add(Calendar.SECOND, period)) {
				Bus bus1 = (Bus) bus.clone();
				bus1.setTrip(trip1);
				Calendar c2 = (Calendar) c1.clone();
				bus1.setDepartureTime(c2);
				bus1.setShift(count);
				fBuses1.addElement(bus1);
				count++;
			}

		}
		for (int i = 0; i < fBuses1.size(); i++) {
			if (fBuses1.get(i).departureTime.before(endTime)) {
				Buses1.addElement(fBuses1.get(i));
			}
		}
		Collections.sort(Buses1);
	}

	public void busHandler31(Vector<Bus> initializedBus, Vector<Bus> Buses1,
			Vector<Bus> Buses2, int finalStopId1, int finalStopId2) {
		for (int i = 0; i < initializedBus.size(); i++) {
			Bus bus = initializedBus.get(i).clone();
			double period1 = PredictionHandler.getTravelTime(tripID1,
					bus.departureTime, ses,usePrediction);
			int timeSlot1 = (int) period1 + miniRestTime;
			Calendar c1 = (Calendar) bus.departureTime.clone();
			c1.add(Calendar.SECOND, timeSlot1);
			double period2 = PredictionHandler.getTravelTime(tripID2,
					c1, ses,usePrediction);
			
			int timeSlot2 = (int) period2 + miniRestTime;
			int timeSlot = timeSlot1 + timeSlot2;
			int count = 1;
			Calendar c = (Calendar) bus.departureTime.clone();
			while (c.before(endTime)) {
				Bus bus1 = (Bus) bus.clone();
				bus1.setTrip(trip1);
				bus1.setDirection(1);
				bus1.setShift(count);
				Calendar c2 = (Calendar) c.clone();
				Buses1.addElement(bus1);
				bus1.setDepartureTime(c2);
				Calendar c3 = (Calendar) c2.clone();
				c3.add(Calendar.SECOND, timeSlot1);
				if (c3.before(endTime)) {
					Bus bus2 = (Bus) bus1.clone();
					bus2.setTrip(trip2);
					bus2.setDirection(2);
					bus2.setDepartureTime(c3);
					Buses2.addElement(bus2);
				}
				c.add(Calendar.SECOND, timeSlot);
				count++;
			}

		}
	}

	public void busHandler32(Vector<Bus> initializedBus, Vector<Bus> Buses1,
			Vector<Bus> Buses2, int finalStopId1, int finalStopId2) {
		for (int i = 0; i < initializedBus.size(); i++) {
			Bus bus = initializedBus.get(i).clone();
			double period1 = 
			PredictionHandler.getTravelTime(tripID2,
					bus.departureTime, ses,usePrediction);
			int timeSlot1 = (int) period1 + miniRestTime;
			Calendar c1 = (Calendar) bus.departureTime.clone();
			c1.add(Calendar.SECOND, timeSlot1);
			double period2 = 
			PredictionHandler.getTravelTime(tripID1,
					c1, ses,usePrediction);
			int timeSlot2 = (int) period2 + miniRestTime;
			int timeSlot = timeSlot1 + timeSlot2;
			int count = 1;
			Calendar c = (Calendar) bus.departureTime.clone();
			while (c.before(endTime)) {
				Bus bus1 = (Bus) bus.clone();
				bus1.setTrip(trip2);
				bus1.setDirection(2);
				bus1.setShift(count);
				Calendar c2 = (Calendar) c.clone();
				Buses2.addElement(bus1);
				bus1.setDepartureTime(c2);
				Calendar c3 = (Calendar) c2.clone();
				c3.add(Calendar.SECOND, timeSlot1);
				if (c3.before(endTime)) {
					Bus bus2 = (Bus) bus1.clone();
					bus2.setTrip(trip1);
					bus2.setDirection(1);
					bus2.setDepartureTime(c3);
					Buses1.addElement(bus2);
				}
				c.add(Calendar.SECOND, timeSlot);
				count++;
			}

		}
	}

	public void busHandler4(Vector<Bus> initializedBus, Vector<Bus> Buses1,
			Vector<Bus> fBuses1, int finalStopId1, int finalStopId2) {
		for (int i = 0; i < initializedBus.size(); i++) {
			Bus bus = initializedBus.get(i).clone();
			double period1 = PredictionHandler.getTravelTime(tripID1,
					bus.departureTime, ses,usePrediction);
			
			int timeSlot1 = (int) period1 + miniRestTime;
			Calendar c1 = (Calendar) bus.departureTime.clone();
			c1.add(Calendar.SECOND, timeSlot1);
			double period2 = PredictionHandler.getTravelTime(tripID2, c1,
					 ses,usePrediction);
			int timeSlot2 = (int) period2 + miniRestTime;
			int timeSlot = timeSlot1 + timeSlot2;
			int count = 1;
			for (Calendar c = (Calendar) bus.departureTime.clone(); c
					.before(endTime); c.add(Calendar.SECOND, timeSlot)) {
				Bus bus1 = (Bus) bus.clone();
				bus1.setTrip(trip1);
				bus1.setDirection(1);
				fBuses1.addElement(bus1);
				bus1.setShift(count);
				count++;
			}

		}

		for (int i = 0; i < fBuses1.size(); i++) {
			if (fBuses1.get(i).departureTime.before(endTime)) {
				Buses1.addElement(fBuses1.get(i));
			}
		}
		Collections.sort(Buses1);
	}

	public void prePareNetWork() {
		Vector<Bus> Buses1 = new Vector<Bus>();
		Vector<Bus> Buses2 = new Vector<Bus>();
		Vector<Bus> fBuses1 = new Vector<Bus>();
		Vector<Bus> fBuses2 = new Vector<Bus>();

		switch (tripType) {
		case 1:
			Stop finalStop1 = trip1.lastElement();
			int finalStopId1 = finalStop1.stopId;
			Vector<Bus> trip1buses = buses1copy(buses1);
			Vector<Bus> inibuses = initialbuses(trip1buses, tripID1);
			busHandler(inibuses, Buses1, fBuses1, finalStopId1);
			break;

		case 2:
			Stop finalStop21 = trip1.lastElement();
			int finalStopId21 = finalStop21.stopId;
			Stop finalStop22 = trip2.lastElement();
			int finalStopId22 = finalStop22.stopId;
			Vector<Bus> tripbuses = buses1copy(buses1);
			Vector<Vector<Bus>> split = splitdirection(tripbuses);
			Vector<Bus> direction1 = splitbuses1(split);
			Vector<Bus> direction2 = splitbuses2(split);
			Vector<Bus> inibuses1 = initialbuses(direction1, tripID1);
			Vector<Bus> inibuses2 = initialbuses(direction2, tripID1);
			busHandler(inibuses1, Buses1, fBuses1, finalStopId21);
			busHandler(inibuses2, Buses2, fBuses2, finalStopId22);
			break;

		case 3:
			Stop finalStop31 = trip1.lastElement();
			Stop finalStop32 = trip2.lastElement();
			int finalStopId31 = finalStop31.stopId;
			int finalStopId32 = finalStop32.stopId;
			Vector<Bus> trip3buses = buses1copy(buses1);
			Vector<Vector<Bus>> firstSplit = splitdirection(trip3buses);
			Vector<Bus> fromAtoB = splitbuses1(firstSplit);
			Vector<Bus> fromBtoA = splitbuses2(firstSplit);
			Vector<Vector<Bus>> inibuses3 = initialbuses2(fromAtoB, fromBtoA,
					tripID1, tripID2);
			Vector<Bus> iniFromAtoB = splitbuses1(inibuses3);
			Vector<Bus> iniFromBtoA = splitbuses2(inibuses3);
			busHandler31(iniFromAtoB, fBuses1, fBuses2, finalStopId31,
					finalStopId32);
			busHandler32(iniFromBtoA, fBuses1, fBuses2, finalStopId31,
					finalStopId32);
			for (int i = 0; i < fBuses1.size(); i++) {
				if (fBuses1.get(i).departureTime.before(endTime)) {
					Buses1.addElement(fBuses1.get(i));
				}
			}
			Collections.sort(Buses1);

			for (int i = 0; i < fBuses2.size(); i++) {
				if (fBuses2.get(i).departureTime.before(endTime)) {
					Buses2.addElement(fBuses2.get(i));
				}
			}
			Collections.sort(Buses2);

			break;

		case 4:
			Stop finalStop41 = trip1.lastElement();
			int finalStopId41 = finalStop41.stopId;
			Stop finalStop42 = trip2.lastElement();
			int finalStopId42 = finalStop42.stopId;
			Vector<Bus> trip4buses = buses1copy(buses1);
			Vector<Bus> inibuses4 = initialbuses4(trip4buses, tripID1, tripID2);
			busHandler4(inibuses4, Buses1, fBuses1, finalStopId41,
					finalStopId42);
			break;
		}

		updatedBuses1 = Buses1;
		updatedBuses2 = Buses2;

	}

	public Vector<Solution> run() {
		Vector<Solution> solutions = new Vector<Solution>();
		prePareNetWork();

		 nw.buses1 = updatedBuses1;
		 nw.buses2 = updatedBuses2;
		 nw.startTime = startTime;
		 nw.endTime = endTime;
		 nw.tripID1 = tripID1;
		 nw.tripID2 = tripID2;
		 nw.miniRestTime = miniRestTime;
		 nw.weight1 = weight1;
		 nw.weight2 = weight2;
		 nw.weight3 = weight3;
		 nw.ses = ses;
		
		 GA.mingap = gap1_min;
		 GA.maxgap = gap1_max;
		 GA.buses1 = nw.buses1;
		 GA.buses2 = nw.buses2;
		 GA.tripId1 = tripID1;
		 GA.tripId2 = tripID2;
		 solutions  = GA.run();
		 return solutions;
	}
}
