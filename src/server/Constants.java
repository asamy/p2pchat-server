package server;

public class Constants {
	public static final int WHOISIN = 0, MESSAGE = 1;

	// logout
	public static final int LOGOUT = 20, LOGOUTSUCCESS = 21, LOGOUTFAILED = 22, AUTHENTICATIONFAILED = 23;

	// login
	public static final int LOGIN = 10, LOGINFAILED = 11, LOGINSUCCESS = 12;

	// friends
	public static final int ADDFRIEND = 40, DELETEFRIEND = 41, NOTFOUND = 42, FOUND = 43, SEARCH = 30,
			FRIENDSLIST = 31, FRIENDALLREADY = 33, ADDFRIENDOK = 47, DELETEFRIENDOK = 48, WHONLINE = 32,
			FRIENDREQUEST = 50, ADDBLOCK = 60, REMOVEBLOCK = 61, BLOCKLIST = 67, REMOVEREQUEST = 88;

	// register
	public static final int REGISTER = 4, REGISTERSUCCESS = 5, REGISTERFAILED = 6;

	// global chat
	public static final int GLOBALCHAT = 60, GLOBALMASSAGE = 61;

	public static final int REQUESTUSERIP = 70, REQUESTIPACCEPTED = 71, REQUESTIPFAILED = 75, SOMEONEWANTCHAT = 72,
			UPDATEPORT = 73;

}
