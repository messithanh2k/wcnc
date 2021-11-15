package experiments;

import utils.Constant;
import utils.Factor;
import utils.FileManipulation;
import wrsn.Individual;
import wrsn.Map;
import wrsn.Population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class GACS extends Algorithm {
	public static void main(String args[]) {
		GACS gacs = new GACS();
		
		Factor factor = new Factor();
		FileManipulation fm = new FileManipulation();
		Map map = new Map();

		
		String filename = Constant.FILENAME;
        fm.readFile(filename, map, factor);
    
        Population P = new Population(Factor.N, map);
        
        Individual best_path_individual = gacs.phase1(P, map, factor);
        
        // phase 2
        Population P2 = new Population(Factor.N, map, best_path_individual);
        gacs.phase2(P2, map, factor);
	}
//    @Override
//    public void execute(Map map, Factor factor) {
//        // Khoi tao quan the ban dau
//        Population P = new Population(Factor.N, map);
//        double timestamp = 0;
//        while (timestamp < Factor.T) {
//            phase1(P, map, factor);
//            phase2(P, map, factor);
//            timestamp ++;
//        }
//    }
	
	// GA : khởi tạo quần thể -> tính fitness -> chọn cha mẹ -> lai ghép, đột biến -> nạp cá thể con vào -> chọn lọc để tạo quần thể mới 
    private Individual phase1(Population P, Map map, Factor factor) {
    	for(int gen = 0; gen < 100000; gen++) {
            int populationSize = P.getN();  // kich thuoc quan the

            int N = map.getN(); // so luong sensor
            // Khoi tao va tinh fitness
            for (Individual individual : P.getIndividuals()) {
                individual.calculateFitnessFGACS(map);
            }

            // Tien hanh lai ghep va dot bien
            ArrayList<Individual> offspingIndividuals = new ArrayList<>();
            Random rand= new Random();
//            rand.setSeed(Factor.SEED);
            for (int i = 0; i < populationSize; i++) {
                double rmx = rand.nextDouble();
                // xac suat de lai ghep
                if (rmx < Factor.CROSSOVER_RATE_1) {
                    int parent1 = rand.nextInt(populationSize);
                    int parent2 = rand.nextInt(populationSize);

                    double rmpmx = rand.nextDouble();
                    if (rmpmx < Factor.PMX_RATE) {
                        // lai ghep pmx
                        offspingIndividuals.addAll(PMX(P.getIndividual(parent1), P.getIndividual(parent2)));
                    }
                    else {
                        // lai ghep spx
                        offspingIndividuals.addAll(SPX(P.getIndividual(parent1), P.getIndividual(parent2)));
                    }
                    
                    double rmm = rand.nextDouble();
                    if (rmm < Factor.MUTATION_RATE_1) {
                    	double rmcm = rand.nextDouble();
                    	if (rmcm < Factor.CIM_RATE) {
                    		offspingIndividuals.add(CIM(P.getIndividual(parent1)));
                    		offspingIndividuals.add(CIM(P.getIndividual(parent2)));
                    	}
                    	else {
                    		offspingIndividuals.add(SWAP(P.getIndividual(parent1)));
                    		offspingIndividuals.add(SWAP(P.getIndividual(parent2)));
                    	}
                    	
                    }
                }
            }
            
            for (Individual individual : offspingIndividuals) {
            	individual.calculateFitnessFGACS(map);
            }
            
            ArrayList<Individual> new_population = P.getIndividuals();
            new_population.addAll(offspingIndividuals);
            
            Collections.sort(new_population, new FitnessComparator());
            
            int len = new_population.size();

            for(int i = populationSize; i < len; i++) {
            	new_population.remove(populationSize);
            }
            
            P.setIndividuals(new_population);
//            System.out.println();
//            for(Individual individual : P.getIndividuals()) {
//            	System.out.print(individual.getFitnessF()+ " ");
//            }
    	}
    	
    	Individual best_individual = P.getIndividuals().get(0);
    	System.out.println(best_individual.getFitnessF());
    	System.out.println(best_individual.getPath());
    	
    	return best_individual;

    }
    
    class FitnessComparator implements Comparator<Individual>{
    	
        // override the compare() method
        public int compare(Individual i1, Individual i2)
        {
            if (i1.getFitnessF() == i2.getFitnessF())
                return 0;
            else if (i1.getFitnessF() > i2.getFitnessF())
                return 1;
            else
                return -1;
        }
    }
    
    
    
    private Individual CIM(Individual parent) {
    	Individual child = new Individual(parent);
    	Random rand = new Random();
    	int N = parent.getN();
    	int point = rand.nextInt(N-1) + 1;
    	
    	for(int i = 0; i < N; i++) {
    		if (i < point) {
    			child.setNode(i, parent.getNode(point - 1 - i));
    		}
    		else if (i == point){
    			child.setNode(i, parent.getNode(i));
    		}
    		else {
    			child.setNode(i, parent.getNode(N + point - i));
    		}
    	}
    	return child;
    }
    
    private Individual SWAP(Individual parent) {
    	Individual child = new Individual(parent);
    	Random rand = new Random();
    	int N = parent.getN();
    	int point1 = rand.nextInt(N);
    	int point2 = rand.nextInt(N);
		child.setNode(point1, parent.getNode(point2));
		child.setNode(point2, parent.getNode(point1));
    	return child;
    }

    private ArrayList<Individual> PMX(Individual parent1, Individual parent2) {
        int N = parent1.getN();
        Random rand= new Random();
//        rand.setSeed(Factor.SEED);
        int point1 = rand.nextInt(N-4); // chon ngau nhien diem cat thu nhat
        int point2 = rand.nextInt(N-2-point1) + point1 + 2; // chon ngau nhien diem cat thu hai
        Individual offspring1 = new Individual(parent1);
        Individual offspring2 = new Individual(parent2);
		ArrayList<Integer> path1 = parent1.getPath();
		ArrayList<Integer> path2 = parent2.getPath();
		
		ArrayList<Integer> child1 = getChild(path1, path2, point1, point2);
		ArrayList<Integer> child2 = getChild(path2, path1, point1, point2);

		for (int i = 0; i < N; i++) {
			offspring1.setNode(i, child1.get(i));
			offspring2.setNode(i, child2.get(i));
		}
	    
        
        return new ArrayList<>() {{
            add(offspring1);
            add(offspring2);
        }};
    }
    
    private ArrayList<Integer> getChild(ArrayList<Integer> path1, ArrayList<Integer> path2, int point1, int point2) {
    	ArrayList<Integer> parent1MiddleCross = new ArrayList<Integer>(path1.subList(point1, point2));
	    ArrayList<Integer> parent2MiddleCross = new ArrayList<Integer>(path2.subList(point1, point2));
    	ArrayList<int[]> relations = new ArrayList<int[]>();
	    for(int i = 0; i < parent1MiddleCross.size(); i++) {
	    	int[] tmp = {parent2MiddleCross.get(i), parent1MiddleCross.get(i)};
	    	relations.add(tmp);
	    }
	    
	    ArrayList<Integer> child = new ArrayList<Integer>();
	    
	    for(int i = 0; i < path1.size(); i++) {
	    	if (i < point1 || i >= point2) {
	    		int childres = path1.get(i);
	    		Boolean check = true;
	    		
	    		while(check){
	    			Boolean c = false;
	    			for(int j = 0; j < relations.size(); j++) {
	    				if (childres == relations.get(j)[0]) {
	    					c = true;
	    					childres = relations.get(j)[1];
	    					break;
	    				}
	    			}
	    			if(c == false) {
	    				check = false;
	    			}
	    		}
	    		child.add(childres);
	    	}
	    	else {
	    		child.add(path2.get(i));
	    	}
	    }
	    
	    return child;
	    
	    
    }

    private ArrayList<Individual> SPX(Individual parent1, Individual parent2) {
        int N = parent1.getN();
        Random rand= new Random();
//        rand.setSeed(Factor.SEED);
        int point = rand.nextInt(N); // chon ngau nhien mot diem cat
        Individual offspring1 = new Individual(parent1);
        Individual offspring2 = new Individual(parent2);
        int[] location1 = new int[N - point]; // vi tri cua cac sensor ben phai diem cat con 1
        int[] location2 = new int[N - point]; // vi tri cua cac sensor ben phai diem cat con 2
        int pointSize = N - point; //kich thuoc ben phai diem cat cua cac con
        int loc1Count = 0;
        int loc2Count = 0;

        // Xac dinh thu tu ben phai diem cat cua con 1 tren cha me 2
        for (int i = 0; i < N; i++) {
            if (loc1Count == pointSize) break;
            for (int j = point; j < N; j++) {
                if (parent2.getNode(i) == offspring1.getNode(j)) {
                    location1[loc1Count] = parent2.getNode(i);
                    loc1Count ++;
                }
            }
        }

        // Xac dinh thu tu ben phai diem cat cua con 2 tren cha me 1
        for (int i = 0; i < N; i++) {
            if (loc2Count == pointSize) break;
            for (int j = point; j < N; j++) {
                if (parent1.getNode(i) == offspring2.getNode(j)) {
                    location2[loc2Count] = parent1.getNode(i);
                    loc2Count ++;
                }
            }
        }

        for (int i = point; i < N; i++) {
            offspring1.setNode(i, location1[i - point]);
            offspring2.setNode(i, location2[i - point]);
        }

        return new ArrayList<>() {{
            add(offspring1);
            add(offspring2);
        }};
    }

    private void phase2(Population P, Map map, Factor factor) {
    }
    
    /////// trí
    private ArrayList<Individual> SPAHX(Individual parent1, Individual parent2){
        int N = parent1.getN();
        
        Individual offspring1 = new Individual(parent1);
        Individual offspring2 = new Individual(parent2);
        
        return new ArrayList<>() {{
            add(offspring1);
            add(offspring2);
        }};
    }
    
    /////// huy
    private Individual SAC(Individual parent) {
    	Individual child = new Individual(parent);
    	return child;
    }
}
