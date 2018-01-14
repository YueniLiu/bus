
package plannist;

import java.util.Vector;


public class FitnessCalc {
	static Vector<Bus> buses = null;
	static String tripId= null;

    static Solution getFitness(Individual individual) {
    	//int fitness = 0;
//        for (int i = 0; i < individual.size(); i++) {
//            fitness = fitness + individual.getGene(i);
//    	}
//        return -fitness;
    	int[] frontGenes = new int[buses.size()-1];
//    	System.out.println("individual.getGenes(): "+frontGenes.length);
//    	System.out.println("buses: "+buses.size());
//    	System.out.println("individual.size() "+individual.size());
    	for(int i=0; i<frontGenes.length;i++)
    	{
    		frontGenes[i] = individual.getGene(i); 
    	}
        Solution s =  Network.getObjectiveValue(frontGenes,buses,tripId);
      //fitness=(int) s.getTotalObj();
      //return -fitness;
      return s;
    }
    
    static Solution getFitnessP(PartialIndividual individual) {
//		 Solution s = Network.getObjectiveValue(individual.getGenes(), buses, tripId);
    	    int[] frontGenes = new int[buses.size()-1];
	    	for(int i=0; i<frontGenes.length;i++)
	    	{
	    		frontGenes[i] = individual.getGene(i); 
	    	}
	        Solution s =  Network.getObjectiveValue(frontGenes,buses,tripId);
		return s;
	    }
    
}