import os
import sys
import simpy

from iostream.Util import Util
from network.MobileCharger import MobileCharger
from network.Network import Network
from optimizer.offlineoptimizer.RandomOffineOptimizer import RandomOfflineOptimizer
from optimizer.offlineoptimizer.GraphRL.GraphRLOptimizer import GraphRlOptimizer

util = Util("data/ga200_05_simulated.txt")
env = simpy.Environment()
# print(util.listNodes)
# sys.exit()
net = Network(env=env, listNodes=util.listNodes, baseStation=util.BaseStation)
mc = MobileCharger(env=env, location=[250, 250])
testedT = 72000
algorithm = RandomOfflineOptimizer(env=env)
# algorithm = GraphRlOptimizer(env=env, T=500, testedT=testedT)


env.process(mc.operate(net, testedT, algorithm))
env.process(algorithm.controller(mcs=[mc], net=net))
env.process(net.runNetwork(testedT))

env.run(until=testedT)
print('Death Node: {}'.format(net.countDeadNodes()))
