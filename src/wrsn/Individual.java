package wrsn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;


import utils.Factor;
import utils.Factory;


public class Individual {
    private static Random rd = new Random();
    private int N; // so luong gene
    private ArrayList<Integer> path; // duong di cua xe sac qua cac sensor

    private double totalDistance;   // tong chi phi duong di
    private ArrayList<Double> taus; // thoi gian sac cua cac sensor
    private double fitnessF;    // fitness f - danh gia duong di
    private int fitnessG;    // fitness g - danh gia thoi gian sac

    public Individual(Map map) {
        N = map.getN();
        path = new ArrayList<>();
        taus = new ArrayList<>();
        // Chi gan gia tri cho duong di truoc
        for (int i = 0; i < N; i++) {
            setNode(i, i);
        }
        // Sinh hoan vi
        Collections.shuffle(path);
    }

    public Individual(Individual individual) {
        N = individual.getN();
        path = new ArrayList<>(individual.getPath());
        taus = new ArrayList<>(individual.getTaus());
    }
    
    // for phase 2
    public Individual(Map map, Individual individual) {
        HashMap<Double, Boolean> isDuplicate  = new HashMap<>();
        taus = new ArrayList<>();
        N = individual.getN();
        path = new ArrayList<>(individual.getPath());

        
        this.calculateTotalDistance(map);
		double E_T = totalDistance * WCE.P_M / WCE.V;
        double t = (WCE.E_MC - E_T)/WCE.U;
        ArrayList<Double> max_t_list = new ArrayList<>();
		for(int i = 0; i < N; i++){
			double max_t = (Sensor.E_MAX - Sensor.E_MIN) / (WCE.U - map.getSensor(i).getP());
			max_t_list.add(max_t / t);
		}
        
        ArrayList<Double> t_sensor_charged = new ArrayList<>();


// Chia thời gian ra N khoảng 
        t_sensor_charged.add(0.0);
		for (int i = 0 ; i < N - 1  ; i++) {
            Double tmp = rd.nextDouble();
            while( (tmp==0 || isDuplicate.containsKey(tmp))) {
                tmp = rd.nextDouble();
            }
            t_sensor_charged.add(tmp);
            isDuplicate.put(tmp, true);
        }
        t_sensor_charged.add(1.0);
        Collections.sort(t_sensor_charged);
        
        // Trường hợp sensor cuối dư t_charged > t_max thì đem chia lại cho các sensor khác
        boolean flag=false;
        for (int i = 1 ; i<t_sensor_charged.size() ; i++) {
            Double t_sensor = t_sensor_charged.get(i)-t_sensor_charged.get(i-1);
            if (i==t_sensor_charged.size()-1 && t_sensor>max_t_list.get(i-1)) {
                flag=true;
            }

            if (t_sensor > max_t_list.get(i-1)) {
                double current = t_sensor_charged.get(i);
                t_sensor_charged.set(i-1,current - (t_sensor-max_t_list.get(i-1))); 
            }
        }

        if (flag) {
            for (int i = t_sensor_charged.size()-2 ; i>=0 ; i--) {
                Double t_sensor = t_sensor_charged.get(i+1)-t_sensor_charged.get(i);
                if (t_sensor > max_t_list.get(i)) {
                    Double current = t_sensor_charged.get(i);
                    t_sensor_charged.set(i,current + (t_sensor-max_t_list.get(i))); 
                    }
                else {
                    break;
                }
            }
        }

        for (int i = 1 ; i<t_sensor_charged.size() ; i++) {
            taus.add(t_sensor_charged.get(i)-t_sensor_charged.get(i-1));
        }
    }

    public void calculateTotalDistance(Map map) {
        totalDistance = 0;
        int nSensors = path.size();
        for (int i = 0; i < nSensors; i++) {
            int previous = i == 0 ? Factor.BS_INDEX : path.get(i - 1);
            int current = path.get(i);
            totalDistance += map.distanceCalculate(previous, current);
        }
        totalDistance += map.distanceCalculate(path.get(nSensors - 1), Factor.BS_INDEX);
    }
    public void calculateFitnessFGACS(Map map) {
        Factory factory = new Factory();
        fitnessF  = factory.fitnessFGACS(path, map);
    }
    public void calculateFitnessGGACS(Map map) {
        Factory factory = new Factory();
        fitnessG  = factory.fitnessGGACS(this, map);
    }
    private void calculateFitnessF(Map map) {
        Factory factory = new Factory();
        fitnessF  = factory.fitnessF(path, map);
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public int getN() {
        return N;
    }

    public double getFitnessF() {
        return fitnessF;
    }

    public int getFitnessG() {
        return fitnessG;
    }

    public ArrayList<Integer> getPath() {
        return path;
    }

    public void setPath(ArrayList<Integer> path) {
        this.path = path;
    }

    public int getNode(int index) {
        return path.get(index);
    }

    public void setNode(int index, int node) {
        if (index > path.size() - 1) path.add(node);
        else path.set(index, node);
    }

    public ArrayList<Double> getTaus() {
        return taus;
    }

    public void setTaus(ArrayList<Double> taus) {
        this.taus = taus;
    }

    public Double getTau(int index) {
        return taus.get(index);
    }

    public void setTau(int index, Double tau) {
        if (index > taus.size() - 1) taus.add(tau);
        else taus.set(index, tau);
    }
}
