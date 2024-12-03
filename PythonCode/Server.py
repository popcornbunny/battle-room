# Server

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
    p = sock.getsockname()[1]

    # Adds player to room
    player = BattleRoom.Entity(msg.decode(), addr, p)
    print(msg.decode(), "has joined the game.")
    logMove(msg.decode() + "joined the game. (IP/port: " + addr + ")")
    return player

# The main code for playing the game
def playGame(room):
    # Spawns creatures and fights
    BattleRoom.runGame(room)




def run():
    numPlayers = 0
    clearLog()
    # Starts the room
    port = int(input("Please enter the port number: "))
    room = BattleRoom.createRoom(0.05, 1000)
    #BattleRoom.runGame(room)
    logMove("Room started.")
    # Socket creation
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as ss:
        ss.bind((host, port))
        print("Server started on IP", socket.gethostbyname(socket.gethostname()), "with port", port)
        playGame(room)
        while True:
            msg, addr = ss.recvfrom(1024)
            if msg.decode() == "join":
                numPlayers += 1
                player = playerJoin(ss, addr, numPlayers)
                room.addPlayer(player)

def main():
    run()

if __name__ == "__main__":
    main()
