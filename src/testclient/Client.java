package testclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import server.Message;
import server.Util;

public class Client {

	BufferedReader input;
	PrintStream output;

	@SuppressWarnings("resource")
	public void start() throws UnknownHostException, IOException {

		SSLSocket socket = null;
		SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		socket = (SSLSocket) sslSocketFactory.createSocket("localhost", 5555);

		final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
		socket.setEnabledCipherSuites(enabledCipherSuites);

		output = new PrintStream(socket.getOutputStream());
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		new Listen().start();

		while (true) {
			String str = new Scanner(System.in).nextLine();

			if (!Util.sendBytes(str, output))
				break;

		}

	}

	class Listen extends Thread {

		public void run() {
			String str = null;
			while ((str = Util.readBytes(input)) != null) {
				Message msg = new Message(null);
				msg.decodeMessage(str);
				System.out.println(msg.toString());

			}
			System.out.println("end listen server");
		}

	}

	public static void main(String[] args) {

		try {
			new Client().start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
