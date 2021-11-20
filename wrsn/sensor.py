import os
import numpy as np

from utils.factor import *


class Sensor:
    def __init__(self, cX, cY, p, E):
        self.E_MAX = E_MAX
        self.E_MIN = E_MIN
        self.E_THRESHOLD = 0.5 * E_MAX
        self.cX = cX
        self.cY = cY
        self.p = p
        self.E = E

    def getcX(self):
        return self.cX

    def setcX(self, cX):
        self.cX = cX
    
    def getcY(self):
        return self.cY

    def setcY(self, cY):
        self.cY = cY
    
    def getP(self):
        return self.p
    
    def setP(self, p):
        self.p = p

    def getE(self):
        return self.E
    
    def setE(self, E):
        self.E = E

    def calculateE(self, time):
        self.E -= self.p * time