package plannist;
import java.util.Calendar;
import java.util.Random;
import java.util.Vector;

public class GA {


    private static final double uniformRate = 0.5;
    private static final double mutationRate = 0.015;
    private static final int tournamentSize = 2;
    private static final int generation = 20;
    private static final int popsize = 20;
    private static final boolean elitism = true;
	public static int step = 1;

    public static int mingap = 10;
    public static int maxgap = 20;
    public static Vector<Bus> buses1=new Vector<Bus>();
    public static Vector<Bus> buses2=new Vector<Bus>();
    public static Vector<Bus> buses_all=new Vector<Bus>();
    public static int gaplength;//the length of it is depending on length of (buses1+buses2) 
    public static String tripId=null;
    public static String tripId1=null;
    public static String tripId2=null;

    public static Population evolvePopulation(Population pop, Vector<Bus> bs, String Tid) {
    	FitnessCalc.buses=bs;
    	FitnessCalc.tripId=Tid;
//    	System.out.println("pop.size(): "+pop.size());
        Population newPopulation = new Population(pop.size(), true);

        if (elitism) {
            newPopulation.saveIndividual(0, pop.getFittest());
        }
//        System.out.println("Crossover in operation");
        int elitismOffset;
        if (elitism) {
            elitismOffset = 1;
        } else {
            elitismOffset = 0;
        }
        for (int i = elitismOffset; i < pop.size(); i++) {
            Individual indiv1 = tournamentSelection(pop);
            Individual indiv2 = tournamentSelection(pop);
            Individual newIndiv = crossover(indiv1, indiv2);
            newPopulation.saveIndividual(i, newIndiv);
//          System.out.println("Crossover in operation");
        }

        for (int i = elitismOffset; i < newPopulation.size(); i++) {
            mutate(newPopulation.getIndividual(i));
            //System.out.println("mutation in operation");
        }

        //local search
        int[] GA_generation_solution = new int[gaplength];
        for (int i = 0; i <gaplength; i++){
        	GA_generation_solution[i] = newPopulation.getFittest().getGene(i);
        }
        
  //      boolean flag=true;
        Individual LocalIndiv = newPopulation.getIndividual(0);
        int i=0;
        while(i< gaplength-3)
        {      
        	int currentbestfit = newPopulation.getFittest().getFitness();
	        //for (int i = 0; i <gaplength; i++){
	        	for (int j = 1; j < step; j++){
	        		if (GA_generation_solution[i]-j > mingap)
	        		{
	        			for (int k = 0; k < gaplength; k++)
	        			{
	        				if (k!=i)
	        				{	        					
	        					LocalIndiv.setGene(k, GA_generation_solution[k]);
	        				}
	        				else
	        				{		        				
	        					LocalIndiv.setGene(k, GA_generation_solution[k]-j);
	        				}
	        			}
	        			int obj= LocalIndiv.getFitness();
	        			if (obj > currentbestfit)
	        			{
	        				//System.out.println("Local search improves the solution");
	        				newPopulation.saveIndividual(0, LocalIndiv);
	        				GA_generation_solution= LocalIndiv.getGenes().clone();
	        				currentbestfit = obj;
	        				//i=-1;
	        				//break;
	        			}
	        		}	        		
	        		if (GA_generation_solution[i]+j < maxgap)
	        		{
	        			for (int k = 0; k < gaplength; k++)
	        			{
	        				
	        				if (k!=i)
	        				{
	        					LocalIndiv.setGene(k, GA_generation_solution[k]);
	        				}
	        				else
	        				{
	        					LocalIndiv.setGene(k, GA_generation_solution[k]+j);
	        				}	     
	        			}
	        			int obj= LocalIndiv.getFitness();
	        			if (obj > currentbestfit)
	        			{
	        				//System.out.println("Local search improves the solution");
	        				newPopulation.saveIndividual(0, LocalIndiv);
	        				GA_generation_solution= LocalIndiv.getGenes().clone();
	        				currentbestfit = obj;
	        				//i=-1;
	        				//break;
	        			}        			
	        		}	        		
	        	}
	        	i++;
	       // }
        }
        
        for (int j = 0; j <gaplength; j++){
        	GA_generation_solution[j] = newPopulation.getFittest().getGene(j);
        }
        
        while (i < gaplength-3)
        {
        	int currentbestfit = newPopulation.getFittest().getFitness();    
        	//int currentPartialbestfit = newPopulation.getFittest().getPartialFitness(i);  
        	PartialIndividual ind = new PartialIndividual();
        	PartialIndividual Preind = new PartialIndividual();
			ind.setGeneLength(gaplength-i);
//			System.out.println("ind121"+ind.size());
			Preind.setGeneLength(i);
			int currentPartialbestfit = Preind.getFitness(); 
        	for (int j = 1; j < step; j++){
        		if (GA_generation_solution[i]-j > mingap)
        		{
        			for (int k = 0; k <ind.size(); k++)
        			{      				
        				ind.setGene(k, GA_generation_solution[k]-j);
        			}
        			int obj= currentPartialbestfit + ind.getFitness();
        			//System.out.println("obj"+obj);
        			if (obj > currentbestfit)
        			{
        				for (int k = 0; k <i; k++)
            			{      				
        					LocalIndiv.setGene(k, newPopulation.getFittest().getGene(k));
            			}
        				
        				newPopulation.saveIndividual(0, LocalIndiv);
        				GA_generation_solution= LocalIndiv.getGenes().clone();
        				currentbestfit = obj;
        				//i=-1;
        				break;
        			}
        		}
        		
        		if (GA_generation_solution[i]+j < maxgap)
        		{
        			for (int k = 0; k <ind.size(); k++)
        			{      				
        				ind.setGene(k, GA_generation_solution[k]-j);
        			}

        			int obj= currentPartialbestfit + ind.getFitness();
        			//System.out.println("obj"+obj);
        			if (obj > currentbestfit)
        			{
        				for (int k = 0; k <i; k++)
            			{      				
        					LocalIndiv.setGene(k, newPopulation.getFittest().getGene(k));
            			}
        				
        				newPopulation.saveIndividual(0, LocalIndiv);
        				GA_generation_solution= LocalIndiv.getGenes().clone();
        				currentbestfit = obj;
        				//i=-1;
        				break;
        			}
        		}
        	}
        	i++;
        }


        return newPopulation;
    }

