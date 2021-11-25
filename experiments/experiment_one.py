from math import fabs
import os
import numpy as np

from utils import constants
from utils import file_manipulation
from utils import factor

from wrsn import map
from wrsn import wce


class ExperimentOne:
    def __init__(self):
        pass

    def execute(self, filename: str, scenario_factor: int, algorithmNumber: int):
        print('Filename: {}'.format(filename))
        st = filename.split('/')

        actualFileName = st[-1]
        actualFileDir = '/'.join(st[:-1])

        constants.RESULT_DIRECTORY_PATH = constants.RESULT_DIRECTORY_PATH.replace('algorithm', factor.algorithm[algorithmNumber - 1])
        constants.RESULT_DIRECTORY_PATH_TIME = constants.RESULT_DIRECTORY_PATH.replace('algorithm', factor.algorithm[algorithmNumber - 1])

        resultFilePath = os.path.join(constants.RESULT_DIRECTORY_PATH, 'scenario_{}'.format(scenario_factor, actualFileDir, constants.RESULT_FILE))

        # if os.path.isfile(resultFilePath):