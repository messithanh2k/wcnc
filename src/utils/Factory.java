package utils;

import wrsn.Individual;
import wrsn.Map;
import wrsn.Sensor;
import wrsn.WCE;

import java.util.ArrayList;
import java.util.stream.DoubleStream;

public class Factory {
    /* fitness f - danh gia duong di
        f  = alpha * f1 + (1 - alpha) * f2
        voi f1 = sum(wi)
            f2 = sum(|wi - f1/n|)
        trong do i = 1,..,n; n - so luong sensor;
        wi  = 0 neu Er = 0 voi Er la nang luong con lai cua sensor i
            = pi * t_w/Er neu nguoc lai, voi t_w la thoi gian cho do
            di chuyen cua sensor i
     */
    public double fitnessF(ArrayList<Integer> path, Map map) {
        int nSensors = path.size();
        double[] w = new double[nSensors];
        double waitingTime = 0;
        for (int i = 0; i < nSensors; i++) {
            int previous = i == 0 ? Factor.BS_INDEX : path.get(i - 1);
            int current = path.get(i);
            double distance = map.distanceCalculate(previous, current);
            waitingTime += distance / WCE.V;
            double residualEnergy = map.getSensor(i).getE();
            /*
                wi  = 0 neu Er = 0 voi Er la nang luong con lai cua sensor i
                    = pi * t_w/Er neu nguoc lai, voi t_w la thoi gian cho do
                    di chuyen cua sensor i
             */
            if (residualEnergy == 0) w[i] = 0;
            else {
                w[i] = map.getSensor(i).getP() * waitingTime / residualEnergy;
            }
        }
        /*
            f1 = sum(wi)
            f2 = sum(|wi - f1/n|)
         */
        double f1 = DoubleStream.of(w).sum();
        double f2 = 0;
        double avgF = f1 / nSensors;
        for(double w_i : w){
            f2 += Math.abs(w_i - avgF);
        }

        return Factor.ALPHA * f1 + (1 - Factor.ALPHA) * f2;
    }

    public double fitnessG(Individual individual, double[] arx, Map map) {
        int N = arx.length;
        // Tinh tong gia tri cua arx
        double sumArx = 0;
        for (double x : arx) {
            sumArx += x;
        }
        // bien arx sau khi chuan hoa
        double[] nArx = new double[N];
        // Chuan hoa arx sao cho tong cua chung bang 1.
        for (int i = 0; i < N; i++) {
            nArx[i] = arx[i] / sumArx;
        }
        // Thoi gian di chuyen cua WCE
        double travellingTime = individual.getTotalDistance() / WCE.V;
        // Tong thoi gian sac trong mot chu ky theo ly thuyet tau = (E_MC - travellingTime * P_M) / U;
        double tau = (WCE.E_MC - travellingTime * WCE.P_M) / WCE.U;
        // Neu tau < 0 (hay nang luong con lai khong du de sac), cho tau = 0.
        if (tau < 0) tau = 0;
        // Tong thoi gian trong 1 chu ky tren ly thuyet
        double totalTime = tau + travellingTime;
        return 1;
    }

    public double fitnessFGACS(ArrayList<Integer> path, Map map) {
        int N = map.getN(); // so luong sensor
        double f1 = 0;   // gia tri fitness1
        double f2 = 0;   // gia tri fitness2
        double[] f1s = new double[N]; // gia tri fitness1 cua tung sensor
        double[] weights = new double[N];   // trong so w
        double distance = map.distanceCalculate(Factor.BS_INDEX, path.get(0));  // khoang cach tu depot toi sensor dau tien
        for (int i = 0; i < N; i++) {
            int sensor1 = path.get(i);
            int sensor2 = 0;
            if (i == N - 1) {
                sensor2 = Factor.BS_INDEX;
            }
            else {
                sensor2 = path.get(i + 1);
            }

            // Tinh f1
            weights[sensor1] = map.getSensor(sensor1).getE() / map.getSensor(sensor1).getP();
            double waitingTime = distance / WCE.V;
            f1s[sensor1] = waitingTime / weights[sensor1];
            f1 += f1s[sensor1];

            distance += map.distanceCalculate(sensor1, sensor2);
        }

        for (int sensor : path) {
            f2 += Math.abs(f1s[sensor] - f1 / N);
        }
        return Factor.ALPHA * f1 + (1 - Factor.ALPHA) * f2;
    }

    public int fitnessGGACS(Individual individual, Map map){
        int N = individual.getN();
        int numDeadNode = 0;
        ArrayList<Integer> path = individual.getPath();
        ArrayList<Double> taus = individual.getTaus();
        individual.calculateTotalDistance(map);

		double E_T = individual.getTotalDistance() * WCE.P_M / WCE.V;

		double t = (WCE.E_MC - E_T) / WCE.U;
        for(int i = 0; i < N; i++){
            taus.set(i, taus.get(i) * t);
        }

        double distance = 0.0;
        double time_charged = 0.0;
        for(int i = 0; i < N; i++){
            Sensor s = map.getSensor(path.get(i));
            int previous = i == 0 ? Factor.BS_INDEX : path.get(i - 1);
            int current = path.get(i);
            distance += map.distanceCalculate(previous, current);
            if(i > 0){
                time_charged += taus.get(path.get(i - 1));
            }
            double E_arrives = s.getE() - s.getP() * (distance / WCE.V + time_charged);
            double E_finish = s.getE() - s.getP() * (individual.getTotalDistance() / WCE.V + t) + taus.get(path.get(i)) * WCE.U;

            if (E_arrives < Sensor.E_MIN || E_finish < Sensor.E_MIN){
                numDeadNode++;
            }


        }
        for(int i = 0; i < N; i++){
            taus.set(i, taus.get(i) / t);
        }
        return numDeadNode;
    }
}
