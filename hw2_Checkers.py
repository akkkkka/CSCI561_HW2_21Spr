import copy
import math
import time
import random


class Node():  # node is the board
    def __init__(self, state, parent, start, action):
        self.state = state  # the current board
        self.parent = parent  # the last single step board
        self.start = start
        self.action = action  # current single step
        self.score = 1  # how many checkers are captured


# define queue's operation
class QueueOper():
    def __init__(self):
        self.storage = []
        self.imporinfo = set()

    def add(self, node):
        self.storage.append(node)
        # self.imporinfo.add((node.state, node.score))

    def delete(self, node):  # delete the dedicated node
        self.storage.remove(node)
        # self.imporinfo.remove((node.state, node.score))

    def empty(self):
        return len(self.storage) == 0
    
    def remove(self):  # pop the first one out
        node = self.storage[0]
        self.storage = self.storage[1:]
        # self.imporinfo.remove()
        return node
        
    def contains_state(self, state):  # TODO: change list to set
        # storage_set = set(self.storage)
        for node in self.storage:
            if node.state == state:
                return node
        return None

    def sort(self):
        self.storage.sort(key = lambda x: x.score, reverse=True)


DIRECTORY = "input.txt"
information = {}
ROW = 8
COL = 8
ORIGIN_BOARD = [(i, j) for i in range(ROW) for j in range(COL) if (i + j) % 2 != 0]
# print(ORIGIN_BOARD)
DEPTH_LIMIT = 5  # change this according to time limit, iterative deepening search
BLACK_LOSE = -4000000
WHITE_LOSE = 4000000

def load_chessboard():
    """
    Load data from input text files into memory.
    """
    with open(DIRECTORY, "r") as f:
        lines = f.readlines()

    # chosen test format(SINGLE or GAME)
    information["test"] = lines[0].rstrip()
    
    # chosen player: decide playing order
    information["player"] = lines[1].rstrip()

    # chosen time restriction
    information["time"] = float(lines[2].rstrip())

    # chessboard
    rawboard = [i.split() for i in lines[-ROW:]]
    chessboard = []
    for i in range(len(rawboard)): 
        chessboard.append(list(letter for letter in rawboard[i][0]))
    # change the forbidden cells to 'X'
    # for j in range(ROW):
    #     chessboard[j] = ['X' if k%2 else x for k,x in enumerate(chessboard[j])] if j%2 \
    #                 else ['X' if not k%2 else x for k,x in enumerate(chessboard[j])]
    information["board"] = chessboard


def actions(coordinates, board):  # coordinates is the beginning cell
    """
    Returns a dictionary with "regular" key and "jump" key of all possible single step actions (i, j)
    available for the dedicated cell on the board for this cell's player.
    """
    regular = set()
    jump = set()  # jump is a set of jump steps (tuple)
    jump_exist = False
    actions = {"begin": coordinates, "regular": regular, "jump": jump}
    if board[coordinates[0]][coordinates[1]].lower() == "b":
        opponent = "w"
    elif board[coordinates[0]][coordinates[1]].lower() == "w":
        opponent = "b"
    else:  # == "."
        opponent = None
    k = 0 if board[coordinates[0]][coordinates[1]] == "b" else 1
    l = 1 if board[coordinates[0]][coordinates[1]] == "w" else 2
    for i in range(coordinates[0] - k, coordinates[0] + l):
        for j in range(coordinates[1] - 1, coordinates[1] + 2):
            if 0 <= i < ROW and 0 <= j < COL:
                if (i, j) == coordinates:
                    continue
                if i != coordinates[0] and j != coordinates[1]:  # diagonal
                    if board[i][j].isalpha() and\
                        board[i][j].lower() == opponent and\
                            0 <= 2*i-coordinates[0] < ROW and 0 <= 2*j-coordinates[1] < COL and\
                                board[2*i-coordinates[0]][2*j-coordinates[1]] == ".":  # jump
                        jump.add((2*i-coordinates[0], 2*j-coordinates[1]))
                        jump_exist = True
                    if not jump_exist:  # if jump not exists, then record regular move
                        if board[i][j] == ".":
                            regular.add((i, j))
    # print(f"actions: {actions}")
    if jump_exist:
        actions["jump"] = jump  # only add jump
        return actions  # {"begin": coordinates, "regular": {}, "jump": jump}
    actions["regular"] = regular
    return actions  # {"begin": coordinates, "regular": regular, "jump": {}} may also empty


