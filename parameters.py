import os
import argparse


def get_args():
    parser = argparse.ArgumentParser()

    parser.add_argument('--num_weights', type=int, default=6)
    parser.add_argument('--pop_size', type=int, default=8)

    parser.add_argument('--maxmin', type=int, default=4)
    parser.add_argument('--num_generations', type=int, default=10)
    parser.add_argument('--parents_size', type=int, default=4)

    args = parser.parse_args()

    return args