import os
import math
import numpy as np
from numpy.core.defchararray import add

from utils.constants import *
from utils.factor import *

from wrsn.wce import WCE
from wrsn.sensor import Sensor


class Map:
    def __init__(self, N):
        self.sensors = []
        self.wce = WCE()
        self.N = N

    def getWCE(self):
        return self.wce
    
    def setWCE(self, wce):
        self.wce = wce
    
    def getN(self):
        return self.N
    
    def setN(self, N):
        self.N = N

    def getSensors(self):
        return self.sensors
    
    def setSensors(self, sensors):
        self.sensors = sensors

    def getSensor(self, index):
        return self.sensors[index]
    
    def setSensor(self, index, sensor):
        if index > len(self.sensors) - 1:
            self.sensors.append(sensor)
        else:
            self.sensors[index] = sensor

    def distanceCalculate(self, index1, index2):
        # Tinh khoang cach giua 2 sensor
        x1 = y1 = x2 = y2 = 0
        # Sensor 1
        if index1 == BS_INDEX:
            x1 = DEFAULT_X
            y1 = DEFAULT_Y
        elif index1 == WCE_INDEX:
            x1 = self.wce.getcX()
            y1 = self.wce.getcY()
        else:
            s: Sensor = self.sensors[index1]        
            x1 = s.getcX()
            y1 = s.getcY()
        
        # Sensor 2
        if index2 == BS_INDEX:
            x2 = DEFAULT_X
            y2 = DEFAULT_Y
        elif index2 == WCE_INDEX:
            x2 = self.wce.getcX()
            y2 = self.wce.getcY()
        else:
            s: Sensor = self.sensors[index2]        
            x2 = s.getcX()
            y2 = s.getcY()

        return math.sqrt((x1 - x2)**2 + (y1 - y2)**2)

    def charging(self, i, remainTime):
        s: Sensor = self.getSensor(i)
        additionalE = 0
        WCE_ = WCE()
        Sensor_ = Sensor()

        if WCE_.U - s.getP() > 0:
            additionalE = (WCE_.U - s.getP()) * remainTime
        oldE = s.getE()

        if Sensor_.E_MAX - oldE - additionalE < 0:
            additionalE = Sensor_.E_MAX - oldE
            s.setE(Sensor_.E_MAX)
            return additionalE / (WCE_.U - s.getP())

        s.setE(oldE + additionalE)
        self.wce.RE = additionalE
        
        return 0
    
    def setWCEState(self, state):
        self.wce.setState(state)

    def wceLocationCalculate(self, nextChargingNode, remainTime):
        s: Sensor = Sensor()
        WCE_ = WCE()
        s_x = s_y = 0
        if nextChargingNode != BS_INDEX:
            s = self.getSensor(nextChargingNode)
            s_x = s.getcX()
            s_y = s.getcY()
        else:
            s_x = DEFAULT_X
            s_y = DEFAULT_Y

        # Tinh goc di chuyen cua WCE - alpha
        # Dau tien, tinh khoang cach tu WCE den sensor
        distance = self.distanceCalculate(WCE_INDEX, nextChargingNode)       
        # Tiep theo, cosin alpha = ke / huyen
        cosin = np.abs(s_x - self.wce.getcX()) / distance

        # Tinh vi tri moi cua WCE
        # Dau tien, tinh khoang cach di duoc trong t_interval
        movingDistance = WCE_.V * remainTime

        # Neu khoang cach ngan hon khoang cach di duoc trong t_interval,
        # tra ve thoi gian con thua
        if distance < movingDistance:
            delta = movingDistance - distance
            if nextChargingNode != BS_INDEX:
                self.wce.setcX(s.getcX())
                self.wce.setcY(s.getcY())
            else:
                self.wce.setcX(DEFAULT_X)
                self.wce.setcY(DEFAULT_Y)

            self.wce.RE -= delta * WCE_.P_M
            return delta / WCE_.V

        newX1 = self.wce.getcX() + cosin*WCE_.V*T_INTERVAL
        newX2 = self.wce.getcX() - cosin*WCE_.V*T_INTERVAL

        newY1 = self.wce.getcY() + math.sqrt(1 - cosin*cosin)*WCE_.V*T_INTERVAL
        newY2 = self.wce.getcY() - math.sqrt(1 - cosin*cosin)*WCE_.V*T_INTERVAL

        d1 = math.sqrt((s_x - newX1)**2 + (s_y - newY1)**2)
        d2 = math.sqrt((s_x - newX1)**2 + (s_y - newY2)**2)
        d3 = math.sqrt((s_x - newX2)**2 + (s_y - newY1)**2)
        d4 = math.sqrt((s_x - newX2)**2 + (s_y - newY2)**2)

        minD = d1
        newX = newX1
        newY = newY1

        if d2 < minD:
            minD = d2;
            newX = newX1
            newY = newY2
        if d3 < minD:
            minD = d3;
            newX = newX2
            newY = newY1
        if d4 < minD:
            minD = d4;
            newX = newX2;
            newY = newY2;

        self.wce.RE -= math.sqrt((wce.getcX() - newX)**2 + (wce.getcY() - newY)**2) * WCE_.P_M;
        self.wce.setcX(newX)
        self.wce.setcY(newY)

        return 0

    def canReturnBS(self, nextChargingNode):
        s: Sensor = self.getSensor(nextChargingNode)
        WCE_ = WCE()

        # Tinh khoang cach giua wce va base station
        distanceToBS = self.distanceCalculate(WCE_INDEX, BS_INDEX)
        distanceToSensor = self.distanceCalculate(WCE_INDEX, nextChargingNode)

        # Tinh nang luong can thiet de quay ve tram sac
        eAfterCharge = self.wce.RE - distanceToSensor*WCE_.P_M - (WCE_.U - s.getP())*T_INTERVAL
        eNeeded = distanceToBS * WCE_.P_M

        # Neu nang luong con lai cua MC < nang luong can thiet
        return eAfterCharge >= eNeeded # phai tro ve tram sac ngay
        # neu khong can tro ve tram sac