P_MULTIPLIER = 1
P_MULTIPLIER_1 = 1.5
P_MULTIPLIER_2 = 2
P_BOUND = 0

# Tham so chu ki sac
T = 24800
t = 15000
T_INTERVAL = 10
BS_INDEX = -1
WCE_INDEX = -2
M = 1000
P_MIN = 1e-5

# Trang thai xe sac WCE
IDLE = 1
MOVING = 2
CHARGING = 3
REPLENISH = 4

# Tham so thuat toan
STOP_EVAL = 1e5
STOP_FITNESS = 0.0
SEEDS = 10
SEED = 0
N = 200

# Tham so thuat toan GACS
# phase 1
CROSSOVER_RATE_1 = 0.75
PMX_RATE = 0.5
SPX_RATE = 0.5
MUTATION_RATE_1 = 0.1
SWAP_RATE = 0.5
CIM_RATE = 0.5

# Tham so thuat toan PA
alpha = 0.6

# Tham so fitness f
ALPHA = 0.5

# For experiment 20/08/2020
E_MAX = 10800 # Huy tự đặt cho tiện
E_MIN = 0 # Huy tự đặt cho tiện
U_DEFAULT = 5
E_MC_DEFAULT = 108000
V_DEFAULT = 5
T_DEFAULT = 24800
P_DEFAULT = 1

scenario_factor = {1, 2, 7}
algorithm = {"hgacmaes", "samer", "gacs", "fcfs", "pa\\" + str(alpha), "inma", "nah"}
Us = {2, 3, 4, 5, 6, 7, 8, 9, 10}
E_MS_1s = {54000, 81000, 108000, 162000, 216000, 324000}
E_MC_2s = {13500, 27000, 54000, 81000, 108000}
Vs = {2, 5, 7, 10, 15, 20}
Ts = {32000, 72000, 86400, 129600, 172800, 216000, 720000}
Ps = {0.5, 1, 1.5, 2, 2.5, 3}