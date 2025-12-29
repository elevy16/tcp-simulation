package tcpJavaSimulation;

import java.util.*;
import java.io.*;
import java.net.*;

public class Client {

	// ***** Text protocol tokens *****
	private static final String TOKEN_ACK = "ACK"; // Used when client acknowledges it has received everything

	public static void main(String[] args) {
		System.out.println("Protocol Simulation Packet Demo");
		// Check that the user typed exactly two arguments when running the program:
		if (args.length != 2) {
			System.err.println("Usage: java Client <host> <port>");
			return;
		}

		// Store the first argument (host) and second argument (port)
		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		try (
				// Open a connection to the server
				Socket socket = new Socket(host, port);
				// Create input & output streams to read from and write to server
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

			runClient(in, out);

		} catch (IOException e) {
			System.err.println("Connection error: " + e.getMessage());
		}
	}

	// ***** Client state to keep track of the message we're rebuilding *****

	private static class ClientState {

		int total = -1; // unknown until we get the first packet

		String[] chunks = null; // array to hold message packets

		BitSet received = new BitSet(); // tracks which packets arrived

		boolean lastPacketReceived = false; // tracks whether the last packet has arrived

	}

	// ***** Read from server *****
	private static void runClient(BufferedReader in, PrintWriter out) throws IOException {
		ClientState state = new ClientState();
		String line;

		// remember the last resend we asked for, so we don't spam identical
		// requests
		String lastResendAskedFor = "";

		while ((line = in.readLine()) != null) { // keep reading until server closes connection
			if (line.isEmpty()) // if server sends a blank line,
				continue; // skip loop and continue to next line
			if (line.startsWith("SEQUENCE:")) { // server sent one piece of the message,
				handleDataLine(line, state); // so client breaks it into chunks and saves the payload
			} else {

				// Ignore unrecognized tokens
				System.err.println("Ignoring unrecognized line: " + line);

			}

			// Check if we have received the last packet and all chunks
			if (state.lastPacketReceived) {
				List<Integer> missing = findMissing(state);
				if (missing.isEmpty()) {
					// Got everything — reconstruct and print
					System.out.println("Message reconstructed, rendering to stdout...");
					System.out.println(joinChunks(state.chunks));

					// Acknowledge that everything sent successfully (optional)
					out.println(TOKEN_ACK + "|DONE");
					break;
				} else {
					// ADDED: ask the server to resend the missing indexes
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < missing.size(); i++) {
						if (i > 0)
							sb.append(',');
						sb.append(missing.get(i));
					}
					String ask = sb.toString();
					if (!ask.equals(lastResendAskedFor)) { // avoid sending the exact same request repeatedly
						out.println("RESEND:" + ask);
						lastResendAskedFor = ask;
					}
					// then keep looping and wait for resent chunks
				}
			}
		}
	}

	// ***** Handles one DATA packet from the server *****

	// Parse and store: SEQUENCE: <seq> DATA: <payload> END: <true/false>

	private static void handleDataLine(String line, ClientState state) {

		// Expect format: SEQUENCE: <seq> DATA: <payload> END: <true/false>
		try {
			// robust parsing that preserves spaces in <payload>
			// Find the marker positions
			int seqTag = line.indexOf("SEQUENCE:");
			int dataTag = line.indexOf(" DATA:");
			int endTag = line.lastIndexOf(" END:");

			if (seqTag != 0 || dataTag < 0 || endTag < 0 || dataTag >= endTag) {
				throw new IllegalArgumentException("Malformed line");
			}

			// Extract numbers and payload using the exact tag lengths (with trailing
			// spaces)
			int seq = Integer.parseInt(line.substring("SEQUENCE:".length(), dataTag).trim()); // extract sequence number
			String payload = line.substring(dataTag + " DATA: ".length(), endTag).trim(); // extract payload (may
																							// contain spaces)
			boolean isLast = Boolean.parseBoolean(line.substring(endTag + " END: ".length()).trim()); // check if this
																										// is the last
																										// packet

			ensureTotalInitialized(state, seq + 1); // assume total is at least seq+1

			// Ignore out-of-range seq
			if (seq < 0 || seq >= state.total) {
				System.err.println("Ignoring DATA with out-of-range seq=" + seq + " (total=" + state.total + ")");
				return;
			}

			// Only store if we don’t already have this piece
			if (!state.received.get(seq)) {
				state.chunks[seq] = payload; // keep payload exactly as sent
				state.received.set(seq);
			}

			if (isLast) {
				state.lastPacketReceived = true; // mark that last packet has been received
			}
		} catch (Exception e) {
			System.err.println("Bad packet format: " + line);
		}

	}

	// ***** Helper methods *****
	// (AI helped me with choosing what methods to create and then helped me with
	// them)
	// If we haven't learned total yet, set it and allocate the chunks array
	private static void ensureTotalInitialized(ClientState state, int declaredTotal) {
		if (state.total == -1) { // unknown total
			state.total = declaredTotal;
			state.chunks = new String[state.total]; // create array to hold payload

		} else if (state.total < declaredTotal) {
			// Resize array if declared total is larger than current
			String[] newChunks = new String[declaredTotal];
			System.arraycopy(state.chunks, 0, newChunks, 0, state.chunks.length);
			state.chunks = newChunks;
			state.total = declaredTotal;
		}
	}

	// After each packet, find which chunks are missing.
	private static List<Integer> findMissing(ClientState state) {
		List<Integer> missing = new ArrayList<>(); // create list to hold missing packet numbers
		for (int i = 0; i < state.total; i++) { // loop through all possible packets
			if (state.chunks[i] == null) {
				missing.add(i); // add missing index number to list
			}
		}

		return missing;
	}

	// Rebuilds final message - concatenate all chunks
	private static String joinChunks(String[] chunks) {
		StringBuilder sb = new StringBuilder();
		for (String chunk : chunks) {
			if (chunk != null) {
				sb.append(chunk);
			}
		}
		return sb.toString();
	}
}
