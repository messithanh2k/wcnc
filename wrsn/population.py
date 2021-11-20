import os
import numpy as np
from numpy.core.fromnumeric import sort

from wrsn.individual import Individual
from wrsn.map import Map
from wrsn.population import Population


class Population:
    def __init__(self, nIndividuals=None, map: Map=None, p: Population=None, best_path_individual: Individual=None):
        self.N = 0 # so luong ca the trong quan the
        self.individuals = [] # tap hop cac ca the

        if (nIndividuals != None) & (map != None):
            self.N = nIndividuals
            for i in range(0, self.N):
                self.setIndividual(i, Individual(map))

            self.individuals = sorted(self.individuals, key=lambda x: x.getFitnessF())
        
        # Khoi tao quan the tai to hop tu quan the ban dau
        elif (nIndividuals != None) & (p != None):
            self.N = nIndividuals
            for i in range(self.N):
                self.setIndividual(i, p.getIndividual(i))
        
        # Phase 2
        elif (nIndividuals != None) & (map != None) & (best_path_individual != None):
            self.N = nIndividuals
            for i in range(self.N):
                self.setIndividual(i, Individual(map, best_path_individual))
            
            self.individuals = sorted(self.individuals, key=lambda x: x.getFitnessF())

    def getN(self):
        return self.N
    
    def setN(self, n):
        self.N = n
    
    def getIndividuals(self):
        return self.individuals
    
    def setIndividuals(self, individuals):
        self.individuals = individuals
    
    def getIndividual(self, index):
        return self.individuals[index]
    
    def setIndividual(self, index, individual: Individual):
        if index > len(self.individuals - 1):
            self.individuals.append(individual)
        else:
            self.individuals[index] = individual