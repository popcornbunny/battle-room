# Client
import socket
import BattleRoom

def runClient(host, port):
    print("Connecting to the Battle Room...")
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((host, port))
        print("Connected to server at " + host + " on port " + str(port))
        msg = s.recv(1024).decode()

        # Game

        s.close()

def main():
    serverHost = input("Enter the IP address you want to connect to: ")
    serverPort = int(input("Enter the port number: "))

    runClient(serverHost, serverPort)

if __name__ == "__main__":
    main()