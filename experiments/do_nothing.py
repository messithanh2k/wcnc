import os
import numpy as np
from experiments.algorithm import Algorithm

from utils import factor

from wrsn.map import Map
from wrsn.sensor import Sensor


class DoNothing(Algorithm):
    def __init__(self):
        super().__init__()
    
    def execute(self, map: Map):
        N = map.getN()
        deadNodeCount = 0
        for i in range(N):
            s = map.getSensor(i)
            s.calculateE(factor.T)
            if s.getE() < self.Sensor_.E_MIN:
                deadNodeCount += 1
                self.deadNodes.append(i)