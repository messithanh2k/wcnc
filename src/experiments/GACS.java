package experiments;

import utils.Constant;
import utils.Factor;
import utils.FileManipulation;
import wrsn.Individual;
import wrsn.Map;
import wrsn.Population;
import wrsn.Sensor;
import wrsn.WCE;

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

		Population P = new Population(200, map);

		Individual best_path_individual = gacs.phase1(P, map, factor);

		// calculate t and max_t_list
		best_path_individual.calculateTotalDistance(map);
		double E_T = best_path_individual.getTotalDistance() * WCE.P_M / WCE.V;
		double t = (WCE.E_MC - E_T) / WCE.U;
		int N = best_path_individual.getN();

		ArrayList<Double> max_t_list = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			double max_t = (Sensor.E_MAX - Sensor.E_MIN) / (WCE.U - map.getSensor(i).getP());
			max_t_list.add(max_t / t);
		}

		Population population_phase2 = new Population(200, map, best_path_individual);
		double t_normalize = 1.0;
		Individual best = gacs.phase2(best_path_individual, population_phase2, map, factor, t_normalize, max_t_list);

		// if (gacs.checkIndividualValid(best_path_individual, best, map)){
		// 	System.out.println(best.getPath());
		// 	System.out.println(best.getTaus());
		// }



		/* test init population
		Population population_phase2 = new Population(1000, map, best_path_individual);
		int countt = 0;
		for (Individual individual : population_phase2.getIndividuals()) {
			if (gacs.checkIndividualValid(best_path_individual, individual, map) == false){
				countt++;
			}
		}
		System.out.println("sai: " + countt);
		*/

		/* test calfitness
		Individual in_pha_2 = new Individual(map, best_path_individual);
		in_pha_2.calculateFitnessGGACS(map);
		System.out.println(in_pha_2.getFitnessG());
		*/

		/* test mutation 
		Individual parent = new Individual(map, best_path_individual);
		Individual child = gacs.SAC(parent, max_t_list);
		
		ArrayList<Double> abs = new ArrayList<>(); 
		for(int i = 0; i < parent.getN(); i++){ 
			abs.add(child.getTau(i) - parent.getTau(i)); 
		} 
		System.out.println(abs);
		System.out.println(gacs.checkIndividualValid(best_path_individual, child, map));
		*/

		/* test crossover 
		Individual parent1 = new Individual(map, best_path_individual); 
		Individual parent2 = new Individual(map, best_path_individual);
		double t_normalize = 1.0;
		ArrayList<Individual> offspring = gacs.SPAHX(parent1, parent2, t_normalize, max_t_list); 
		ArrayList<Double> tausss = offspring.get(1).getTaus(); 
		double sum = 0; 
		for (double i : tausss){ sum += i; } 
		System.out.println(sum);
	
		System.out.println(gacs.checkIndividualValid(best_path_individual, offspring.get(0), map));
		System.out.println(gacs.checkIndividualValid(best_path_individual, offspring.get(1), map));
		*/
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

	// GA : khá»Ÿi táº¡o quáº§n thá»ƒ -> tÃ­nh fitness -> chá»�n cha máº¹ -> lai
	// ghÃ©p, Ä‘á»™t biáº¿n -> náº¡p cÃ¡ thá»ƒ con vÃ o -> chá»�n lá»�c Ä‘á»ƒ táº¡o
	// quáº§n thá»ƒ má»›i
	private Individual phase1(Population P, Map map, Factor factor) {
		ArrayList<Double> result = new ArrayList<>();
		for (int gen = 0; gen < 1000; gen++) {
			int populationSize = P.getN(); // kich thuoc quan the

			int N = map.getN(); // so luong sensor
			// Khoi tao va tinh fitness
			for (Individual individual : P.getIndividuals()) {
				individual.calculateFitnessFGACS(map);
			}

			// Tien hanh lai ghep va dot bien
			ArrayList<Individual> offspingIndividuals = new ArrayList<>();
			Random rand = new Random();
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
					} else {
						// lai ghep spx
						offspingIndividuals.addAll(SPX(P.getIndividual(parent1), P.getIndividual(parent2)));
					}

					double rmm = rand.nextDouble();
					if (rmm < Factor.MUTATION_RATE_1) {
						double rmcm = rand.nextDouble();
						if (rmcm < Factor.CIM_RATE) {
							offspingIndividuals.add(CIM(P.getIndividual(parent1)));
							offspingIndividuals.add(CIM(P.getIndividual(parent2)));
						} else {
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

			for (int i = populationSize; i < len; i++) {
				new_population.remove(populationSize);
			}
			result.add(new_population.get(0).getFitnessF());

			P.setIndividuals(new_population);
		}
		System.out.println(result);

		Individual best_individual = P.getIndividuals().get(0);

		return best_individual;

	}

	class FitnessComparator implements Comparator<Individual> {

		// override the compare() method
		public int compare(Individual i1, Individual i2) {
			if (i1.getFitnessF() == i2.getFitnessF())
				return 0;
			else if (i1.getFitnessF() > i2.getFitnessF())
				return 1;
			else
				return -1;
		}
	}
	class FitnessGComparator implements Comparator<Individual> {

		// override the compare() method
		public int compare(Individual i1, Individual i2) {
			if (i1.getFitnessG() == i2.getFitnessG())
				return 0;
			else if (i1.getFitnessG() > i2.getFitnessG())
				return 1;
			else
				return -1;
		}
	}

	private Individual CIM(Individual parent) {
		Individual child = new Individual(parent);
		Random rand = new Random();
		int N = parent.getN();
		int point = rand.nextInt(N - 1) + 1;

		for (int i = 0; i < N; i++) {
			if (i < point) {
				child.setNode(i, parent.getNode(point - 1 - i));
			} else if (i == point) {
				child.setNode(i, parent.getNode(i));
			} else {
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
		Random rand = new Random();
//        rand.setSeed(Factor.SEED);
		int point1 = rand.nextInt(N - 4); // chon ngau nhien diem cat thu nhat
		int point2 = rand.nextInt(N - 2 - point1) + point1 + 2; // chon ngau nhien diem cat thu hai
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

		return new ArrayList<>() {
			{
				add(offspring1);
				add(offspring2);
			}
		};
	}

	private ArrayList<Integer> getChild(ArrayList<Integer> path1, ArrayList<Integer> path2, int point1, int point2) {
		ArrayList<Integer> parent1MiddleCross = new ArrayList<Integer>(path1.subList(point1, point2));
		ArrayList<Integer> parent2MiddleCross = new ArrayList<Integer>(path2.subList(point1, point2));
		ArrayList<int[]> relations = new ArrayList<int[]>();
		for (int i = 0; i < parent1MiddleCross.size(); i++) {
			int[] tmp = { parent2MiddleCross.get(i), parent1MiddleCross.get(i) };
			relations.add(tmp);
		}

		ArrayList<Integer> child = new ArrayList<Integer>();

		for (int i = 0; i < path1.size(); i++) {
			if (i < point1 || i >= point2) {
				int childres = path1.get(i);
				Boolean check = true;

				while (check) {
					Boolean c = false;
					for (int j = 0; j < relations.size(); j++) {
						if (childres == relations.get(j)[0]) {
							c = true;
							childres = relations.get(j)[1];
							break;
						}
					}
					if (c == false) {
						check = false;
					}
				}
				child.add(childres);
			} else {
				child.add(path2.get(i));
			}
		}

		return child;

	}

	private ArrayList<Individual> SPX(Individual parent1, Individual parent2) {
		int N = parent1.getN();
		Random rand = new Random();
//        rand.setSeed(Factor.SEED);
		int point = rand.nextInt(N); // chon ngau nhien mot diem cat
		Individual offspring1 = new Individual(parent1);
		Individual offspring2 = new Individual(parent2);
		int[] location1 = new int[N - point]; // vi tri cua cac sensor ben phai diem cat con 1
		int[] location2 = new int[N - point]; // vi tri cua cac sensor ben phai diem cat con 2
		int pointSize = N - point; // kich thuoc ben phai diem cat cua cac con
		int loc1Count = 0;
		int loc2Count = 0;

		// Xac dinh thu tu ben phai diem cat cua con 1 tren cha me 2
		for (int i = 0; i < N; i++) {
			if (loc1Count == pointSize)
				break;
			for (int j = point; j < N; j++) {
				if (parent2.getNode(i) == offspring1.getNode(j)) {
					location1[loc1Count] = parent2.getNode(i);
					loc1Count++;
				}
			}
		}

		// Xac dinh thu tu ben phai diem cat cua con 2 tren cha me 1
		for (int i = 0; i < N; i++) {
			if (loc2Count == pointSize)
				break;
			for (int j = point; j < N; j++) {
				if (parent1.getNode(i) == offspring2.getNode(j)) {
					location2[loc2Count] = parent1.getNode(i);
					loc2Count++;
				}
			}
		}

		for (int i = point; i < N; i++) {
			offspring1.setNode(i, location1[i - point]);
			offspring2.setNode(i, location2[i - point]);
		}

		return new ArrayList<>() {
			{
				add(offspring1);
				add(offspring2);
			}
		};
	}

	private Individual phase2(Individual best_path_individual, Population P, Map map, Factor factor, double t_normalize, ArrayList<Double> max_t_list) {
		for (Individual individual : P.getIndividuals()) {
			if (checkIndividualValid(best_path_individual, individual, map) == false){
				System.out.println("sai init");
			}
		}
		ArrayList<Integer> result = new ArrayList<>();
		for (int gen = 0; gen < 1000; gen++) {
			int populationSize = P.getN(); // kich thuoc quan the
			for (Individual individual : P.getIndividuals()){
				if (checkIndividualValid(best_path_individual, individual, map) == false){
					System.out.println("sai quan the moi roi");
				}
			}
			int N = map.getN(); // so luong sensor
			// Khoi tao va tinh fitness
			for (Individual individual : P.getIndividuals()) {
				individual.calculateFitnessGGACS(map);;
			}
			
			// Tien hanh lai ghep va dot bien
			ArrayList<Individual> offspingIndividuals = new ArrayList<>();
			Random rand = new Random();
//            rand.setSeed(Factor.SEED);
			for (int i = 0; i < populationSize; i++) {
				double rmx = rand.nextDouble();
				// xac suat de lai ghep
				if (rmx < Factor.CROSSOVER_RATE_1) {
					int parent1 = rand.nextInt(populationSize);
					int parent2 = rand.nextInt(populationSize);

					offspingIndividuals.addAll(SPAHX(P.getIndividual(parent1), P.getIndividual(parent2), t_normalize, max_t_list));

					double rmm = rand.nextDouble();
					if (rmm < Factor.MUTATION_RATE_1) {
						offspingIndividuals.add(SAC(P.getIndividual(parent1), max_t_list));
						offspingIndividuals.add(SAC(P.getIndividual(parent2), max_t_list));

					}
				}
			}

			for (Individual individual : offspingIndividuals) {
				individual.calculateFitnessGGACS(map);;
			}

			ArrayList<Individual> new_population = P.getIndividuals();
			new_population.addAll(offspingIndividuals);

			Collections.sort(new_population, new FitnessGComparator());

			int len = new_population.size();

			for (int i = populationSize; i < len; i++) {
				new_population.remove(populationSize);
			}
			
			result.add(new_population.get(0).getFitnessG());

			P.setIndividuals(new_population);

		}
		System.out.println(result);

		Individual best_individual = P.getIndividuals().get(0);
		System.out.println(best_individual.getFitnessG());

		return best_individual;
	}

	/////// trÃ­
	private ArrayList<Individual> SPAHX(Individual parent1, Individual parent2, double t_normalize, ArrayList<Double> max_t_list) {
		int N = parent1.getN();
		Random rand = new Random();
		int i = rand.nextInt(N); // random vi tri cat
		Individual offspring1 = new Individual(parent1);
		Individual offspring2 = new Individual(parent2);

		double beta = rand.nextDouble() - 0.5; // random beta

		// lai 2 con
		ArrayList<Double> taus1 = offspring1.getTaus();
		ArrayList<Double> taus2 = offspring2.getTaus();

		taus1.set(i, (1 - beta) * parent1.getTaus().get(i) + beta * parent2.getTaus().get(i));
		taus2.set(i, (1 - beta) * parent2.getTaus().get(i) + beta * parent1.getTaus().get(i));
		if (taus1.get(i) < 0){
			taus1.set(i, -taus1.get(i));
		}
		if (taus2.get(i) < 0){
			taus2.set(i, -taus2.get(i));
		}

		for (int j = 0; j < i; j++) {
			taus1.set(j, parent1.getTaus().get(j));
			taus2.set(j, parent2.getTaus().get(j));
		}
		for (int j = i + 1; j < N; j++) {
			taus1.set(j, parent2.getTaus().get(j));
			taus2.set(j, parent1.getTaus().get(j));
		}

		// chinh sua gen thoa man yeu cau tong W
		double sumTaus1 = 0;
		double sumTaus2 = 0;
		for (int j = 0; j < N; j++) {
			sumTaus1 += taus1.get(j);
			sumTaus2 += taus2.get(j);
		}

		// chinh gen1
		if (sumTaus1 > t_normalize) {
			double ratio = t_normalize / sumTaus1;
			for (int j = 0; j < N; j++) {
				taus1.set(j, taus1.get(j) * ratio);
			}
		} else {
			editTaus(taus1, sumTaus1, t_normalize, max_t_list, N);
		}
		// chinh gen2
		if (sumTaus2 > t_normalize) {
			double ratio = t_normalize / sumTaus2;
			for (int j = 0; j < N; j++) {
				taus2.set(j, taus2.get(j) * ratio);
			}
		} else {
			editTaus(taus2, sumTaus2, t_normalize, max_t_list, N);
		}

		return new ArrayList<>() {
			{
				add(offspring1);
				add(offspring2);
			}
		};
	}

	// Them tgian
	private void editTaus(ArrayList<Double> taus, double sumTaus, double t_normalize,
			ArrayList<Double> max_t_list, int N) {
		double min = 0;
		double max = t_normalize - sumTaus;
		ArrayList<Double> rd = new ArrayList<Double>();
		Random rand = new Random();
		rd.add(min);
		for (int i = 0; i < N - 1; i++) {
			double temp = rand.nextDouble() * max;
			rd.add(temp);
		}
		rd.add(max);
		rd.sort(Comparator.naturalOrder());
		double accumulate = 0;
		for (int i = 0; i < N; i++) {
			rd.set(i, rd.get(i + 1) - rd.get(i));
		}

		ArrayList<Double> index = new ArrayList<Double>();
		index.add(min);
		for (int i = 0; i < N - 1; i++) {
			double temp = taus.get(i) + rd.get(i) + accumulate;
			accumulate = temp;
			index.add(temp);
		}
		index.add(t_normalize);

		for (int i = 1; i < N; i++) {
			if ((index.get(i) - index.get(i - 1)) > max_t_list.get(i - 1)) {
				index.set(i, index.get(i - 1) + max_t_list.get(i - 1));
			}
		}
		if ((index.get(N) - index.get(N - 1)) > max_t_list.get(N - 1)) {
			for (int i = N - 1; i > 0; i--) {
				if ((index.get(i + 1) - index.get(i)) > max_t_list.get(i)) {
					index.set(i, index.get(i + 1) - max_t_list.get(i));
				} else {
					break;
				}
			}
		}
		for (int i = 0; i < N; i++) {
			taus.set(i, index.get(i + 1) - index.get(i));
		}
	}

	/////// huy
	private Individual SAC(Individual parent, ArrayList<Double> max_t_list) {
		Individual child = new Individual(parent);
		int N = parent.getN();
		Random rand = new Random();
		int i = rand.nextInt(N);
		int j = rand.nextInt(N);

		double max_random_theta1 = max_t_list.get(i) - parent.getTau(i);

		double theta1;
		do {
			theta1 = max_random_theta1 * rand.nextDouble();
		} while (theta1 == 0);
		double theta2;
		do {
			theta2 = parent.getTau(j) * rand.nextDouble();
		} while (theta2 == 0);
		double theta = Math.min(theta1, theta2);
		child.setTau(i, parent.getTau(i) + theta);
		child.setTau(j, parent.getTau(j) - theta);

		return child;
	}

	private boolean checkIndividualValid(Individual best_path_individual, Individual individual, Map map) {
		double E_T = best_path_individual.getTotalDistance() * WCE.P_M / WCE.V;

		double t = (WCE.E_MC - E_T) / WCE.U;
		int N = best_path_individual.getN();

		ArrayList<Double> max_t_list = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			double max_t = (Sensor.E_MAX - Sensor.E_MIN) / (WCE.U - map.getSensor(i).getP());
			max_t_list.add(max_t / t);
		}


		ArrayList<Double> tausss = individual.getTaus();

		boolean check = true;
		for (int i = 0; i < N; i++){
			if (tausss.get(i) < 0){
				System.out.println("nho hon 0");
				return false;
			}
		}
		double sum = 0; 
		for (double i : tausss){ 
			sum += i; 
		}
		if (Math.abs(1 - sum) > 0.1) return false;
		for (int i = 0; i < N; i++) {
			if (tausss.get(i) > max_t_list.get(i)) {
				check = false;
				break;
			}
		}

		return check;
	}
}
