###################################################
# Author: Katelyn Hanft
# Client side of the Battle Room game
###################################################

import socket

# Client side of the Battle Room game
def runClient(host, port):
    print("Connecting to the Battle Room...")
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
        s.sendto("join".encode(), (host, port))  # Sends join to connect
        print("Connected to server at " + host + " on port " + str(port))
        msg = s.recvfrom(1024)  # Waits for welcome message
        msg = msg[0].decode()
        msgCount = 0
        while True:
            if msg is not None:
                print(msg)
            if "Enter your move " in msg:  # Send move
                msgCount += 1
                move = input()
                if move == "exit":  # Exit game
                    print("Exiting game.")
                    s.sendto("exit".encode(), (host, port))
                    break
                action = move
                s.sendto((action + " " + str(msgCount)).encode(), (host, port))
                print("Sending action:", move, "(" + str(msgCount) + ")")
            if "Please enter your name: " in msg:  # Send after confirmation
                s.sendto(input().encode(), (host, port))

            msg, addr = s.recvfrom(1024)  # Get another "Make a move" message
            msg = msg.decode()

        s.close()
        print("\nGame over. Closing connection.")

def main():
    serverHost = input("Enter the IP address you want to connect to: ")
    serverPort = int(input("Enter the port number: "))

    runClient(serverHost, serverPort)

if __name__ == "__main__":
    main()
