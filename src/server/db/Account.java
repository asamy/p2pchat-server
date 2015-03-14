package server.db;

import java.util.HashSet;
import java.util.Set;

public class Account {

	private int id;
	private String user;
	private String password;
	private Set<String> friends = new HashSet<>();

	public Account(int id, String user, String password) {
		this.id = id;
		this.user = user;
		this.password = password;
	}

	public Account(String user, String password) {
		this.user = user;
		this.password = password;
	}

	public Account() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<String> getFriends() {
		return friends;
	}

	public void setFriends(Set<String> friends) {
		this.friends = friends;
	}

}
