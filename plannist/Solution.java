
package plannist;

import java.util.Vector;

public class Solution {
	double o1;//计划时间内该站点乘客总等待时间
	double o2;//计划时间内该站乘客上车后拥挤程度
	double o3;//运营费用，和车辆型号相关，可理解为发一次车所需花费
	double totalObj;//前三个指标之和
	Vector<Bus> updatedbuses;
	int[] bestGaps;//优化后排班间隔
	public Solution(double o1, double o2, double o3, double totalObj,
			Vector<Bus> updatedbuses) {
		super();
		this.o1 = o1;
		this.o2 = o2;
		this.o3 = o3;
		this.totalObj = totalObj;
		this.updatedbuses = updatedbuses;
	}
	public double getO1() {
		return o1;
	}
	public void setO1(double o1) {
		this.o1 = o1;
	}
	public double getO2() {
		return o2;
	}
	public void setO2(double o2) {
		this.o2 = o2;
	}
	public double getO3() {
		return o3;
	}
	public void setO3(double o3) {
		this.o3 = o3;
	}
	public double getTotalObj() {
		return totalObj;
	}
	public void setTotalObj(double totalObj) {
		this.totalObj = totalObj;
	}
	public Vector<Bus> getUpdatedbuses() {
		return updatedbuses;
	}
	public void setUpdatedbuses(Vector<Bus> updatedbuses) {
		this.updatedbuses = updatedbuses;
	}
	public int[] getBestGaps() {
		return bestGaps;
	}
	public void setBestGaps(int[] bestGaps) {
		this.bestGaps = bestGaps;
	}
	
	
	
	
	
}