def get_move(player, board):
    """
    Force to jump for current player. Returns two dictionary: 
    first, list of dictionaries, [{"begin": coordinates, "regular": {regular set}}, ...], if no regular move, return None.
    second, [{"begin": coordinates, "jump": {jump set}}, ...], if no jump, return None.
    """
    regular = []
    jump = []
    jump_exist = False
    for i,j in ORIGIN_BOARD:
        if board[i][j].isalpha() and board[i][j].lower() == player:
            actiondic = actions((i, j), board)
            if len(actiondic["jump"]) != 0:
                jump_exist = True
                jump_dic = {"begin": (i,j)}
                jump_dic["jump"] = actiondic["jump"]
                # print(jump_dic)
                jump.append(jump_dic)
            if not jump_exist:  # if jump not exists, then record regular move
                if len(actiondic["regular"]) != 0:
                    regu_dic = {"begin": (i,j)}
                    regu_dic["regular"] = actiondic["regular"]
                    regular.append(regu_dic)
            # if all == 0, do nothing
    if len(jump) == 0 and len(regular) == 0:
        return None, None
    elif len(jump) == 0 and len(regular) != 0:
        return regular, None
    else:  # jumps exist, only return jump
        return None, jump


def single_mode(board, player):  # player == "b" or "w"
    regular, jump = get_move(player, board)
    if regular == None and jump == None:  # N,N / regu,N / N,jump
        return None
    elif jump != None:  # exists jump, just loop jump: [{"begin": coordinates, "jump": {jump set}}, ...]
        for actiondic in jump:  # actiondic = {"begin": coordinates, "jump": {jump set}}
            for action in actiondic["jump"]:  # actiondic["jump"] = {jump set}
                node, currboard = consecutive_jump(actiondic["begin"], action, board)
    else:  # only exists regular = {{"begin": coordinates, "regular": {regular set}}, ...}
        for actiondic in regular:  # actiondic = {"begin": coordinates, "regular": {regular set}}
            action = random.choice(tuple(actiondic["regular"]))
            node = Node(state = board, parent = None, start = actiondic["begin"], action = action)
    pathlist = find_path(node)
    return pathlist


def result(board, begin, action, is_jump):  # action is (1,2)
    """
    Returns the board that results from making a single move (i, j) on the board.
    """
    result = copy.deepcopy(board)
    result[action[0]][action[1]] = board[begin[0]][begin[1]]  # take the action
    if action[0] == 0 or action[0] == ROW - 1:  # if equal to the edge row, became king
        result[action[0]][action[1]] = result[action[0]][action[1]].upper()
    result[begin[0]][begin[1]] = "."  # remove the original checker
    if is_jump == True:  # jump, need to capture
        result[int((begin[0] + action[0]) / 2)][int((begin[1] + action[1]) / 2)] = "."  # capture opposite's checker
    return result


regular_val = 1  # TODO: just for test
king_val = 2
def evaluation(board):
    """
    Figures how good current board is (some heuristic factor) for the player and opponent.
    Returns BLACK's score by subtracting WHITE's checker score from the BLACK's.
    """
    global DEPTH_LIMIT
    b_king = set()
    w_king = set()
    b_regu = set()
    w_regu = set()
    b = 0
    w = 0
    black = 0
    white = 0

    for i,j in ORIGIN_BOARD:
        if board[i][j] == "b":
            b_regu.add((i,j))
            b += 1
        elif board[i][j] == "w":
            w_regu.add((i,j))
            w += 1
        elif board[i][j] == "B":
            b_king.add((i,j))
            b += 1
        elif board[i][j] == "W":
            w_king.add((i,j))
            w += 1

    black += len(b_regu) * regular_val + len(b_king) * king_val
    white += len(w_regu) * regular_val + len(w_king) * king_val

    if information["player"] == "BLACK":
        mypieces = b
        oppopieces = w
    else:
        mypieces = w
        oppopieces = b
    # if w + b <= 12:
    #     DEPTH_LIMIT = 6
    if w + b <= 9 and mypieces < oppopieces:
        # print(mypieces)
        # print(oppopieces)
        DEPTH_LIMIT = 3
    if w + b <= 5 and mypieces > oppopieces:  # foresee the end of the game
        DEPTH_LIMIT = 4

    return black - white


def find_path(node):
    """Find the path and return a list of all the jumps' starting and ending coordinates."""
    if node == None:
        return None
    steps = []
    while True:
        steps.append((node.start, node.action))  # record the start position to end position of each step
        if node.parent == None:
            break
        node = node.parent
    steps.reverse()
    return steps


