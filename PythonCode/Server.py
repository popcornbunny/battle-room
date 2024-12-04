##############################################
# Author: Katelyn Hanft
# Server side of the Battle Room game
##############################################

import threading
import socket
import BattleRoom

host = socket.gethostbyname(socket.gethostname())

logFile = "log.txt"
def clearLog():
    global logFile
    with open(logFile, "w") as log:
        log.write("Battle Room Server Log\n\n")  # clears the log file
        log.close()

def logMove(entry):
    #print(entry)
    with open(logFile, "a") as log:
        log.write(entry + "\n")
        log.close()

# Code that allows players to join
def playerJoin(sock, addr, num):
    print("Received join message from address: ", addr)
    # Welcome new player
    msg = ("Welcome to the Battle Room!\n"
           f"You are Player {num}.\n"
           "The rules are simple: You can attack, heal or defend.\n"
           "To heal, type '2', to attack, type '1', to defend, type '0'.\n"
           "If you wish to stop playing, enter 'exit' into the move prompt.\n"
           "First, let's create your character.")
    sock.sendto(msg.encode(), addr)

    # Receive player name from client
    msg = "Please enter your name: "
    sock.sendto(msg.encode(), addr)
    msg, addr = sock.recvfrom(1024)
    #p = sock.getsockname()[1]

    # Adds player to room
    player = BattleRoom.Entity(msg.decode(), addr[0], addr[1])
    print(msg.decode(), "has joined the game.")
    logMove(msg.decode() + " joined the game. (IP/port: " + str(addr) + ")")
    return player


def run():
    numPlayers = 0
    clearLog()
    # Starts the room
    port = int(input("Please enter the port number: "))
    room = BattleRoom.createRoom(0.5, 1000)

    logMove("Room started.")
    # Socket creation
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as ss:
        ss.bind((host, port))
        print("Server started on IP", socket.gethostbyname(socket.gethostname()), "with port", port)
        thread = threading.Thread(target=BattleRoom.runGame, args=(room,), daemon=True)
        thread.start()
        # waits for first client to join
        msg, addr = ss.recvfrom(1024)
        while True:
            if msg is not None:
                if msg.decode() == "join":  # New client
                    numPlayers += 1
                    player = playerJoin(ss, addr, numPlayers)
                    room.addPlayer(player)
                elif msg.decode() == "exit":  # Existing client leaves
                    print(player.getName() + " has left the game.")
                    logMove(player.getName() + " has left the game." + " (IP/port: " + str(addr) + ")")
                    room._Room__players.remove(player)
                    numPlayers -= 1
                    msg = None
                    continue
                for player in room._Room__players:  # Loop for each player
                    addr = (player.getAddress(), player.getPort())
                    ss.sendto("Enter your move (0: Defend, 1: Attack, 2: Heal): ".encode(), addr)
                    msg, addr = ss.recvfrom(1024)
                    move = msg.decode().strip()
                    if move[0] in ["0", "1", "2"]:
                        player.setAction(int(move[0]))
                        BattleRoom.Room._Room__messages.append(move[2] + ": " + player.getName() + " chose to " + player.getActionName())
                        logMove(player.getName() + " chose to " + player.getActionName() + " (IP/port From: " + str(addr) + "), " + "(IP/port To: " + str(host) + ", " + str(port) + ");" + " Message number: " + move[2])


def main():
    run()

if __name__ == "__main__":
    main()
