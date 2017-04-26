import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
	public static void main(String[] args) {
			// checks number of arguments
			if (args.length != 2) {
				throw new IllegalArgumentException();
			}

			// Gets wordlist from file
			int servPort = Integer.parseInt(args[0]);
			int window = Integer.parseInt(args[1]);
			System.out.println("Running on server port " + servPort);

			RELDATMID conn = new RELDATMID(servPort, window);
			//conn.waitForConnection();
			while (true) {
				System.out.println("Waiting on file from client...");
				conn.receiveFile();
				
				//conn.sendFile();
			}
	}
}