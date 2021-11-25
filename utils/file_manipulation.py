import os
import math

from numpy.core.defchararray import count

from experiments import algorithm

from utils import factor
from utils import constants

from wrsn import map
from wrsn import sensor
from wrsn import wce


class FileManiPulation:
    def __init__(self) -> None:
        pass

    def readFile(self, fileName: str, map: map.Map):
        file = open(fileName, 'r')
        x = y = p = e = 0         # Biến tạm thời 

        # Doc thong tin cac sensor
        countN = 0 # So luong sensor
        sumP = 0
        for i, data in enumerate(file.readlines()):
            if i == 0:
                constants.DEFAULT_X = float(data.split()[0])
                constants.DEFAULT_Y = float(data.split()[1])
            else:
                # dinh dang 1 dong trong file: x y p e
                data = data.split()
                x = float(data[0])
                y = float(data[1])
                p = float(data[2])
                e = float(data[3])

                p_temp = p

                p = p * factor.P_MULTIPLIER
                sumP += p
                e = e - p_temp * factor.t
                s = sensor.Sensor(x, y, p, e)
                map.setSensor(countN, s)
                countN += 1

        map.setN(countN)
        factor.N = int(2 * (4 + math.floor(3 * math.log(countN)))) # 4+floor(3*log(n)), so ca the trong quan the tinh theo thuat toan cma_es
        factor.P_BOUND = sumP / countN / 1.5
        
    def writeFile(self, al: algorithm.Algorithm, fileName: str, scenario_factor: int, algorithmNumber: int):
        constants.RESULT_DIRECTORY_PATH = constants.RESULT_DIRECTORY_PATH.replace('algorithm', factor.algorithm[algorithmNumber - 1])
        constants.RESULT_E_MOVE_DIRECTORY_PATH = constants.RESULT_E_MOVE_DIRECTORY_PATH.replace('algorithm', factor.algorithm[algorithmNumber - 1])

        st = fileName.split('/')
        actualFileName = st[-1]
        actualFileDir = '/'.join(st[:-1])

        resultFilePath = os.path.join(constants.RESULT_DIRECTORY_PATH, 'scenario_{}'.format(scenario_factor, actualFileDir, constants.RESULT_FILE))
        resultEmoveFilePath = os.path.join(constants.RESULT_E_MOVE_DIRECTORY_PATH, 'scenario_{}'.format(scenario_factor, actualFileDir, constants.RESULT_FILE))

        file = open(resultFilePath, 'w')
        if scenario_factor == 1:
            file.write("filename #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")
        elif scenario_factor == 2:
            file.write("filename U #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")         
        elif scenario_factor == 3:
            file.write("filename E_MC #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")      
        elif scenario_factor == 4:
            file.write("filename V #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")    
        elif scenario_factor == 5:
            file.write("filename T #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")    
        elif scenario_factor == 6:
            file.write("filename U E #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")   
        elif scenario_factor == 7:
            file.write("filename xP #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")   

        WCE_ = wce.WCE()

        line = ''
        if factor.SEED == 0:
            line += actualFileName + ' '
            if scenario_factor == 2:
                line += str(WCE_.U) + ' '
            elif scenario_factor == 3:
                line += str(WCE_.E_MC) + ' '
            elif scenario_factor == 4:
                line += str(WCE_.V) + ' '
            elif scenario_factor == 5:
                line += str(factor.T) + ' '
            elif scenario_factor == 6:
                line += str(WCE_.U) + ' ' + str(WCE_.E_MC) + ' '
            elif scenario_factor == 7:
                line += str(factor.P_MULTIPLIER) + ' '
        
        line += str(len(al.getDeadNodes())) + ' '
        file.write(line)

        if factor.SEED == 9:
            file.write('\n')

        fileEmove = open(resultEmoveFilePath, 'w')
        lineEmove = ''
        if factor.SEED == 0:
            lineEmove += actualFileName + ' '
            if scenario_factor == 2:
                lineEmove += str(WCE_.U) + ' '
            elif scenario_factor == 3:
                lineEmove += str(WCE_.E_MC) + ' '
            elif scenario_factor == 4:
                lineEmove += str(WCE_.V) + ' '
            elif scenario_factor == 5:
                lineEmove += str(factor.T) + ' '
            elif scenario_factor == 6:
                lineEmove += str(WCE_.U) + ' ' + str(WCE_.E_MC) + ' '
            elif scenario_factor == 7:
                lineEmove += str(factor.P_MULTIPLIER) + ' '
        
        lineEmove += str(al.geteMoveRatio()) + ' '
        fileEmove.write(lineEmove)

        if factor.SEED == 9:
            fileEmove.write('\n')

    def writeTimeFile(self, al: algorithm.Algorithm, fileName: str, scenario_factor: int, algorithmNumber: int):
        constants.RESULT_DIRECTORY_PATH_TIME = constants.RESULT_DIRECTORY_PATH_TIME.replace('algorithm', factor.algorithm[algorithmNumber - 1])

        st = fileName.split('/')
        actualFileName = st[-1]
        actualFileDir = '/'.join(st[:-1])

        resultFilePath = os.path.join(constants.RESULT_DIRECTORY_PATH_TIME, 'scenario_{}'.format(scenario_factor, actualFileDir, constants.RESULT_FILE))

        file = open(resultFilePath, 'w')
        if scenario_factor == 1:
            file.write("filename #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")
        elif scenario_factor == 2:
            file.write("filename U #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")         
        elif scenario_factor == 3:
            file.write("filename E_MC #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")      
        elif scenario_factor == 4:
            file.write("filename V #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")    
        elif scenario_factor == 5:
            file.write("filename T #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")    
        elif scenario_factor == 6:
            file.write("filename U E #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")   
        elif scenario_factor == 7:
            file.write("filename xP #1 #2 #3 #4 #5 #6 #7 #8 #9 #10")   

        WCE_ = wce.WCE()

        line = ''
        if factor.SEED == 0:
            line += actualFileName + ' '
            if scenario_factor == 2:
                line += str(WCE_.U) + ' '
            elif scenario_factor == 3:
                line += str(WCE_.E_MC) + ' '
            elif scenario_factor == 4:
                line += str(WCE_.V) + ' '
            elif scenario_factor == 5:
                line += str(factor.T) + ' '
            elif scenario_factor == 6:
                line += str(WCE_.U) + ' ' + str(WCE_.E_MC) + ' '
            elif scenario_factor == 7:
                line += str(factor.P_MULTIPLIER) + ' '
        
        line += str(len(al.getExecutedTime())) + ' '
        file.write(line)