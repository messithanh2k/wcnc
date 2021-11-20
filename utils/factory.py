import os
import numpy as np

from utils import factor

from wrsn.individual import Individual
from wrsn.map import Map
from wrsn.wce import WCE


class Factory:
    def __init__(self):
        """
        fitness f - danh gia duong di
        f = alpha * f1 + (1 - alpha) * f2
        voi f1 = sum(wi)
            f2 = sum(|wi - fi/n|)
        trong do i = 1, ..., n - so luong sensor
        wi = 0 neu Er = 0 - nang luong con lai cua sensor i
           = pi * t_w / Er neu nguoc lai, voi t_w la thoi gian cho do di chuyen cua sensor i 
        """ 
        self.WCE_ = WCE()

    def fitnessF(self, path, map: Map):
        nSensors = len(path)
        w = []
        waitingTime = 0
        for i in range(nSensors):
            if i == 0:
                previous = factor.BS_INDEX
            else:
                previous = path.get(i - 1)
            current = path[i]
            distance = map.distanceCalculate(previous, current)
            waitingTime += distance / self.WCE_.V
            residualEnergy = map.getSensor(i).getE()
            
            # wi  = 0 neu Er = 0 voi Er la nang luong con lai cua sensor i
            # = pi * t_w/Er neu nguoc lai, voi t_w la thoi gian cho do
            # di chuyen cua sensor i

            if residualEnergy == 0:
                w[i] = 0
            else:
                w[i] = map.getSensor(i).getP() * waitingTime / residualEnergy

        # f1 = sum(wi)
        # f2 = sum(|wi - f1/n|)

        f1 = sum(w)
        f2 = 0
        avgF = f1 / nSensors
        for w_i in w:
            f2 += np.abs(w_i - avgF)

        return factor.ALPHA * f1 + (1- factor.ALPHA) * f2

    def fitnessG(self, individual: Individual, arx, map: Map):
        N = len(arx)
        sumArx = 0
        for i in arx:
            sumArx += i
        
        # arx sau khi chuan hoa, tong bang 1
        nArx = []
        for i in range(N):
            nArx[i] = arx[i] / sumArx

        # Thoi gian di chuyen cua WCE
        travallingTime = individual.getTotalDistance() / self.WCE_.V
        # ong thoi gian sac trong mot chu ky theo ly thuyet 
        # tau = (E_MC - travellingTime * P_M) / U
        tau = (self.WCE_.E_MC - travallingTime * self.WCE_.P_M) / self.WCE_.U
        # Neu tau < 0 (hay nang luong con lai khong du de sac), cho tau = 0
        if tau < 0:
            tau = 0
        totalTime = tau + travallingTime

        return 1

    def fitnessFGACS(self, path, map: Map):
        N = map.getN() # so luong sensor
        f1 = 0 
        f2 = 0
        f1s = np.zeros(N)
        weights = np.zeros(N) # trong so w
        # khoang cach tu depot toi sensor dau tien
        distance = map.distanceCalculate(factor.BS_INDEX, path[0])

        for i in range(N):
            sensor1 = path[i]
            sensor2 = 0
            if i == N - 1:
                sensor2 = factor.BS_INDEX
            else:
                sensor2 = path[i + 1]
            
            # Tinh f1
            weights[sensor1] = map.getSensor(sensor1).getE() / map.getSensor(sensor1).getP()
            waitingTime = distance / self.WCE_.V
            f1s[sensor1] = waitingTime / weights[sensor1]
            f1 += f1s[sensor1]

            distance += map.distanceCalculate(sensor1, sensor2)
        
        for sensor in path:
            f2 += np.abs(f1s[sensor] - f1 / N)

        return factor.ALPHA * f1 + (1 - factor.ALPHA) * f2