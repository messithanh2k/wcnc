import os
import numpy as np 
np.random.seed(42)

import ga
from parameters import get_args
args = get_args()


# Y = w1x1 + w2x2 + w3x3 + w4x4 + w5x5 + w6x6 + ... + wnxn

# Number of the weights we are looking to optimize
num_weights = args.num_weights
# Input
equation_inputs = np.random.rand(num_weights, 1)
print('Input: {}'.format(equation_inputs))
# Solution per population
sol_per_pop = args.pop_size
# Definining the population
pop_size = (sol_per_pop, num_weights)
# Creating the initial population
new_population = np.random.uniform(low=-args.maxmin, high=args.maxmin, size=pop_size)

num_generations = args.num_generations
num_parents_mating = args.parents_size

for generation in range(num_generations):
    # Measuring the fitness of each chromosome in the population.
    fitness = ga.cal_pop_fitness(equation_inputs, new_population)
    # Selecting the best parents in the population for mating.
    parents = ga.select_mating_pool(new_population, fitness, 
                                    num_parents_mating)

    # Generating next generation using crossover.
    offspring_crossover = ga.crossover(parents,
            offspring_size=(pop_size[0]-parents.shape[0], num_weights))

    # Adding some variations to the offsrping using mutation.
    offspring_mutation = ga.mutation(offspring_crossover)# Creating the new population based on the parents and offspring.
    new_population[0:parents.shape[0], :] = parents
    new_population[parents.shape[0]:, :] = offspring_mutation
    print('Best result after generation {}: {}\n'.format(generation, np.max(fitness)))