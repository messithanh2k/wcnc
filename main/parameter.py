import os
import argparse


def get_args():
    parser = argparse.ArgumentParser()

    parser.add_argument('--result_dir', type=str, \
                        default='result')

    parser.add_argument('--algo', type=str, \
                        default='monte_carlo')
    
    parser.add_argument('--time_T', type=int, \
                        default=18000)
    
    parser.add_argument('--time_test', type=int, \
                        default=72000)

    parser.add_argument('--dataset', type=int, \
                        default=200)

    args = parser.parse_args()

    return args