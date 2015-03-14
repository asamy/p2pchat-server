package server;

import gui.GuiController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLSocket;

import server.db.Account;
import server.db.AccountDAO;

/**
 * All packet data represent like: [type]message
 * 
 * login , register, : [10]kfir 123123 addfriend : [55]kfir 123123 fersht
 * broadcast online friends : [66]kfir fersht lala logout : [77] RequestIP :
 * [524]kfir 123123 fersht RequestIPsuccess: [525]fersht 192.168.2.2 10115
 * 
 * @author Kfir fersht
 */
public class ClientHandler implements Runnable {


	private static Map<String, ClientHandler> onlineAccounts = new HashMap<>();
	private final static Object lock = new Object();

	private boolean running = true;
	private SSLSocket socket;
	private BufferedReader input;
	private PrintStream output;
	private Message message;
	private Account account;
	private int port;
	private String ip;
	private volatile boolean logged;
	private volatile int bruteForce = 0;
	private GuiController controller;
	private AccountDAO dao;
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

	public ClientHandler(SSLSocket socket, GuiController primeryController) {
		this.controller = primeryController;
		dao = new AccountDAO(controller);
		this.socket = socket;
		Calendar cal = Calendar.getInstance();
		controller.appendTextAreaConnections("Ip: " + socket.getInetAddress().getHostAddress() + " Connected   "  + dateFormat.format(cal.getTime()));
		controller.addConnection(socket.getInetAddress().getHostAddress());
		this.message = new Message(controller);
		this.account = new Account();
		try {
			this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.output = new PrintStream(socket.getOutputStream(), true, "UTF-8");
		} catch (IOException e) {
			controller.exception(e);
		}

		Runnable bruteForcePrevent = () -> {
			while (!socket.isClosed()) {
				if (bruteForce > 0)
					bruteForce--;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					controller.exception(e);
				}
			}
		};
		new Thread(bruteForcePrevent).start();

	}

	public void run() {
		String string = null;

		while (running && !socket.isClosed()) {

			if ((string = Util.readBytes(input)) == null)
				break;

			controller.appendTextAreaPacketRecieved(string);

			if (!message.decodeMessage(string))
				break;

			switch (message.getType()) {
			case Constants.REGISTER:
				register(message.getMessage());
				break;
			case Constants.ADDFRIEND:
				addFriend(message.getMessage());
				break;
			case Constants.DELETEFRIEND:
				deleteFriend(message.getMessage());
				break;
			case Constants.LOGIN:
				login(message.getMessage());
				break;
			case Constants.LOGOUT:
				logout(message.getMessage());
				break;
			case Constants.SEARCH:
				search(message.getMessage());
				break;
			case Constants.REQUESTUSERIP:
				requestIp(message.getMessage());
				break;
			case Constants.UPDATEPORT:
				updatePort(message.getMessage());
				break;
			case Constants.ADDBLOCK:
				addBlock(message.getMessage());
				break;
			case Constants.REMOVEBLOCK:
				removeBlock(message.getMessage());
				break;
			case Constants.BLOCKLIST:
				blockList(message.getMessage());
				break;
			case Constants.REMOVEREQUEST:
				removeRequest(message.getMessage());
				break;
			case Constants.FRIENDSLIST:
				broadcastFriends();
				break;
			case Constants.FRIENDREQUEST:
				broadcastFriendRequests();
				break;
			case Constants.WHONLINE:
				broadcastOnlineFriends();
				break;
			default:
				System.err.println("Unkown type");
				break;
			}

			if (++bruteForce > 5) {
				controller.appendTextAreaConnections("Suspicious brute force - " + socket.getInetAddress().getHostAddress());
				running = false;
			}

		}
		
		
		// Testing
		if (account.getUser() != null)
			controller.removeLogged(account.getUser() + "  " + socket.getInetAddress().getHostAddress());
		Calendar cal = Calendar.getInstance();
		controller.appendTextAreaConnections("Ip: " + socket.getInetAddress().getHostAddress() + " Disconnected   "  + dateFormat.format(cal.getTime()));
		controller.removeConnection(socket.getInetAddress().getHostAddress());
			
		close();
			

	}

	private void addBlock(String message) {
		String[] strings;
		if ((strings = checkPacket(message)) == null || strings.length != 3)
			return;

		dao.addBlock(strings[0], strings[2]);

	}

	private void removeBlock(String message) {
		String[] strings;
		if ((strings = checkPacket(message)) == null || strings.length != 3)
			return;

		dao.removeBlock(strings[0], strings[2]);

	}

	private void blockList(String message) {
		String[] strings;
		if ((strings = checkPacket(message)) == null || strings.length != 2)
			return;

		Set<String> blocked = dao.getBlockedList(strings[0]);

		if (blocked != null) {
			StringBuilder builder = new StringBuilder();
			for (String string : blocked) {
				builder.append(string + " ");
			}

			this.message.setMessage(Constants.BLOCKLIST, builder.toString().trim());
			Util.sendBytes(this.message.toString(), output);
			controller.appendTextAreaPacketSent(this.message.toString());
		}

	}

	private void removeRequest(String message) {
		String[] strings;
		if ((strings = checkPacket(message)) == null || strings.length != 3)
			return;

		dao.removeRequest(strings[0], strings[2]);

	}

	private void updatePort(String message) {
		String[] strings;
		if ((strings = checkPacket(message)) == null || strings.length != 4)
			return;

		try {
			this.ip = strings[2];
			this.port = Integer.parseInt(strings[3]);
		} catch (NumberFormatException e) {
			controller.exception(e);
		}

	}

	private void requestIp(String message) {

		String[] strings;
		if ((strings = checkPacket(message)) == null || strings.length != 3)
			return;

		synchronized (lock) {

			Set<String> friends = dao.getFriends(strings[2]);
			Set<String> blocked = dao.getBlockedList(strings[2]);

			if (friends.contains(strings[0]) && onlineAccounts.containsKey(strings[2])
					&& onlineAccounts.get(strings[2]).port != 0 && !blocked.contains(strings[0])) {

				String ip = onlineAccounts.get(strings[2]).ip;
				int port = onlineAccounts.get(strings[2]).port;

				this.message.setMessage(Constants.REQUESTIPACCEPTED, strings[2] + " " + ip + " " + port);

			} else {
				this.message.setType(Constants.REQUESTIPFAILED);
			}

		}
		Util.sendBytes(this.message.toString(), output);
		controller.appendTextAreaPacketSent(this.message.toString());
	}

	private void deleteFriend(String message) {
		String[] strings;
		if ((strings = checkPacket(message)) == null || strings.length != 3)
			return;

		dao.removeFriend(strings[0], strings[2]);
	}

	private void search(String message) {
		String[] strings;
		if ((strings = checkPacket(message)) == null || strings.length != 3)
			return;

		if (dao.getAccount(strings[2]) != null)
			this.message.setType(Constants.FOUND);
		else
			this.message.setType(Constants.NOTFOUND);
		Util.sendBytes(this.message.toString(), output);
		controller.appendTextAreaPacketSent(this.message.toString());

	}

	private void login(String message) {
		String[] strings;
		synchronized (lock) {

			if (((strings = checkPacket(message)) != null))
				if (!onlineAccounts.containsKey(strings[0]))
					this.message.setType(Constants.LOGINSUCCESS);
				else
					this.message.setType(Constants.LOGINFAILED);
			else
				this.message.setType(Constants.LOGINFAILED);

			if (Util.sendBytes(this.message.toString(), output)) {
				if (this.message.getType() == Constants.LOGINSUCCESS) {
					logged = true;
					onlineAccounts.put(account.getUser(), this);
					controller.addLogged(strings[0] + "  " + socket.getInetAddress().getHostAddress());
				}
			}
			
			controller.appendTextAreaPacketSent(this.message.toString());
		}

	}

	private void logout(String message) {

		synchronized (lock) {
			if (logged) {
				onlineAccounts.remove(account.getUser());
				controller.removeLogged(account.getUser() + "  " + socket.getInetAddress().getHostAddress());
				logged = false;
			}

		}
	}

	public void close() {
		synchronized (lock) {
			if (logged) {
				if (account.getUser() != null)
					onlineAccounts.remove(account.getUser());
				this.logged = false;
			}
		}

		try {
			if (this.output != null)
				this.output.close();

			if (this.input != null)
				this.input.close();

			if (this.socket != null)
				this.socket.close();
		} catch (IOException e) {
			controller.exception(e);
		}

		this.running = false;
	}

	private void register(String message) {

		String[] strings = message.split(" ");

		if (strings.length != 3)
			return;

		if (!(strings[0] + strings[1]).matches("^[a-zA-Z0-9]+([ ][a-zA-Z0-9]+)?$"))
			return;

		if (!strings[2].matches("[A-Za-z0-9._%-]+@[A-Za-z0-9._-]+\\.[A-Za-z]{2,4}"))
			return;

		if (!dao.createAccount(strings[0], strings[1], strings[2]))
			this.message.setType(Constants.REGISTERFAILED);
		else
			this.message.setType(Constants.REGISTERSUCCESS);

		Util.sendBytes(this.message.toString(), output);
		controller.appendTextAreaPacketSent(this.message.toString());
	}

	private void addFriend(String message) {
		String[] strings;
		if ((strings = checkPacket(message)) == null || strings.length != 3)
			return;

		if (dao.addFriend(strings[0], strings[2]))
			this.message.setType(Constants.ADDFRIENDOK);
		else
			this.message.setType(Constants.FRIENDALLREADY);

		Util.sendBytes(this.message.toString(), output);
		controller.appendTextAreaPacketSent(this.message.toString());

		if (this.message.getType() == Constants.ADDFRIENDOK) {
			Set<String> blocked = dao.getBlockedList(strings[2]);
			if (!blocked.contains(strings[0])) {
				dao.addRequest(strings[2], strings[0]);
			}
		}

		dao.removeRequest(strings[0], strings[2]);
	}

	private String[] checkPacket(String message) {
		if (message == null)
			return null;
		String[] strings = message.split(" ");

		if (strings.length > 1 && authentication(strings[0], strings[1]))
			return strings;
		else
			return null;

	}

	private boolean authentication(String user, String password) {

		if (!(user + password).matches("^[a-zA-Z0-9]+([ ][a-zA-Z0-9]+)?$"))
			return false;

		if (dao.isValidAccount(user, password)) {
			account.setUser(user);
			account.setPassword(password);
			return true;
		} else
			return false;

	}

	private void broadcastFriends() {
		if (logged && account.getUser() != null) {

			StringBuilder builder = new StringBuilder();
			Set<String> friends = dao.getFriends(account.getUser());
			Iterator<String> items = friends.iterator();

			builder.append("[" + Constants.FRIENDSLIST + "]");

			while (items.hasNext()) {
				String friend = items.next();
				builder.append(friend + " ");
			}

			Util.sendBytes(builder.toString().trim(), output);
			controller.appendTextAreaPacketSent(this.message.toString());

		}
	}

	private void broadcastFriendRequests() {

		if (logged) {
			Set<String> requests = dao.getRequestsList(account.getUser());

			Message message = new Message(controller);
			Object[] strings = requests.toArray();
			StringBuilder builder = new StringBuilder();

			for (int i = 0; i < strings.length; i++) {
				builder.append(strings[i] + " ");
			}

			message.setMessage(Constants.FRIENDREQUEST, builder.toString().trim());
			Util.sendBytes(message.toString(), output);
			controller.appendTextAreaPacketSent(this.message.toString());

		}

	}

	private void broadcastOnlineFriends() {
		if (logged) {
			Set<String> friends = dao.getFriends(account.getUser());
			Iterator<String> items = friends.iterator();

			StringBuilder builder = new StringBuilder();

			while (items.hasNext()) {
				String friend = items.next();
				if (onlineAccounts.containsKey(friend))
					builder.append(friend + " ");
			}

			Message message = new Message(controller);
			message.setMessage(Constants.WHONLINE, builder.toString().trim());

			Util.sendBytes(message.toString(), output);
			controller.appendTextAreaPacketSent(this.message.toString());

		}

	}

}
