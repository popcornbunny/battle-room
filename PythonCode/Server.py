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

def playGame():
    run = True
    msg = ""
    clearLog()

    print("Room started.")
    room = BattleRoom.Room(0.05, 1000)
    startTime = BattleRoom.currentTimeMillis()
    lastSpawnCheck = startTime
    port = int(input("Please enter the port number: "))
    # Socket creation
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as ss:
        ss.bind((host, port))
        print("Server started on IP", socket.gethostbyname(socket.gethostname()), "with port", port)

    # Receive join message from client
        msg, addr = ss.recvfrom(1024)
        print("Received join message from address: ", addr)
    # Welcome new player
        num = len(room._Room__players)
        msg = ("Welcome to the Battle Room!\n"
               f"You are Player {num + 1}.\n"
               "The rules are simple: You can attack, heal or defend.\n"
               "To heal, type '2', to attack, type '1', to defend, type '0'.\n"
               "If you wish to stop playing, enter 'exit' into the move prompt.\n"
               "First, let's create your character.")
        ss.sendto(msg.encode(), addr)

    # Receive player name from client
        msg = "Please enter your name: "
        ss.sendto(msg.encode(), addr)
        msg, addr = ss.recvfrom(1024)
        p = ss.getsockname()[1]

    # Starts room and adds player

        player = BattleRoom.Entity(msg.decode(), addr, p)
        room.addPlayer(player)
        print(msg.decode(), "has joined the game.")

def main():
    playGame()

if __name__ == "__main__":
    main()
