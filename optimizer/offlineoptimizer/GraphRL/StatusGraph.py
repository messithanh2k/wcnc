import math
import random
import numpy as np

from scipy.spatial import distance

from optimizer.offlineoptimizer.GraphRL.Vertex import Vertex


class StatusGraph:
    def __init__(self, args, net, mc, Esafe, delta=100, T=20000):
        self.args = args
        self.delta = delta
        self.net = net
        self.mc = mc
        self.mapNodeVertexes = [{} for i in range(len(self.net.listNodes))]
        self.T = T
        self.Esafe = Esafe
        self.path = []
        self.Gt = []
        self.U = self.mc.alpha / (self.mc.beta ** 2)
        self.start = Vertex(0, 0, 0)
        self.visited = [False for i in range(len(self.net.listNodes))]
        self.epi = 1

    def upperNormalize(self, value):
        tmp = int(value / self.delta)
        if tmp * self.delta < value:
            tmp += 1
        return tmp * self.delta

    def lowerNormalize(self, value):
        return int(value / self.delta) * self.delta

    def initialize(self):
        for node in self.net.listNodes:
            if node.status == 0:
                continue
            # lower bound of arrival time
            lowerBound = self.upperNormalize(
                distance.euclidean(node.location, self.net.baseStation.location) / self.mc.velocity)

            # upper bound of arrival time
            upperBound = self.lowerNormalize(min(self.T - distance.euclidean(node.location,
                                                                             self.net.baseStation.location) /
                                                 self.mc.velocity,
                                                 (node.energy - node.threshold) / node.energyCR))
            upperBound -= self.delta
            tcSafe = (self.Esafe[node.id] - (node.energy - self.T * node.energyCR)) / self.U
            # trimming the tree
            # the node with lots of energy will no need more energy so we will charge full for these node
            if tcSafe <= 0:
                if random.random() < 0.7:
                    continue
                else:
                    tcSafe = random.random() * ((node.capacity - (node.energy - self.T * node.energyCR)) / self.U)
            l = lowerBound
            # discrete the time
            while l <= upperBound:
                reward = 0
                endVertex = False
                tc = min(tcSafe,
                         (node.capacity - (node.energy - (l + self.delta) * node.energyCR)) / (self.U - node.energyCR))
                # to fix the charging time if it exceeds T
                if l + self.delta + tc + distance.euclidean(node.location, self.net.baseStation.location) / \
                        self.mc.velocity >= self.T - 1:
                    tc = self.T - distance.euclidean(node.location, self.net.baseStation.location) / \
                         self.mc.velocity - l - self.delta
                    endVertex = True
                # reward obtained by charging
                reward += tc * self.U / node.capacity
                # reward for saving a risky node
                if node.energy - self.T * node.energyCR <= node.threshold and node.energy - self.T * node.energyCR + tc * self.U >= \
                        self.Esafe[node.id]:
                    reward += 100
                elif node.energy - self.T * node.energyCR <= self.Esafe[
                    node.id] <= node.energy - self.T * node.energyCR + tc * self.U:
                    reward += 10
                self.mapNodeVertexes[node.id][l] = Vertex(node=node, lowerArrivalTime=l,
                                                          upperArrivalTime=l + self.delta,
                                                          chargingTime=tc, endVertex=endVertex, reward=reward)
                l += self.delta

        for dic in self.mapNodeVertexes:
            for vertex in dic.values():
                self.setNeighbors(vertex)
        for i, dic in enumerate(self.mapNodeVertexes):
            if self.upperNormalize(
                    distance.euclidean(self.net.listNodes[i].location
                        , self.net.baseStation.location) / self.mc.velocity) in dic.keys():
                self.start.adjacent.append(dic[self.upperNormalize(
                    distance.euclidean(self.net.listNodes[i].location
                                       , self.net.baseStation.location) / self.mc.velocity)])
        self.epi = 1
        
        if self.args.algo == 'monte_carlo':
            self.monte_carlo()
        elif self.args.algo == 'sarsa':
            self.sarsa()
        elif self.args.algo == 'expected_sarsa':
            pass

    def setNeighbors(self, vertex):
        sumTime = 0
        if vertex.endVertex:
            return
        for node in self.net.listNodes:
            if vertex.node == node:
                continue
            sumTime = vertex.chargingTime + distance.euclidean(node.location,
                                                               vertex.node.location) / self.mc.velocity
            sumTime = self.upperNormalize(sumTime)
            if sumTime + vertex.lowerArrivalTime in self.mapNodeVertexes[node.id].keys():
                vertex.adjacent.append(self.mapNodeVertexes[node.id][vertex.lowerArrivalTime + sumTime])

    def dfs(self, vertex, epsilon):
        if len(vertex.adjacent) == 0:
            return
        next = None
        tmp = []
        for ver in vertex.adjacent:
            if not self.visited[ver.node.id]:
                tmp.append(ver)
                if next is None:
                    next = ver
                    continue
                if ver.value + ver.reward > next.value + next.reward:
                    next = ver
        # print(type(next))
        if next is None:
            return
        # if random.random() < self.epi:
        if random.random() < epsilon:
            next = tmp[random.randint(0, len(tmp) - 1)] 
        self.path.append(next)
        if len(self.Gt) == 0:
            self.Gt.append(next.reward)
        else:
            self.Gt.append(next.reward + self.Gt[-1])
        self.visited[next.node.id] = True
        self.dfs(next, epsilon)
    
    def monte_carlo(self):
        for i in range(1, 30000):
            if i == 30000:
                self.epi = 0
            self.visited = [False for i in range(len(self.net.listNodes))]
            self.path.clear()
            self.Gt.clear()
            self.dfs(self.start, self.epi)
            # print(self.Gt)
            # print(np.sum(np.array(self.Gt)))
            # print(str(i)+" "+str(self.Gt[-1]))
            for j, vertex in enumerate(self.path):
                vertex.N += 1
                vertex.value = vertex.value + 1.0 / vertex.N * (self.Gt[-1] - self.Gt[j] - vertex.value)
            self.epi = 1 / math.sqrt(i)

    def sarsa(self):    
        total_episodes = 1000
        max_steps = 100
        epsilon = 0.8
        alpha = 0.05
        gamma = 0.95

        def choose_action(vertex, epsilon):
            if len(vertex.adjacent) == 0:
                return None
            next = None
            tmp = []
            for ver in vertex.adjacent:
                if not self.visited[ver.node.id]:
                    if next is None:
                        next = ver
                        tmp.append(ver)
                        continue
                    if ver.value > next.value:
                        next = ver
            if next is None:
                return None
            if random.random() < epsilon:
                next = tmp[random.randint(0, len(tmp) - 1)]
            self.visited[next.node.id] = True

            return next

        for i in range(1, total_episodes):
            sum_reward = 0
            count = 0

            vertex = self.start
            self.visited = [False for i in range(len(self.net.listNodes))]
            self.path.clear()
            while count < max_steps:
                count += 1
                
                vertex_2 = choose_action(vertex, epsilon)
                if vertex_2 is None:
                    break
                self.path.append(vertex_2)
                sum_reward += vertex_2.reward
                vertex.value = vertex.value + alpha * (vertex_2.reward + gamma * vertex_2.value - vertex.value)

                vertex = vertex_2
            
            # print('Total reward: {}'.format(sum_reward))


    