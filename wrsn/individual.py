import os
import random
import numpy as np

from utils import factor
from utils import factory

from wrsn.map import Map
from wrsn.individual import Individual
from wrsn.wce import WCE
from wrsn.sensor import Sensor


class Individual:
    def __init__(self, map=None, individual: Individual=None):
        self.N = 0 # So luong gene
        self.path = [] # duong di cua xe sac qua cac sensor
        self.taus = [] # thoi gian sac cua cac sensor
        self.totalDistance = 0 # tong chi phi duong di
        self.fitnessF = 0 # fitness danh gia duong di
        self.fitnessG = 0 # fitness danh gia thoi gian sac

        self.WCE_ = WCE()
        self.Sensor_ = Sensor()

        if (map != None) & (individual == None): 
            self.N = map.getN()
            # Chi gan gia tri cho duong di truoc
            for i in range(self.N):
                self.setNode(i, i)
            # Sinh hoan vi
            random.shuffle(self.path)
        elif (map == None) & (individual != None):
            self.N = individual.getN()
            self.path = individual.getPath()
            self.taus = individual.getTaus()
        # Phase 2
        else:
            self.isDuplicate = {}
            self.N = individual.getN()
            self.path = individual.getPath()
            self.taus = []

            self.calculateTotalDistance(map)
            self.E_T = self.totalDistance * self.WCE_.P_M / self.WCE_.V
            t = (self.WCE_.E_MC - self.E_T) / self.WCE_.U

            self.max_t_list = []
            for i in range(self.N):
                max_t = (self.Sensor_.E_MAX - self.Sensor_.E_MIN) / (self.WCE_.U - map.getSensor(i).getP())
                self.max_t_list.append(max_t / t)

            self.t_sensor_charged = []
        
            # Chia thoi gian ra n khoang
            self.t_sensor_charged.append(0.0)
            for i in range(self.N - 1):
                tmp = np.random.randn()
                while (tmp == 0 or tmp in self.isDuplicate.keys()):
                    tmp = np.random.randn()
                self.t_sensor_charged.append(tmp)
                self.isDuplicate[tmp] = True
            
            self.t_sensor_charged.append(1.0)
            self.t_sensor_charged.sort()

            # Trường hợp sensor cuối dư t_charged > t_max 
            # thì đem chia lại cho các sensor khác
            flag = False
            for i in range(1, len(self.t_sensor_charged)):
                t_sensor = self.t_sensor_charged[i] - self.t_sensor_charged[i - 1]
                if (i == len(self.t_sensor_charged)) & (t_sensor > self.max_t_list[i - 1]):
                    flag = True
                
                if t_sensor > self.max_t_list[i - 1]:
                    current = self.t_sensor_charged[i]
                    self.t_sensor_charged[i-1] = current - (t_sensor - self.max_t_list[i - 1])

            if flag:
                for i in range(len(self.t_sensor_charged) - 2, -1, -1):
                    t_sensor = self.t_sensor_charged[i + 1] - self.t_sensor_charged[i]
                    if t_sensor > self.max_t_list[i]:
                        current = self.t_sensor_charged[i]
                        self.t_sensor_charged[i] = current + (t_sensor - self.max_t_list[i])
                    else:
                        break
            
            for i in range(1, len(self.t_sensor_charged)):
                self.taus.append(self.t_sensor_charged[i] - self.t_sensor_charged[i - 1])

    def calculateTotalDistance(self, map: Map):
        nSensors = len(self.path)
        for i in range(nSensors):
            if i == 0:
                previous = factor.BS_INDEX
            else:
                previous = self.path[i - 1]
            current = self.path[i]
            self.totalDistance += map.distanceCalculate(previous, current)
        
        self.totalDistance += map.distanceCalculate(self.path[nSensors - 1], factor.BS_INDEX)

    def calculateFitnessFGACS(self, map):
        pass

    def calculateFitnessF(self, map):
        pass

    def getTotalDistance(self):
        return self.totalDistance

    def getN(self):
        return self.N

    def getFitnessF(self):
        return self.fitnessF

    def getFitnessG(self):
        return self.fitnessG

    def getPath(self):
        return self.path

    def setPath(self, path):
        self.path = path

    def getNode(self, index):
        return self.path[index]

    def setNode(self, index, node):
        if index > len(self.path) - 1:
            self.path.append(node)
        else:
            self.path[index] = node
    
    def getTaus(self):
        return self.taus

    def setTaus(self, taus):
        self.taus = taus
    
    def getTau(self, index):
        return self.taus[index]
    
    def setTau(self, index, tau):
        if index > len(self.taus - 1):
            self.taus.append(tau)
        else:
            self.taus[index] = tau