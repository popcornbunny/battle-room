package JavaCode;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class Entity {
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
	 * 0 : Defend (take half damage)
	 * 1 : Attack (if player attack random creature, if creature attack random player)
	 * (only once per attack_rate)
	 * 2 : Heal (5 health, only once per heal_rate)
	 */
	private int action = -1;  // Ideally would have an enum for this

	public Entity() {
		// Just use the default values.
	}

	public Entity(String n, int h, int mh, int d, double hc, int ar, int hr) {
		this.port = 0;
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
	public Entity(String n, InetAddress address, int port) {
		name = n;
		this.address = address;
		this.port = port;
		health = 20;
		max_health = 200;
		damage = 10;
		hit_chance = 0.6;
		attack_rate = 1000;
		heal_rate = 5000;
		start_time = System.currentTimeMillis();
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public int getAction() {
		return action;
	}

	public int getAttackRate() {
		return attack_rate;
	}

	public int getDamage() {
		return damage;
	}

	public int getHealRate() {
		return heal_rate;
	}

	public int getHealth() {
		return health;
	}

	public long getLastAttack() {
		return last_attack;
	}

	public long getLastHeal() {
		return last_heal;
	}

	public String getName() {
		return name;
	}

	public void heal(long time) {
		health += 5;
		if (health > max_health)
			health = max_health;
		last_heal = time;
	}

	/**
	 * Updates the value in action, if outside the allowed actions sets it to -1 (do nothing).
	 *
	 * @param value
	 */
	public void setAction(int value) {
		action = value;
		if ((action < -1) || (action > 2)) {
			action = -1;
		}
	}

	public void setLastAttack(long time) {
		last_attack = time;
	}


	public void takeDamage(int dmg) {
		if (action != 0)
			health -= dmg;
		else  // We are defending.
			health -= dmg / 2;
		if (health < 0)
			health = 0;
	}

	public static void main(String[] args) {
			try {
				DatagramSocket socket = new DatagramSocket();
				InetAddress address = InetAddress.getByName("localhost");
				int serverPort = 4445;
				Scanner scanner = new Scanner(System.in);

				System.out.println("Welcome to Battle Room!");
				System.out.println("Once you join, creatures will begin spawning randomly");
				System.out.println("After each turn, you can either wait and allow another player to join,");
				System.out.println("or you can type 'yes' when prompted to continue to the next turn");
				System.out.println("To attack, type 1, to defend, type 0, to heal, type 2, to exit, type 'exit' ");
				System.out.println("Good luck!");
				System.out.print("Enter your name to join: ");
				String name = "join " + scanner.nextLine();

				// Send the player's name to the server
				byte[] buf = name.getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, address, serverPort);
				socket.send(packet);

				//receive loop
				new Thread(() -> {
					while (true) {
						try {
							byte[] receiveBuf = new byte[256];
							DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
							socket.receive(receivePacket);
							String received = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
							System.out.println(received);
						} catch (IOException e) {
							System.out.println("Error receiving server message: " + e.getMessage());
						}
					}
				}).start();

				//send loop
				while (true) {
					// Send the player's move to the server
					String move = scanner.nextLine();
					buf = move.getBytes();
					packet = new DatagramPacket(buf, buf.length, address, serverPort);
					socket.send(packet);

					if (move.equalsIgnoreCase("exit")) {
						System.out.println("Exiting game.");
						break;
					}
				}
				socket.close();
			} catch (IOException e) {
				System.out.println("Error receiving server message: " + e.getMessage());
			}
	}
}

