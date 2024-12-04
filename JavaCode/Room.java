package JavaCode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * There should only be one of these running as a server and players should connect to it as clients.
 * @author cdgira
 *
 */
public class Room extends Thread
{
	/*
	Server variables
	 */
	private DatagramSocket socket;
	private byte[] buf;


	private ArrayList<Entity> creatures = new ArrayList<Entity>();
	private ArrayList<Entity> players = new ArrayList<Entity>();
	private ArrayList<String> messages = new ArrayList<String>();
	private double spawn_chance = 0.05;
	/**
	 * Kept in milliseconds.
	 */
	private int check_spawn = 1000;

	private boolean run = true;

	private long presentTime;
	private int creatureID = 1;

	public Room(double sc, int cs)
	{
		spawn_chance = sc;
		check_spawn = cs;
	}

	public void addPlayer(Entity player)
	{
		players.add(player);
		messages.add(player.getName()+" has entered the room.");
	}

	private void attackRandomEntity(Entity attacker,ArrayList<Entity> options)
	{
		int index = (int)(Math.random()*options.size());
		Entity target = options.get(index);
		int targetHealth = target.getHealth();
		target.takeDamage(attacker.getDamage());
		messages.add(attacker.getName()+" attacked "+target.getName()+" doing "+(targetHealth - target.getHealth())+" damage.");
		attacker.setLastAttack(presentTime);
		if (target.getHealth() == 0)
		{
			if(target.getPort() == 0){
				options.remove(index);
			} else {
				sendMessageToPlayer(target, "You have died, if you would like to rejoin, enter 'join <name>'.");
				players.remove(target);
			}
			messages.add(target.getName()+" killed.");
		}

	}

	/*
  converts all of the messages from the current turn into one string
  for easier sending to each player
   */
	private void printMessages() throws IOException {
		String message = "";
		while (messages.size() > 0)
		{
			message += "\n" + messages.remove(0);
		}
		for (Entity player : players)
		{
			buf = message.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, player.getAddress(), player.getPort());
			socket.send(packet);
		}
	}

	private void processActions(ArrayList<Entity> entities,ArrayList<Entity> targets)
	{
		for (Entity entity : entities)
		{
			int action = entity.getAction();
			if ((action == 1) && (targets.size() > 0))
			{
				long lastAttack = entity.getLastAttack();
				if ((presentTime - lastAttack) > entity.getAttackRate())
				{
					attackRandomEntity(entity,targets);
				}
			}
			if (action == 2)
			{
				long lastHeal = entity.getLastHeal();
				if ((presentTime - lastHeal) > entity.getHealRate())
				{
					int entityHealth = entity.getHealth();
					entity.heal(presentTime);
					messages.add(entity.getName()+" healed for "+(entity.getHealth()-entityHealth));
				}

			}
		}
	}

	public void run() {
		System.out.println("Room started.");
		long startTime = System.currentTimeMillis();
		long lastSpawnCheck = startTime;

		/*
		Initializes server and waits for join message from client
		 */
		try {
			//creates socket
			socket = new DatagramSocket(4445);


			//waits for join message from client, form: "join <name>"
			buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			String joinMessage = new String(packet.getData(), 0, packet.getLength());
			String playerName = joinMessage.split(" ")[1];

			//creates player and adds them to players list
			Entity new_player = new Entity(playerName, packet.getAddress(), packet.getPort());
			players.add(new_player);
			messages.add(new_player.getName() + " has entered the room.");

			//spawns creature
			Entity creature = new Entity("Creature" + creatureID, 100, 100, 10, 0.6, 1000, 5000);
			creatureID++;
			messages.add(creature.getName() + " has entered the room.");
			creatures.add(creature);
			Iterator iterator = players.iterator();
			while (run) {

				printMessages();


				//checks for 0 players, if there is 0 room listens for new join request, blocking here
				if (players.size() == 0) {
					buf = new byte[256];
					packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					joinMessage = new String(packet.getData(), 0, packet.getLength());
					playerName = joinMessage.split(" ")[1];
					new_player = new Entity(playerName, packet.getAddress(), packet.getPort());
					sendMessageToPlayer(new_player, "Welcome, you are currently the only player in the room.");
					players.add(new_player);
					messages.add(new_player.getName() + " has entered the room.");
				}

				//asks player if they wish to continue, or wait for another player to join
				//if any one player elects to continue, the game will continue
				for (Entity player : players) {
					sendMessageToPlayer(player, "Press enter to continue, to wait for another player do nothing:");
				}
				buf = new byte[256];
				packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				joinMessage = new String(packet.getData(), 0, packet.getLength());
				if (joinMessage.contains("join")) {
					playerName = joinMessage.split(" ")[1];
					new_player = new Entity(playerName, packet.getAddress(), packet.getPort());
					sendMessageToPlayer(new_player, "Welcome, there are currently " + players.size() + " players in the room.");
					players.add(new_player);
					messages.add(new_player.getName() + " has entered the room.");
				}


				//randomly spawns creatures based on spawn chance
				presentTime = System.currentTimeMillis();
				if ((presentTime - lastSpawnCheck) > check_spawn) {
					double check = Math.random();
					if (spawn_chance > check) {
						creature = new Entity("Creature" + creatureID, 100, 100, 10, 0.6, 1000, 5000);
						creatures.add(creature);
						messages.add(creature.getName() + " has entered the room.");
						creatureID++;
					}
					lastSpawnCheck = presentTime;
				}

				//get player moves
				for (Entity player : players) {
					try {
						//prompts player for move
						System.out.println("Prompting player: " + player.getName());
						buf = "enter move (defend 0 | attack 1 | heal 2) : ".getBytes();
						packet = new DatagramPacket(buf, buf.length, player.getAddress(), player.getPort());
						socket.send(packet);

						//receives move from player
						socket.receive(packet);
						String moveData = new String(packet.getData(), 0, packet.getLength());
						System.out.println("Received move from " + player.getName() + ": " + moveData);
						int move = Integer.parseInt(moveData.split(" ")[0]);
						player.setAction(move);

					} catch (IOException e) {
						System.out.println("Error while handling player " + player.getName() + ": " + e.getMessage());
					}
				}

				//processes player and creature actions
				System.out.println("Processing actions...");
				processActions(players, creatures);
				processActions(creatures, players);
				updateCreatureAction();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		socket.close();
	}

	//sends a message directly to a player
	private void sendMessageToPlayer(Entity player, String message) {
		try {
			byte[] buf = message.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, player.getAddress(), player.getPort());
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("Error while sending message to player " + player.getName() + ": " + e.getMessage());
		}
	}

	public void setRun(boolean value)
	{
		run = value;
	}

	private void updateCreatureAction()
	{
		for (Entity creature : creatures)
		{
			long lastAttack = creature.getLastAttack();
			if ((presentTime - lastAttack) > creature.getAttackRate())
				creature.setAction(1); // Can attack so do that.
			else
			{
				long lastHeal = creature.getLastHeal();
				if ((presentTime - lastHeal) > creature.getHealRate())
					creature.setAction(2); // If can't attack, but can heal do that.
				else
					creature.setAction(0); // If can't attack or heal just defend.
			}
		}
	}

	public static void main(String[] args)
	{
		Room room = new Room(0.5,1000);
		room.start();
	}
}