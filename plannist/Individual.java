
package plannist;
import java.util.Random;
import java.util.Vector;

public class Individual {

    static int defaultGeneLength = GA.gaplength;
    private int[] genes = new int[defaultGeneLength];
    public int fitness = 0;
    private double o1;
    private double o2;
    private double o3;
    private Vector<Bus> buses;
    

    public void generateIndividual() {
    	int max = GA.maxgap;
        int min = GA.mingap;
        Random rnd = new Random();
        for (int i = 0; i < size(); i++) {
            int gene = rnd.nextInt(max)%(max-min+1) + min;
            genes[i] = gene;
        }
    }

    public int getGene(int index) {
        return genes[index];
    }

    public int[] getGenes(){
    	return genes;
    }

    public void setGene(int index, int value) {
        genes[index] = value;
        fitness = 0;
    }

    public int size() {
        return genes.length;
    }

    public int getFitness() {
        if (fitness == 0) {
            fitness = (int) -FitnessCalc.getFitness(this).getTotalObj();
        }
    	/*s = FitnessCalc.getFitness(this);
        return s;*/
        return fitness;
    }

    public double geto1() {
    	o1 = FitnessCalc.getFitness(this).getO1();
        return o1;
    }

    public double geto2() {
    	o2 = FitnessCalc.getFitness(this).getO2();
        return o2;
    }

    public double geto3() {
    	o3 = FitnessCalc.getFitness(this).getO3();
        return o3;
    }

    public Vector<Bus> getbus(){
    	buses=FitnessCalc.getFitness(this).getUpdatedbuses();
    	return buses;
    }

}