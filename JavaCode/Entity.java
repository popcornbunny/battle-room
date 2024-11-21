package JavaCode;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Entity
{
	//player specific address and port varibales
	private InetAddress address;
	private int port;

	private String name = "None";
	private int health = 100;
	private int max_health = 100;
	private int damage = 10;
	private double hit_chance = 0.6;
	/**
	 * Kept in terms of milliseconds.
	 */
	private int attack_rate = 1000; 
	private int heal_rate = 5000;
	
	
	private long start_time;
	private long last_attack = 0;
	private long last_heal = 0;
	/**
	 * -1 : Do Nothing
	 *  0 : Defend (take half damage)
	 *  1 : Attack (if player attack random creature, if creature attack random player)
	 *             (only once per attack_rate)
	 *  2 : Heal (5 health, only once per heal_rate)
	 */
	private int action = -1;  // Ideally would have an enum for this
	
	public Entity()
	{
		// Just use the default values.
	}
	public Entity(String n, int h, int mh, int d, double hc, int ar, int hr)
	{
		name = n;
		health = h;
		max_health = mh;
		damage = d;
		hit_chance = hc;
		attack_rate = ar;
		heal_rate = hr;
		start_time = System.currentTimeMillis();
	}

	//player specific constructor
	public Entity(String n, InetAddress address, int port)
	{
		name = n;
		this.address = address;
		this.port = port;
		health = 200;
		max_health = 200;
		damage = 10;
		hit_chance = 0.6;
		attack_rate = 1000;
		heal_rate = 5000;
		start_time = System.currentTimeMillis();
	}

	public InetAddress getAddress(){ return address; }

	public int getPort(){ return port; }
	
	public int getAction()
	{
		return action;
	}
	
	public int getAttackRate()
	{
		return attack_rate;
	}
	
	public int getDamage()
	{
		return damage;
	}
	
	public int getHealRate()
	{
		return heal_rate;
	}
	
	public int getHealth()
	{
		return health;
	}
	
	public long getLastAttack()
	{
		return last_attack;
	}
	
	public long getLastHeal()
	{
		return last_heal;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void heal(long time)
	{
		health += 5;
		if (health > max_health)
			health = max_health;
		last_heal = time;
	}
	
	/**
	 * Updates the value in action, if outside the allowed actions sets it to -1 (do nothing).
	 * @param value
	 */
	public void setAction(int value)
	{
		action = value;
		if ((action < -1) || (action > 2))
		{
			action = -1;
		}
	}
	
	public void setLastAttack(long time)
	{
		last_attack = time;
	}
	
	
	public void takeDamage(int dmg)
	{
		if (action != 0)
		    health -= dmg;
		else  // We are defending.
			health -= dmg/2;
		if (health < 0)
			health = 0;
	}

	public static void main(String[] args) throws IOException {

		// Create a socket to send data to the server.
		DatagramSocket socket = new DatagramSocket();
		InetAddress address = InetAddress.getByName("localhost");;
		byte[] buf;

		//sends "join" message to the server
		String message = "join";
		buf = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
		socket.send(packet);

		//receives confirmation message from server
		buf = new byte[1024];
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		String received = new String(packet.getData(), 0, packet.getLength());
		System.out.println(received);

		//receievs name prompt from the server
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		received = new String(packet.getData(), 0, packet.getLength());
		System.out.println(received);

		//sends name to the server
		Scanner scanner = new Scanner(System.in);
		String name = scanner.nextLine();
		buf = name.getBytes();
		packet = new DatagramPacket(buf, buf.length, address, 4445);
		socket.send(packet);

		//receives entity creation message from server
		buf = new byte[256];
		packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		received = new String(packet.getData(), 0, packet.getLength());
		System.out.println(received);

		//main game loop
		String action = "";
		int msg_count = 0;
		while(!(action.equals("quit")))
		{
			//receives game state from server
			buf = new byte[256];
			packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			received = new String(packet.getData(), 0, packet.getLength());
			System.out.println(received);

			//sends action to server
			action = scanner.nextLine();
			buf = action.getBytes();
			packet = new DatagramPacket(buf, buf.length, address, 4445);
			socket.send(packet);
		}
	}
}
