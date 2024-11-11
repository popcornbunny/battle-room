# Programming Assignment 2 – UDP Battle Room

You will be working in groups of three or a pair depending on number of students in the course.  For this project you will be creating a battle room game that can be played over a network using UPD as the transport layer communication protocol.  Your group will develop potentially three different versions of the game: Java, C, and Python (groups of two will only have two out of the three versions).  However, each version will be able to connect and play with any of the other versions in your group. 

 
As this class is supposed to focus on network programming the core of the game has already been written in Java, C, or Python and posted to D2L.  You must use the core provided, if you get stuck on some aspect of it, or some part of it does not appear to be working correctly please contact me.  I did do testing of each version and even wrote some JUnit tests for the Java version, but that does not mean I missed something.

 

## Details

This battle room game is based on old style MUD games that were the precursors for games like World of Warcraft.  These purely text-based games allowed users to connect in and control a player in a world to fight creatures.  In this simple version there is only one room that can be setup to randomly spawn enemies.  Players can connect into this room with their hero to then fight and defeat the enemies.  Both the heroes and enemies have three possible actions each round: attack, defend, and heal.  Both can only attack and heal at a certain rate (so you can’t just spam heal or attack).

 

### The enemy are setup with a simple state system as follows:

If they can attack, they will attack a random player
If they can’t attack, but can heal they will
If they can’t attack or heal they will defend.
 

For this you will have a server application in Java, C, or Python.  The server will act as the battle room that players will connect into.  For this room you will decide how often a creature can spawn and how strong the creature is for that room.  Each room will allow any number of players to connect to then fight the creatures that appear.  As such, you will also write a client that will connect to the server.  The client will allow the player to determine the stats for their hero and send those to the server to be added to the room.  The client will then let the player send commands and inform the player what is going on in the room.

 

### Each hero and enemy have the following information:

Name: A string that is used to identify the hero or enemy in the messages.  Should be unique.
Present Health: How much health the hero or enemy has.  When a hero or enemy gets to or below 0 they are dead and cannot do anything else.
Max Health: The max amount of health a hero or enemy has.  They should be able to heal past this (code should enforce this).
Damage: How much damage a hero or enemy will do when they attack.
Hit Chance: Does nothing, pretty sure was supposed to determine how likely an attack was to succeed, but didn’t put in the code for this.  Feel free to remove or use as was intended.
Attack Rate: In terms of milliseconds, how often a hero or enemy can attack someone.
Heal Rate: In terms of milliseconds, how often a hero or enemy can heal damage.
Action: Do Nothing (-1), Defend (0), Attack (1), or Heal (2)
 

Each client should provide an interface that allows a player to connect to a Room that is active using UDP.  How the information for a player’s hero is determined can be hard coded, user input, or some mixture in between.  Both the server and client should display useful messages based on the information being sent on what is going on in the room. Note, the Room already has a built-in system for recording and printing out what occurs in the room, so start with that.

 

Both the server and clients should save a log file of all messages received or sent.  Information in the message should be: Action Taken or Reported, IP of where the message is from, and IP of where the message is sent.  Again, the Room already has a built-in system for recording and printing out what occurs in the room, that would be a good place to look for guidance on the log system.

 

## UDP Loss

Remember that UDP does not ensure a message is received or received in the proper order.  To track this each client should also number the messages sent starting at 1 and increasing with each message.  The server should then track from each client which a message skips or arrives out of order. It is up to your group how to tell which client sent a message.  As two clients could be on the same computer using IP may not work.  A simple way is at client startup set an ID by user input and just make sure you put in a unique ID for each client. Now because of how close the connections will likely be I expect no loss or ordering issues for your UDP messages.  If you want to try and create such an issue, one option is have a computer be on Ship’s Wifi and another be a lab machine.  Additionally, you could have one person use cellular data to connect if you have a device that can run the application and use the cellular network.  After that just trying to connect to a lab machine from an off campus computer.

 

Important Note: It is your job to inform me (via email, office hours, etc…) if your partner(s) is not working with you on the project by not communicating, not showing up to work on it with you, or not providing any code when expected.  Unless I am informed, I will automatically assume whatever work was turned in was done as a part of a collaborative effort.

 

Turning-In: A hard copy of your code to the correct Assignments folder of D2L (each group member will only turn in a copy of their server and client).  In the comments for each file where you should put the author, be sure and note who worked on it.  That way if the whole group worked on all the pieces, I can compute the grade accordingly.  Additionally, each group demo their code on the Thursday in class after the due date.

 

## Extra Resources

I have links/resources listed below for threading in Java (normally covered in SWE 200) and C (normally covered in CMPE 320).

Java: http://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html
C: (http://www.thegeekstuff.com/2012/04/create-threads-in-linux/
Python: https://realpython.com/python-sockets/
 

## IMPORTANT NOTES

In C threads are much more operating system dependent.
Python doesn't do threads in the way C and Java do (least the older versions didn't), you'll need to rely on what is built into the python sockets.
Reminder that char in C is 8 bits, a char in Java is 16 bits (but a byte is only 8 bits), a char in python can vary in size.
Lastly, remember this is UDP, so completely possible a message could get lost.

 Due November 20 at 10:00 PM
