import os
import numpy as np

from utils.constants import *
from utils.factor import *


class WCE:
    def __init__(self):
        self.E_MC = E_MC_DEFAULT # Jun
        self.P_M = P_DEFAULT # W/s
        self.V = V_DEFAULT # m/s
        self.U = U_DEFAULT # 
        self.cX = DEFAULT_X # Hoanh do cua WCE hien tai
        self.cY = DEFAULT_Y # Tung do cua WCE hien tai
        self.RE = self.E_MC
        self.state = IDLE

    def getcX(self):
        return self.cX

    def setcX(self, cX):
        self.cX = cX
    
    def getcY(self):
        return self.cY

    def setcY(self, cY):
        self.cY = cY
    
    def getState(self):
        return self.state
    
    def setState(self, state):
        self.state = state
