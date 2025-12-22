# TCP Client-Server Simulation (Java)

## Overview
This project is a Java-based simulation of a **TCP client-server model**. It demonstrates how a client and server communicate over a network using sockets, following core TCP concepts such as connection establishment, message transmission, and graceful termination.

The project consists of a `Server` program that listens for incoming connections and a `Client` program that connects to the server and exchanges messages.

---

## Features
- TCP socket communication using Java
- Client-server architecture
- Server listens on a specified port
- Client initiates a connection to the server
- Message exchange between client and server
- Proper handling of input/output streams
- Clean connection setup and shutdown

---

## Project Structure
- **Server.java**  
  Implements the TCP server. Listens for incoming client connections, accepts them, and processes messages sent by the client.

- **Client.java**  
  Implements the TCP client. Connects to the server, sends messages, and receives responses.

---

## How It Works
1. The server starts and listens on a designated port.
2. The client connects to the server using the server’s host and port.
3. The client sends data to the server over a TCP socket.
4. The server receives and processes the data.
5. Responses are sent back to the client.
6. Both sides close the connection cleanly.

---

## How to Run

### Step 1: Compile
From the project directory:

### Step 2: Start the Server
Run the server first:

### Step 3: Run the Client
In a separate terminal window:

---

## Concepts Demonstrated
- TCP networking fundamentals
- Client-server communication
- Java `Socket` and `ServerSocket`
- Input and output streams
- Blocking I/O
- Network programming basics

---

## Assumptions
- The server is running before the client attempts to connect
- Communication occurs over localhost unless otherwise specified
- Only one client connection is handled at a time (unless extended)

---

## Possible Extensions
- Support multiple concurrent clients
- Add threading on the server side
- Implement message protocols or commands
- Add timeout handling and error recovery
- Log client-server interactions

---

## Author
Esther Levy  
Computer Science Student
