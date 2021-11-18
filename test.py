import os
import numpy as np 
np.random.seed(42)

from ga import *


# Y = w1x1 + w2x2 + w3x3 + w4x4 + w5x5 + w6x6 + ... + wnxn

# Input
equation_inputs = np.array([4, -2, 3.5, 5, -11, -4.7]).reshape(-1, 1)

# Number of the weights we are looking to optimize
num_weights = 6

# Solution per population
sol_per_pop = 8

# Definining the population
pop_size = (sol_per_pop, num_weights)

# Creating the initial population
new_population = np.random.uniform(low=-4, high=4, size=pop_size)

print('Population shape: {}'.format(new_population.shape))
print('Equation_input shape: {}'.format(equation_inputs.shape))
print('Population: \n{}'.format(new_population))

pop_fitness = cal_pop_fitness(equation_inputs, new_population)
print('Population Fitness: \n{}'.format(pop_fitness))

num_parents = 4
parents_mating = select_mating_pool(new_population, pop_fitness, num_parents)
print('Parents Mating: \n{}'.format(parents_mating))

os_crossover = crossover(parents_mating, (new_population.shape[0] - parents_mating.shape[0], num_weights)) 
print('OS Crossover: \n{}'.format(os_crossover))

os_mutation = mutation(os_crossover)
print('OS Mutation: \n{}'.format(os_mutation))