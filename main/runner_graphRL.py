from optimizer.offlineoptimizer.GraphRL.GraphRLOptimizer import GraphRlOptimizer
from network.Network import Network
from network.MobileCharger import MobileCharger
from iostream.Util import Util
import numpy as np
import os
import simpy

from network import Parameter
from main.parameter import get_args
args = get_args()
result_dir = args.result_dir
if not os.path.isdir(result_dir):
    os.mkdir(result_dir)

np.random.seed(42)


util = Util("data/ga{}_05_simulated.txt".format(args.dataset))
env = simpy.Environment()
net = Network(env=env, listNodes=util.listNodes, baseStation=util.BaseStation)
mc = MobileCharger(env=env, location=[250, 250])
testedT = args.time_test
algorithm = GraphRlOptimizer(
    args=args, env=env, T=args.time_T, testedT=testedT)

env.process(mc.operate(net, testedT, algorithm))
env.process(algorithm.controller(mcs=[mc], net=net))
env.process(net.runNetwork(testedT))

env.run(until=testedT)

file = open('{}/{}_{}_{}_{}.txt'.format(result_dir, args.dataset,
            args.algo, args.time_test, args.time_T), 'a')
print('Sum Energy Per T:        {}'.format(mc.sumEnergyPerT))
file.write('Sum Energy Per T:   {}\n'.format(mc.sumEnergyPerT))
# print('Sum EnergY:              {}'.format(mc.sumEnergy))
# file.write('Sum EnergY:         {}\n'.format(mc.sumEnergy))
# print('PCT remain battery:      {}%'.format(100 * (Parameter.MC_CAPACITY - mc.sumEnergyPerT) / Parameter.MC_CAPACITY))
# file.write('PCT remain battery: {}%\n'.format(100 * (Parameter.MC_CAPACITY - mc.sumEnergyPerT) / Parameter.MC_CAPACITY))
mc.setSumEnergyPerT()
print('Time: ' + str(env.now) +
      ' The number of dead node:' + str(net.countDeadNodes()))
file.write('Time: ' + str(env.now) + ' The number of dead node:' +
           str(net.countDeadNodes()) + '\n')
file.write('\n')
file.write('\n')
file.close()
