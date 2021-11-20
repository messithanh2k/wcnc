import os
import numpy as np

from utils.constants import *
from utils.factory import *
from utils.factor import *

from wrsn.sensor import Sensor


class Algorithm:
    def __init__(self):
        self.chargedNodes = []
        self.deadNodes = []
        self.eMoveRatio = 0.0
        self.executedTime = 0
        self.nodeCount = 0
        self.nextChargingNode = -1
        self.chargingNode = -1

        self.Sensor_ = Sensor()

    def execute(self):
        pass

    def getChargedNodes(self):
        return self.chargedNodes

    def setChargedNodes(self, chargedNodes):
        self.chargedNodes = chargedNodes

    def getDeadNodes(self):
        return self.deadNodes

    def setDeadNodes(self, deadNodes):
        self.deadNodes = deadNodes

    def getExecutedTime(self):
        return self.executedTime

    def setExecutedTime(self, executedTime):
        self.executedTime = executedTime

    def geteMoveRatio(self):
        return self.eMoveRatio

    def seteMoveRatio(self, eMoveRatio):
        self.eMoveRatio = eMoveRatio


class Request:
    def __init__(self, id, RE, p, ts):
        self.id = id
        self.RE = RE
        self.p = p
        self.ts = ts

    def getId(self):
        return self.id
    
    def setId(self, id):
        self.id = id

    def getRE(self):
        return self.RE
    
    def setRE(self, RE):
        self.RE = RE

    def getP(self):
        return self.p

    def setP(self, p):
        self.p = p

    def getTs(self):
        return self.ts
    
    def setTs(self, ts):
        self.ts = ts

        