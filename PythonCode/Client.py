# Client
import socket
import BattleRoom

logFile = "log.txt"
def clearLog():
    global logFile
    with open(logFile, "w") as log:
        log.write("Battle Room Client Log\n\n")  # clears the log file
        log.close()

def runClient(host, port):
    print("Connecting to the Battle Room...")
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
        s.sendto("join".encode(), (host, port))
        print("Connected to server at " + host + " on port " + str(port))
        msg = s.recvfrom(1024)
        msg = msg[0].decode()
        while True:
            action = -1
            msgCount = 0
            if msg is not None:
                msgCount += 1
                print(msg)
            '''else:
                print("Goodbye!")
                s.close()
                break'''
            if "enter move: " in msg:  # Send move
                move = input()
                if move == "exit":
                    print("Exiting game...")
                    s.sendto(move.encode(), (host, port))
                    s.close()
                    break
                action = move + " " + str(msgCount)
                s.sendto(action.encode(), (host, port))
                print("Sending action:", action)
            if "Please enter your name: " in msg:  # Send after confirmation
                s.sendto(input().encode(), (host, port))

            msg = s.recvfrom(1024)
            msg = msg[0].decode()

        s.close()
        print("\nGame over. Closing connection.")

def main():
    serverHost = input("Enter the IP address you want to connect to: ")
    serverPort = int(input("Enter the port number: "))

    runClient(serverHost, serverPort)

if __name__ == "__main__":
    main()