def str_convert(actions):  # actions = [((1,2), (3,4)), (begin, end), ...]
    """Convert solution list to formatted string and return it. """
    print_steps = []
    if len(actions) == 1:
        if abs(actions[0][1][0] - actions[0][0][0]) == 1:  # actions[0] = ((1,2), (3,4))
            path_str = f"E {str(chr(actions[0][0][1] + 97)) + str(8 - actions[0][0][0])} {str(chr(actions[0][1][1] + 97)) + str(8 - actions[0][1][0])}"  # coordinates[0] = (1, 5)
        else:
            path_str = f"J {str(chr(actions[0][0][1] + 97)) + str(8 - actions[0][0][0])} {str(chr(actions[0][1][1] + 97)) + str(8 - actions[0][1][0])}"  # coordinates[0] = (1, 5)
        print_steps.append(path_str)
    else:
        board = information["board"]
        for coordinates in actions:
            path_str = f"J {str(chr(coordinates[0][1] + 97)) + str(8 - coordinates[0][0])} {str(chr(coordinates[1][1] + 97)) + str(8 - coordinates[1][0])}"  # coordinates[0] = (1, 5)
            print_steps.append(path_str)
    return print_steps


def consecutive_jump(begin, action, board):  # action = (1,2), the next step
    """
    Taking current chosen jump, checking for another jump.
    If there are possible jumps, explore each of them recursively, returns the longest jumpable path and the final board.
    If no possible jump, return None.
    """
    start = Node(state = board, parent = None, start = begin, action = action)  # state: current board, action: the step we will do on this board
    
    queue = QueueOper()
    queue.add(start)
    explored = QueueOper()
    
    while True:  # action is (start, [steps], val)
        if queue.empty():
            # only when you want to return the step, you can add another step in the [steps]
            explored.sort()  # explored couldn't be empty, explored.storage = [node, node, ...]
            return explored.storage[0], explored.storage[0].state  # not the final board? has to call result
        currnode = queue.remove()
        explored.add(currnode)

        # take the action
        newboard = result(currnode.state, currnode.start, currnode.action, True)
        if currnode.state[currnode.start[0]][currnode.start[1]].islower() and\
            newboard[action[0]][action[1]].isupper():  # is upper, is king, this turn ends
            explored.sort()
            return explored.storage[0], explored.storage[0].state
        # isn't promoted to king, keep going
        actionlist =  actions(currnode.action, newboard)  # TODO: currnode.action?
        # actionlist = {"begin": coordinates, "regular": regular, "jump": jump}
        while len(actionlist["jump"]) != 0:  # exist another jump
            action = actionlist["jump"].pop()  # remove one of the actions, and return it
            child = Node(state = newboard, parent = currnode, start = currnode.action, action = action)  # only add jump move
            child.score = child.parent.score + 1
            if queue.contains_state(newboard) == None and explored.contains_state(newboard) == None:
                queue.add(child)
            elif queue.contains_state(newboard) != None:
                node = queue.contains_state(newboard)
                if child.score > node.score:  # if capture more checkers
                    queue.add(child)
            elif explored.contains_state(newboard) != None:
                node = explored.contains_state(newboard)
                if child.score > node.score:
                    explored.delete(node)  # delete the node in explored
                    queue.add(child)
        queue.sort()


# when the clock runs out we use the solution found at the previous depth limit
def terminal(depth):
    """
    Decide current time, check if exceeds the time limit
    (the given time limit or the time you decide to give one circulation?)
    or exceeds the depth limit (decide by programmer, can change).
    If not, return False, else return True.
    """
    currtime = time.time()
    if (currtime - information["starting_time"]) >= 10 or depth > DEPTH_LIMIT or (currtime - information["starting_time"]) >= information["time"] / 2:  # TODO: may change to 2/3
    # if depth > DEPTH_LIMIT:
        return True
    return False


def alpha_beta_search(board):  # return action = [(begin, end), (begin, end), ...]
    """
    Returns an action of the current player by doing alpha beta pruning with minimax algorithm.
    """
    global DEPTH_LIMIT
    if information["player"] == "BLACK":
        for DEPTH_LIMIT in range(1, 8):
            # if DEPTH_LIMIT % 2 != 0:  # only calculate odd depth
            # print(f"DEPTH_LIMIT: {DEPTH_LIMIT}")
            v, node = max_value(0, board, float("-inf"), float("inf"))  # a, b are both local variables
            currtime = time.time()
            if (currtime - information["starting_time"]) >= 10 or (currtime - information["starting_time"]) >= information["time"] / 2:  # TODO: cannot use information time, may use 30s
                # When the clock runs out half of the time: use previous solution.
                break
    else:
        for DEPTH_LIMIT in range(1, 8):  # 1~7
            # if DEPTH_LIMIT % 2 != 0:
            # print(f"DEPTH_LIMIT: {DEPTH_LIMIT}")
            v, node = min_value(0, board, float("-inf"), float("inf"))
            currtime = time.time()
            if (currtime - information["starting_time"]) >= 10 or (currtime - information["starting_time"]) >= information["time"] / 2:
                break
    # step could be a node, solution: make one step also be a node, and return a node
    pathlist = find_path(node)
    return pathlist