    private static Individual crossover(Individual indiv1, Individual indiv2) {
        Individual newSol = new Individual();
        for (int i = 0; i < indiv1.size(); i++) {
            if (Math.random() <= uniformRate) {
                newSol.setGene(i, indiv1.getGene(i));
            } else {
                newSol.setGene(i, indiv2.getGene(i));
            }
        }
        return newSol;
    }

    private static void mutate(Individual indiv) {
        Random rnd = new Random();
        for (int i = 0; i < indiv.size(); i++) {
            if (Math.random() <= mutationRate) {
            	int gene = rnd.nextInt(maxgap)%(maxgap-mingap+1) + mingap;
                indiv.setGene(i, gene);
            }
        }
    }

    private static Individual tournamentSelection(Population pop) {
        Population tournament = new Population(tournamentSize, false);
        for (int i = 0; i < tournamentSize; i++) {
        	int randomId = (int) (Math.random() * pop.size());
            tournament.saveIndividual(i, pop.getIndividual(randomId));
        }
        Individual fittest = tournament.getFittest();
        return fittest;
    }

    public static Vector<Solution> run(){
    	//System.out.println("GA starts");
    	int gapLength=buses1.size()-1;
    	if(!buses2.isEmpty()){
    		gapLength+=buses2.size();
    	}
    	buses_all.addAll(buses1);
    	buses_all.addAll(buses2);
//    	System.out.println("busall "+buses_all.size());
    	gaplength=gapLength;
    	if(buses1.isEmpty() || buses2.isEmpty())
    	{   		
    	Population myPop = new Population(popsize, true);
    	for (int i = 0; i <generation; i++){
//    		System.out.println("gaplength:"+gaplength);
    		myPop = evolvePopulation(myPop,buses_all,tripId1);
    		System.out.println("Generation: " + i + " Fittest: " + -myPop.getFittest().getFitness());
    		System.out.print("Gaps: ");
    		for (int j = 0; j <gaplength; j++){
            	System.out.print(myPop.getFittest().getGene(j) + ", ");
            }
    		System.out.println();
    		    		
    	}
    	 Individual ft = new Individual();
         ft = myPop.getFittest();
         System.out.print("Best Gap: ");
         int[] bestGaps=new int[gaplength];
         for (int i = 0; i <gaplength; i++){
         	System.out.print(ft.getGene(i)+", ");
         	bestGaps[i]=ft.getGene(i);
         }
         double obj1=ft.geto1();
         double obj2=ft.geto2();
         double obj3=ft.geto3();
         double bestFit=-ft.getFitness();
         System.out.println();
         System.out.println("Best Fittest: " + bestFit);
         System.out.println("Objective 1 value: " + obj1);
         System.out.println("Objective 2 value: " + obj2);
         System.out.println("Objective 3 value: " + obj3);
         System.out.println();
         System.out.println("Objectives for each bus and stop:");
         Vector<Bus> busObjs=ft.getbus();
         for(int i=0;i<busObjs.size();i++){
        	 Bus b=busObjs.get(i);
        	 System.out.println("====================");
        	 System.out.println("Bus: "+b.getBusId());
        	 System.out.println("====================");
        	 Vector<Stop> trip=b.getTrip();
        	 for(int k=0;k<trip.size();k++){
        		 Stop s=trip.get(k);
        		 System.out.println("Stop: "+s.getStopId());
        		 System.out.println("Objective 1: "+s.getObjective1());
        		 System.out.println("Objective 2: "+s.getObjective2());
        	 }
         }
         Vector<Solution> solutions = new Vector<Solution>();
         Solution solution=new Solution(obj1, obj2, obj3, bestFit, busObjs);
         solution.setBestGaps(bestGaps);
         solutions.add(solution);
         return solutions;
    }
    else
    {
    	Population myPop1 = new Population(popsize, true);
    	Population myPop2 = new Population(popsize, true);
    	for (int i = 0; i <generation; i++){
//    		System.out.println(tripId1);
//    		System.out.println("abs"+buses1.size());
//    		gaplength = buses1.size()-1;
//    		System.out.println("dfg"+gaplength);
    		myPop1 = evolvePopulation(myPop1,buses1,tripId1);
//    		myPop1 = evolvePopulation(myPop1,buses_all,tripId1);
    		System.out.println("Generation: " + i + " Fittest for trip1: " + -myPop1.getFittest().getFitness());
    		System.out.print("Gaps: ");
    		for (int j = 0; j <gaplength; j++){
            	System.out.print(myPop1.getFittest().getGene(j) + ", ");
            }
    		System.out.println();
    		gaplength = buses2.size()-1;
    		myPop2 = evolvePopulation(myPop2,buses2,tripId2);
    		System.out.println("Generation: " + i + " Fittest for trip2: " + -myPop2.getFittest().getFitness());
    		System.out.print("Gaps: ");
    		for (int j = 0; j <gaplength; j++){
            	System.out.print(myPop2.getFittest().getGene(j) + ", ");
            }
    		System.out.println();
    		    		
    	}
    	 Individual ft1 = new Individual();
    	 Individual ft2 = new Individual();
         ft1 = myPop1.getFittest();
         System.out.print("Best Gap for trip1: ");
         int[] bestGaps=new int[gaplength];
         for (int i = 0; i <gaplength; i++){
         	System.out.print(ft1.getGene(i)+", ");
         	bestGaps[i]=ft1.getGene(i);
         }
         int[] bestGaps_2=new int[gaplength];
         System.out.print("Best Gap for trip2: ");
         for (int i = 0; i <gaplength; i++){
         	System.out.print(ft2.getGene(i)+", ");
         	bestGaps_2[i]=ft2.getGene(i);
         }
         double obj1=ft1.geto1();
         double obj2=ft1.geto2();
         double obj3=ft1.geto3();
         double bestFit=-ft1.getFitness();
         System.out.println();
         System.out.println("Best Fittest for trip1: " + bestFit);
         System.out.println("Objective 1 value for trip1: " + obj1);
         System.out.println("Objective 2 value for trip1: " + obj2);
         System.out.println("Objective 3 value for trip1: " + obj3);
         System.out.println();
         System.out.println("Objectives for each bus and stop for trip1:");
         Vector<Bus> busObjs=ft1.getbus();
         for(int i=0;i<busObjs.size();i++){
        	 Bus b=busObjs.get(i);
        	 System.out.println("====================");
        	 System.out.println("Bus: "+b.getBusId());
        	 System.out.println("====================");
        	 Vector<Stop> trip=b.getTrip();
        	 for(int k=0;k<trip.size();k++){
        		 Stop s=trip.get(k);
        		 System.out.println("Stop: "+s.getStopId());
        		 System.out.println("Objective 1: "+s.getObjective1());
        		 System.out.println("Objective 2: "+s.getObjective2());
        	 }        	         	 
         }
         double obj1_2=ft2.geto1();
         double obj2_2=ft2.geto2();
         double obj3_2=ft2.geto3();
         double bestFit_2=-ft2.getFitness();
         System.out.println();
         System.out.println("Best Fittest for trip2: " + bestFit_2);
         System.out.println("Objective 1 value for trip2: " + obj1_2);
         System.out.println("Objective 2 value for trip2: " + obj2_2);
         System.out.println("Objective 3 value for trip2: " + obj3_2);
         System.out.println();
         System.out.println("Objectives for each bus and stop for trip2:");
         Vector<Bus> busObjs_2=ft2.getbus();
         for(int i=0;i<busObjs_2.size();i++){
        	 Bus b=busObjs_2.get(i);
        	 System.out.println("====================");
        	 System.out.println("Bus: "+b.getBusId());
        	 System.out.println("====================");
        	 Vector<Stop> trip=b.getTrip();
        	 for(int k=0;k<trip.size();k++){
        		 Stop s=trip.get(k);
        		 System.out.println("Stop: "+s.getStopId());
        		 System.out.println("Objective 1: "+s.getObjective1());
        		 System.out.println("Objective 2: "+s.getObjective2());
        	 }        	         	 
         }
         Vector<Solution> solutions = new Vector<Solution>();
         Solution solution_1=new Solution(obj1, obj2, obj3, bestFit, busObjs);
         Solution solution_2=new Solution(obj1_2, obj2_2, obj3_2, bestFit_2, busObjs_2);
         solution_1.setBestGaps(bestGaps);
         solution_2.setBestGaps(bestGaps_2);
         solutions.add(solution_1);
         solutions.add(solution_2);
         return solutions;
    }
    }
}
