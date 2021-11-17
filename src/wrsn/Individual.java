package src.wrsn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import javax.print.event.PrintEvent;

import src.utils.Factor;
import src.utils.Factory;


public class Individual {
    private static Random rd = new Random();
    private int N; // so luong gene
    private ArrayList<Integer> path; // duong di cua xe sac qua cac sensor

    private double totalDistance;   // tong chi phi duong di
    private ArrayList<Double> taus; // thoi gian sac cua cac sensor
    private double fitnessF;    // fitness f - danh gia duong di
    private double fitnessG;    // fitness g - danh gia thoi gian sac

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
        HashMap<Integer, Boolean> isDuplicate  = new HashMap<>();
        taus = new ArrayList<>();
        N = individual.getN();
        path = new ArrayList<>(individual.getPath());


        this.calculateTotalDistance(map);
		double E_T = totalDistance * WCE.P_M / WCE.V;

        // năng lượng sạc
		int E_charge = (int)(WCE.E_MC - E_T);
        
        ArrayList<Integer> E_sensor_charged = new ArrayList<>();
        E_sensor_charged.add(0);


        // Chia năng lượng N khoảng 
		for (int i = 0 ; i < N  ; i++) {
            Integer tmp = rd.nextInt(E_charge);
            while( (tmp==0 || isDuplicate.containsKey(tmp))) {
                tmp = rd.nextInt(E_charge);
            }
            
            E_sensor_charged.add(tmp);
            isDuplicate.put(tmp, true);
        }

        Collections.sort(E_sensor_charged);
        

        for (int i = 1 ; i<E_sensor_charged.size() ; i++) {
            Integer E_sensor = E_sensor_charged.get(i)-E_sensor_charged.get(i-1);
            
            if (E_sensor > (int) Sensor.E_MAX) {
                Integer current = E_sensor_charged.get(i);
                E_sensor_charged.set(i,(int)(current - (E_sensor-Sensor.E_MAX))); 

                E_sensor = (int) Sensor.E_MAX;
            }
            else if (E_sensor < (int) Sensor.E_MIN)
            {
                Integer current = E_sensor_charged.get(i);
                E_sensor_charged.set(i,(int)(current + (Sensor.E_MIN - E_sensor))); 

                E_sensor = (int) Sensor.E_MIN;
            }

            taus.add((E_sensor)/(WCE.U-map.getSensor(path.get(i-1)).getP()));
        }
        
        System.out.println(taus.toString());
    }

    private void calculateTotalDistance(Map map) {
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

    public double getFitnessG() {
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
