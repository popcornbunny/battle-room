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
	private void printMessages()
	{
		while (messages.size() > 0)
		{
			System.out.println(messages.get(0));
			messages.remove(0);
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

	public void run()
	{
		System.out.println("Room started.");
		long startTime = System.currentTimeMillis();
		long lastSpawnCheck = startTime;

		// Create the socket for the server.
		try {
			socket = new DatagramSocket(4445);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		while (run)
		{
			try
			{
				//receives "join" message from client, does nothing with it
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				System.out.println("Received join message from client.");
				InetAddress address = packet.getAddress();
				int port = packet.getPort();

				//sends confirmation message to client
				String message = "Welcome to the Battle Room!";
				buf = message.getBytes();
				packet = new DatagramPacket(buf, buf.length, address, port);
				socket.send(packet);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			presentTime = System.currentTimeMillis();
			if ((presentTime - lastSpawnCheck) > check_spawn)
			{
				double check = Math.random();
				if (spawn_chance > check)
				{
					Entity creature = new Entity("Creature"+creatureID,100,100,10,0.6,1000,5000);
					creatures.add(creature);
					messages.add(creature.getName()+" has entered the room.");
					creatureID++;
				}
				lastSpawnCheck = presentTime;
			}

			processActions(players,creatures);
			processActions(creatures,players);

			updateCreatureAction();
			printMessages();
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