def max_value(currdepth, board, a, b):  # black is max
    """
    Returns the evaluation score of the current board, and the last action node (can find path from it).  
    """
    # print(f"depth: {currdepth}")
    if terminal(currdepth):  # change terminal condition, use IDS
        return evaluation(board), None
    v = float("-inf")

    regular, jump = get_move("b", board)  # all (i,j)'s actions
    if regular == None and jump == None:  # N,N / regu,N / N,jump
        # if for all (i,j), no action, then return LOSE, None, opponent wins
        return BLACK_LOSE, None
    elif jump != None:  # exists jump, just loop jump: [{"begin": coordinates, "jump": {jump set}}, ...]
        for actiondic in jump:  # actiondic = {"begin": coordinates, "jump": {jump set}}
            for action in actiondic["jump"]:  # actiondic["jump"] = {jump set}
                node, currboard = consecutive_jump(actiondic["begin"], action, board)  # node actually means [actions]
                result_board = result(currboard, node.start, node.action, True)
                # if only one jump on board, will always choose that one
                if len(jump) == 1 and currdepth == 0:
                    return v, node
                v2, action2 = min_value(currdepth+1, result_board, a, b)
                # actually minimax don't care the taken step
                if v2 > v:
                    # print(f"max v change from {v} to {v2} in depth {currdepth} with DEPTH_LIMIT {DEPTH_LIMIT}")
                    v, lastnode = v2, node
                    a = max(a, v)
                if v >= b:
                    return v, lastnode  # return early
    else:  # only exists regular = {{"begin": coordinates, "regular": {regular set}}, ...}
        for actiondic in regular:  # actiondic = {"begin": coordinates, "regular": {regular set}}
            for action in actiondic["regular"]:
                result_board = result(board, actiondic["begin"], action, False)
                v2, action2 = min_value(currdepth+1, result_board, a, b)
                # actually minimax don't care the taken step
                if v2 > v:
                    # print(f"max v change from {v} to {v2} in depth {currdepth} with DEPTH_LIMIT {DEPTH_LIMIT}")
                    node = Node(state = board, parent = None, start = actiondic["begin"], action = action)
                    v, lastnode = v2, node
                    a = max(a, v)
                if v >= b:
                    return v, lastnode  # return early
    return v, lastnode


def min_value(currdepth, board, a, b):  # white is min
    # print(f"depth: {currdepth}")
    if terminal(currdepth):
        return evaluation(board), None
    v = float("inf")  # v remember the highest EVAL the player can have

    regular, jump = get_move("w", board)  # all (i,j)'s actions
    if regular == None and jump == None:
        # if for all (i,j), no action, then return LOSE, None, opponent wins
        return WHITE_LOSE, None
    elif jump != None:
        for actiondic in jump:
            for action in actiondic["jump"]:
                node, currboard = consecutive_jump(actiondic["begin"], action, board)
                result_board = result(currboard, node.start, node.action, True)
                if len(jump) == 1 and currdepth == 0:
                    return v, node
                v2, action2 = max_value(currdepth+1, result_board, a, b)
                if v2 < v:
                    # print(f"min v change from {v} to {v2} in depth {currdepth} with DEPTH_LIMIT {DEPTH_LIMIT}")
                    v, lastnode = v2, node
                    b = min(b, v)
                if v <= a:
                    return v, lastnode
    else:
        for actiondic in regular:
            for action in actiondic["regular"]:
                result_board = result(board, actiondic["begin"], action, False)
                v2, action2 = max_value(currdepth+1, result_board, a, b)
                if v2 < v:
                    # print(f"min v change from {v} to {v2} in depth {currdepth} with DEPTH_LIMIT {DEPTH_LIMIT}")
                    node = Node(state = board, parent = None, start = actiondic["begin"], action = action)
                    v, lastnode = v2, node
                    b = min(b, v)
                if v <= a:
                    return v, lastnode
    return v, lastnode


def main():
    information["starting_time"] = time.time()  # record the starting time
    load_chessboard()
    # print(information)

    player = "b" if information["player"] == "BLACK" else "w"
    if information["test"] == "SINGLE":  # similar to depth = 1, but not using any strategy
        action = single_mode(information["board"], player)
    else:  # == "GAME"
        action = alpha_beta_search(information["board"])  # action = [(begin, end), (begin, end), ...] or [(beginning, [step1], 0)] 
    
    if action == None:
        action_str = ["FAIL"]  # if no valid move, print FAIL
    else:
        action_str = str_convert(action)
    solutions = enumerate(action_str, start=1)
    with open("output.txt", "w") as f:
        for i, solution in solutions:
            f.write(solution)
            # avoid the new end line
            if i < len(action_str):
                f.write("\n")

    # endtime = time.time()
    # print(f"endtime: {endtime}")
    # totaltime = endtime - information["starting_time"]
    # print(f"time spent: {totaltime}s")


if __name__ == "__main__":
    main()