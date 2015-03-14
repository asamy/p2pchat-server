package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Util {

	public static boolean sendBytes(String string, DataOutputStream dos) {

		byte[] buffer = string.getBytes(StandardCharsets.UTF_16);
		int len = buffer.length;

		try {
			dos.writeInt(len);
			if (len > 0) {
				dos.write(buffer, 0, len);
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static String readBytes(DataInputStream dis) {
		byte[] data = null;
		int len = 0;

		try {
			len = dis.readInt();
			data = new byte[len];
			if (len > 0) {
				dis.readFully(data);
			}
		} catch (IOException e) {
			return null;
		}
		return new String(data, StandardCharsets.UTF_16);
	}

	public static boolean sendBytes(String string, PrintWriter output) {
		output.println(string);
		output.println();
		if (output.checkError())
			return false;

		return true;
	}

	public static boolean sendBytes(String string, PrintStream output) {
		output.println(string);
		output.println();
		if (output.checkError())
			return false;

		return true;
	}

	public static String readBytes(BufferedReader input) {
		StringBuilder builder = new StringBuilder();

		try {
			String line = input.readLine();
			if (line == null)
				return null;
			while (line != null && line.length() > 0) {
				builder.append(line);
				line = input.readLine();
			}
		} catch (IOException e) {
			return null;
		}

		return builder.toString();

	}
}
