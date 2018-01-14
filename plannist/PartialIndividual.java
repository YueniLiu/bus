package plannist;

import java.util.Random;

public class PartialIndividual {
	static int defaultGeneLength;
    private int[] genes = new int[defaultGeneLength];
    private int fitness = 0;


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
    public int[] getGenes() {
		return genes;
	}

    public void setGene(int index, int value) {
        genes[index] = value;
        fitness = 0;
    }
    
    public void setGeneLength(int index) {
    	defaultGeneLength = index;
    }


    public int size() {
        return genes.length;
    }

    public int getFitness() {
        if (fitness == 0) {
            fitness = (int) -FitnessCalc.getFitnessP(this).getTotalObj();
        }
        return fitness;
    }
    

}
