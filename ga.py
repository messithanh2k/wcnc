import os 
import sys
import numpy as np


def cal_pop_fitness(equation_inputs, pop):
    '''
    Args:
        - equation_inputs: [np.array: (nums_gens, 1)] input X
        - pop: [np.array: (pop_size, nums_gens)] weight W
    Return:
        - fitness: [np.array: (pop_size, 1)] population fitness
    '''
    fitness = pop @ equation_inputs
    return fitness

def select_mating_pool(pop, fitness, num_parents):
    # Selecting the best individuals in the current generation as parents 
    # for producing the offspring of the next generation.
    '''
    Args:
        - pop: [np.array: (pop_size, gens_size)] weight W
        - fitness: [np.array: (pop_size, 1)] population fitness
        - num_parents: [int] nums parents mating
    Return:
        - parents: (num_parents, gens_size) 
    '''
    parents = np.zeros((num_parents, pop.shape[1]))
    for parent_num in range(num_parents):
        max_fitness_idx = np.where(fitness == np.max(fitness))
        max_fitness_idx = max_fitness_idx[0][0]
        # print(max_fitness_idx)
        parents[parent_num, :] = pop[max_fitness_idx, :]
        fitness[max_fitness_idx] = -sys.maxsize # -inf
 
    return parents

def crossover(parents, offspring_size):
    '''
    Args:
        - parents: [np.array: (num_parents, gens_size)]
        - offspring_size: [np.array: (pop_size - parents_size, gens_size)] 
    Return:
        - offspring: [np.array: (pop_size - parents_size, gens_size)]
    '''
    offspring = np.zeros(offspring_size)
    # The point at which crossover takes place between two parents
    # Usually, it is at the center.
    crossover_point = np.uint8(offspring_size[1] / 2)

    for k in range(offspring_size[0]):
        # Index of the first parent to mate.
        parent1_idx = k % parents.shape[0]
        # Index of the second parent to mate.
        parent2_idx = (k + 1) % parents.shape[0]

        offspring[k, 0: crossover_point] = parents[parent1_idx, 0: crossover_point]
        offspring[k, crossover_point:] = parents[parent2_idx, crossover_point:]
    
    return offspring

def mutation(offspring_crossover):
    '''
    Args:
        - offspring_crossover: [np.array: (pop_size - parents_size, gens_size)]
    Return:
        - offspring_crossover: [same as input] mutation
    '''
    # Mutation changes a single gene in each offspring randomly.
    for idx in range(offspring_crossover.shape[0]):
        # The random value to be added to the gene.
        random_value = np.random.uniform(-1.0, 1.0, 1)
        offspring_crossover[idx, 4] = offspring_crossover[idx, 4] + random_value

    return offspring_crossover