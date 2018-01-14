package plannist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

public class Planner {

	//单向调度范例
	public static void main(String[] args) {
		//单（环）线，单向调度
		Session ses=PredictionHandler.createSession();
		// 1.构建路径，对开，环线有两个trip，分别输入，训练
		Vector<Stop> trip = new Vector<Stop>();
		for(int i=0;i<36;i++){
			trip.add(new Stop(i));
		}
		
		// 2.训练模型,测试数据已经训练好
//		PredictionHandler.trainTTSPModel("", "611", trip);// 时间预测模型
//		PredictionHandler.trainARPModel("", "611", trip);// 上客率预测模型
//		PredictionHandler.trainALPModel("", "611", trip);// 下客率预测模型

		//构造计划时间开始时间，该范例开始时间2016-03-25 08:00:01, 结束时间2016-03-25 10:00:01，当前时间2016-03-25 07:00:01
		Calendar startTime = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        try {
        	startTime.setTime(sdf.parse("2016-03-25 08:00:01"));
        }
        catch(ParseException pe){
            System.err.println(pe);
        }
		
		// 3.构造车辆，需传入调用程序时车的状态，参数详见Bus类，在发车场的车会以Vector中顺序依次排班
        Calendar currentTime1=(Calendar) startTime.clone(); currentTime1.add(Calendar.MINUTE, -60);
        Calendar currentTime2=(Calendar) startTime.clone(); currentTime2.add(Calendar.MINUTE, -55);
//    	public Bus(String （公车id）busId, int （最优载荷）capacity, double （单次发车成本）cost, boolean （是否在发车场，true=是，false=在途）depotStatus,Calendar （发车时间，只需在途车辆提供，在车场车辆不必提供）departureTime)
		Vector<Bus> buses = new Vector<Bus>();
		Bus b1 = new Bus("1771", 40, 200, false, currentTime1);
		buses.add(b1);
		Bus b2 = new Bus("1772", 40, 250, false, currentTime2);
		buses.add(b2);
		Bus b3 = new Bus("1773", 40, 250, true, null);
		buses.add(b3);
		Bus b4 = new Bus("1774", 40, 250, true, null);
		buses.add(b4);
		Bus b5 = new Bus("1775", 40, 250, true, null);
		buses.add(b5);
		Bus b6 = new Bus("1776", 40, 250, true, null);
		buses.add(b6);
		Bus b7 = new Bus("1777", 40, 250, true, null);
		buses.add(b7);
		Bus b8 = new Bus("1778", 40, 250, true, null);
		buses.add(b8);
		Bus b9 = new Bus("1779", 40, 250, true, null);
		buses.add(b9);
		Bus b10 = new Bus("1780", 40, 250,true, null);
		buses.add(b10);

		//加载模型需要传入站点id（不含首站）
//		int[] stopIDs=new int[trip.size()-1];
//		for(int i=0;i+1<trip.size();i++){
//			stopIDs[i]=i+1;
//		}
		int[] stopIDs=new int[trip.size()];
		for(int i=0;i<trip.size();i++){
			stopIDs[i]=i;
		}
		PredictionHandler.loadModels("611", stopIDs, ses,false);

		//4.传入用户参数，构建Optimizer对象，根据不同的路线类型，有一个trip的传入一个trip，两个的传入两个
		
		Calendar endTime = (Calendar) startTime.clone();
		endTime.add(Calendar.MINUTE, 40);
		Optimizer opt = new Optimizer(ses,buses, trip, "611", startTime, endTime,
				10, 20, 1, 120, 0.003,0.005,0.3);

		System.out.println("Optimization start...");
		//结果保存在solutions中，每条线路一个solution
		Vector<Solution> solutions=opt.run();
		
//		System.out.println("\nPrint solutions: ");
//		for(int j=0;j<solutions.size();j++){
//			Solution sol=solutions.get(j);
//			System.out.println("Best gaps: "+sol.getBestGaps());
//			System.out.println("Obj1: "+sol.getO1());
//			System.out.println("Obj2: "+sol.getO2());
//			System.out.println("Obj3: "+sol.getO3());
//			
//			Vector<Bus> busObjs=sol.getUpdatedbuses();
//			for(int i=0;i<busObjs.size();i++){
//	        	 Bus b=busObjs.get(i);
//	        	 System.out.println("====================");
//	        	 System.out.println("Bus: "+b.getBusId());
//	        	 System.out.println("====================");
//	        	 Vector<Stop> tp=b.getTrip();
//	        	 for(int k=0;k<tp.size();k++){
//	        		 Stop s=tp.get(k);
//	        		 System.out.println("Stop: "+s.getStopId());
//	        		 System.out.println("Objective 1: "+s.getObjective1());
//	        		 System.out.println("Objective 2: "+s.getObjective2());
//	        	 }
//	         }
//		}

	}
}
