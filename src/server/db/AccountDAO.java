package server.db;

import gui.GuiController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class handle all the database querys
 * 
 * @author Kfir fersht
 *
 */
public class AccountDAO {

	private Connection conn;
	private GuiController controller;
	
	
	public AccountDAO(GuiController controller) {
		this.controller = controller;
		conn = ConnectionManager.getIntance().getConnection(controller);
	}
	

	/**
	 * 
	 * @param user
	 *            - the user name
	 * @param password
	 *            - password
	 * @param email
	 *            - email
	 * @return true if created success, or false
	 */
	public boolean createAccount(String user, String password, String email) {
		String query = "INSERT into accounts (user,password,email) VALUES (?,?,?)";

		if (getAccount(user) != null)
			return false;

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, user);
			ps.setString(2, password);
			ps.setString(3, email);

			if (ps.executeUpdate() == 1)
				return true;
			else
				return false;
		} catch (SQLException e) {
			controller.exception(e);
			return false;
		}

	}

	/**
	 * Authenticator
	 * 
	 * @param user
	 * @param password
	 * @return true if the user is valid, false if not or mabye not exists
	 */

	public boolean isValidAccount(String user, String password) {

		Account account = getAccount(user);

		if (account != null && password.equals(account.getPassword())) {
			account.setId(account.getId());
			return true;
		}

		return false;
	}

	/**
	 * Add new friend
	 * 
	 * @param user
	 *            the user name who want add friend
	 * @param friend
	 *            the friend to be added
	 * @return true if friend added succefuly
	 */
	public boolean addFriend(String user, String friend) {

		if (user.equals(friend) || getAccount(friend) == null)
			return false;

		Set<String> friends = getFriends(user);

		if (friends.contains(friend))
			return false;

		String query = "INSERT into friends (id,friend) VALUES (?,?)";

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setInt(1, getAccount(user).getId());
			ps.setString(2, friend);

			if (ps.executeUpdate() == 1) {

				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			controller.exception(e);
			return false;
		}

	}

	/**
	 * Remove friend
	 * 
	 * @param user
	 *            the username who have this friend
	 * @param friend
	 *            the friend who will be removed
	 * @return true if friend removed succefuly
	 */

	public boolean removeFriend(String user, String friend) {

		Set<String> friends = getFriends(user);

		Account account = getAccount(user);

		if (!friends.contains(friend))
			return false;

		String query = "DELETE FROM friends WHERE friends.id = ? AND friends.friend = ?";

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setInt(1, account.getId());
			ps.setString(2, friend);

			if (ps.executeUpdate() == 1) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			controller.exception(e);
			return false;
		}
	}

	/**
	 * 
	 * @param user
	 * @return Set<String> of the friends of the given user
	 */
	// Get friends
	public Set<String> getFriends(String user) {
		Set<String> friends = new HashSet<>();

		String query = "SELECT friends.* FROM accounts INNER JOIN friends WHERE friends.id = accounts.id and accounts.user = ?";

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, user);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				friends.add(rs.getString("friend"));
			}

		} catch (SQLException e) {
			controller.exception(e);
			return null;
		}

		return friends;
	}

	/**
	 * Add request to friend
	 * 
	 * @param user
	 *            the user who will get request
	 * @param friend
	 *            who sent the request
	 * @return true if request added succefuly
	 */
	// Add request
	public boolean addRequest(String user, String friend) {

		if (user.equals(friend) || getAccount(friend) == null)
			return false;

		Set<String> requests = getRequestsList(user);
		if (requests.contains(friend))
			return false;

		Set<String> friends = getFriends(user);
		if (friends.contains(friend))
			return false;

		String query = "INSERT into requests (id,user) VALUES (?,?)";

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setInt(1, getAccount(user).getId());
			ps.setString(2, friend);

			if (ps.executeUpdate() == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			controller.exception(e);
			return false;
		}

	}

	/**
	 * Remove request
	 * 
	 * @param user
	 * @param request
	 * @return true if reuqest removed success
	 */
	// Remove request
	public boolean removeRequest(String user, String request) {

		Set<String> requests = getRequestsList(user);

		if (!requests.contains(request))
			return false;

		Account account = getAccount(user);

		if (account == null)
			return false;

		String query = "DELETE FROM requests WHERE requests.id = ? AND requests.user = ?";

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setInt(1, account.getId());
			ps.setString(2, request);

			if (ps.executeUpdate() == 1) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			controller.exception(e);
			return false;
		}
	}

	/**
	 * Getting request list
	 * 
	 * @param user
	 * @return Set<String> containing the requests
	 */
	public Set<String> getRequestsList(String user) {
		Set<String> requests = new HashSet<>();

		String query = "SELECT requests.* FROM accounts INNER JOIN requests WHERE accounts.id = requests.id and accounts.user = ?";

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, user);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				requests.add(rs.getString("user"));
			}

		} catch (SQLException e) {
			controller.exception(e);
			return null;
		}

		return requests;
	}

	/**
	 * Add block
	 * 
	 * @param user
	 *            the user who want block
	 * @param block
	 *            the block username
	 * @return true if blocked success
	 */
	public boolean addBlock(String user, String block) {

		if (user.equals(block) || getAccount(block) == null)
			return false;

		Set<String> blocked = getBlockedList(user);

		if (blocked.contains(block))
			return false;

		Account account = getAccount(user);

		if (account == null)
			return false;

		String query = "INSERT into blocked (id,user) VALUES (?,?)";

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setInt(1, account.getId());
			ps.setString(2, block);

			if (ps.executeUpdate() == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			controller.exception(e);
			return false;
		}

	}

	/**
	 * Remove block
	 * 
	 * @param user
	 *            the user who want remove block
	 * @param block
	 *            the block to be removed
	 * @return true if unblock success
	 */
	public boolean removeBlock(String user, String block) {

		Set<String> blocked = getBlockedList(user);

		if (!blocked.contains(block))
			return false;

		Account account = getAccount(user);

		if (account == null)
			return false;

		String query = "DELETE FROM blocked WHERE blocked.id = ? AND blocked.user = ?";

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setInt(1, account.getId());
			ps.setString(2, block);

			if (ps.executeUpdate() == 1) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			controller.exception(e);
			return false;
		}
	}

	// Get blockedList
	public Set<String> getBlockedList(String user) {
		Set<String> blocked = new HashSet<>();

		String query = "SELECT blocked.* FROM accounts INNER JOIN blocked WHERE accounts.id = blocked.id and accounts.user = ?";

		try (PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, user);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				blocked.add(rs.getString("user"));
			}

		} catch (SQLException e) {
			controller.exception(e);
			return null;
		}

		return blocked;
	}

	// Get account
	public Account getAccount(String user) {

		Account account = null;
		String query = "SELECT * FROM accounts WHERE user = ?";

		ResultSet rs = null;
		try (PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, user);

			rs = stmt.executeQuery();

			if (rs.next()) {
				account = new Account(rs.getInt("id"), rs.getString("user"), rs.getString("password"));
			}
		} catch (SQLException e) {
			controller.exception(e);
			return null;
		}

		return account;
	}

}
