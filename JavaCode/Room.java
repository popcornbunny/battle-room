package JavaCode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

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
	private byte[] buf = new byte[1024];


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
			options.remove(index);
			messages.add(target.getName()+" killed.");
		}
			
	}

  //will have to rework to print to clients
	private void printMessages() throws IOException {
		while (messages.size() > 0)
		{
			for (Entity player : players)
			{
				buf = messages.get(0).getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, player.getAddress(), player.getPort());
				socket.send(packet);
				messages.remove(0);
			}
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

		try {
			//creates socket
			socket = new DatagramSocket(4445);

			//receives "join" message from client, does nothing with it
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			System.out.println("Received join message from client.");
			InetAddress address = packet.getAddress();
			int port = packet.getPort();

			//sends confirmation message to client
			String message = "Welcome to the Battle Room! \nYou are player " + (players.size()+1) + ".\n" +
				"The rules are simple: You can attack, heal or defend. \nto heal, type '2', to attack, type" +
				" '1', to defend, type '0'. \nFirst, lets create your character.";
			buf = message.getBytes();
			packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(packet);

			//prompts entity creation
			message = "Please enter your name: ";
			buf = message.getBytes();
			packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(packet);

			//wait for response
			socket.receive(packet);
			String name = new String(packet.getData(), 0, packet.getLength());

			//create player entity, health is increased for player
			address = packet.getAddress();
			port = packet.getPort();
			Entity player = new Entity(name, address, port);
			addPlayer(player);
			System.out.println(player.getName() + " has entered the room.");

		} catch (IOException e) {
			e.printStackTrace();
		}

		messages.add("The battle has begun!\nWait for creatures to spawn.");
		while (run) {

			//main loop

			try {
				printMessages();
			} catch (IOException e) {
				e.printStackTrace();
			}
			presentTime = System.currentTimeMillis();
			if ((presentTime - lastSpawnCheck) > check_spawn) {
				double check = Math.random();
				if (spawn_chance > check) {
					Entity creature = new Entity("Creature" + creatureID, 100, 100, 10, 0.6, 1000, 5000);
					creatures.add(creature);
					messages.add(creature.getName() + " has entered the room.");
					creatureID++;
				}
				lastSpawnCheck = presentTime;
			}

			//get player moves

			processActions(players, creatures);
			processActions(creatures, players);

			updateCreatureAction();

			try {
				printMessages();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket.close();
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
		Room room = new Room(0.05,1000);
		room.start();
	}

}
