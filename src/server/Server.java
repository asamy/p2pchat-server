package server;

import gui.GuiController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import server.db.ConnectionManager;

public class Server {

	private int port = 5555;
	private SSLServerSocket serverSocket = null;
	private boolean running;
	private List<ClientHandler> clients;
	private GuiController controller;
	SSLSocket sslSocket;

	public Server(GuiController controller) {
		this.controller = controller;
		ConnectionManager.getIntance().getConnection(controller);
		Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e)->{
			controller.exception(e);
		});
	}

	private void start() {

		SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		try {
			this.serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
		this.serverSocket.setEnabledCipherSuites(enabledCipherSuites);

		while (running) {
			try {
				sslSocket = (SSLSocket) serverSocket.accept();
			} catch (IOException e) {
				continue;
			}

			ClientHandler client = new ClientHandler(sslSocket,controller);
			new Thread(client).start();
			clients.add(client);
		}

	}

	public void stopServer() {
		controller.appendTextAreaConnections("Server stopped");
		running = false;
		if (clients != null) {
			for (int i = 0; i < clients.size(); i++) {
				clients.get(i).close();
			}
			clients.clear();
		}
		clients = null;
		if (serverSocket != null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		serverSocket = null;
	}

	public void startServer() {
		controller.appendTextAreaConnections("Server started");
		clients = new ArrayList<>();
		running = true;
		Thread startServerWorker = new Thread(()->{
			start();
		});
		
		startServerWorker.setPriority(9);
		startServerWorker.start();
		
		


		
	}
	


	public boolean isRunning() {
		return this.running;
	}


}
