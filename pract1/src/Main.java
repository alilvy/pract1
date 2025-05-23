import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class ServerListener {
    private ServerSocket serverSocket;
    private static ArrayList<ClientHandler> clients;
    private ExecutorService executorService;

    public ServerListener() {
        clients = new ArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(27015);
        ChatLog log = new ChatLog();

        while (true) {
            Socket incomingConnection = serverSocket.accept();
            ClientHandler client = new ClientHandler(incomingConnection, log);
            clients.add(client);
            executorService.execute(client);
        }
    }

    public static List<ClientHandler> getClients() {
        return clients;
    }

    public static void removeClient(ClientHandler clientHandler) {
        System.out.println("Client " + clientHandler + " is deleted");
        clients.remove(clientHandler);
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ChatLog chatLog;
    private BufferedReader in;
    private BufferedWriter out;

    public ClientHandler(Socket clientSocket, ChatLog chatLog) {
        this.clientSocket = clientSocket;
        this.chatLog = chatLog;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String nickName = in.readLine();
            chatLog.put(nickName + " connected to chat", this);

            while (!Thread.currentThread().isInterrupted()) {
                String message = in.readLine();
                if (Objects.isNull(message)) {
                    break;
                }
                chatLog.put(nickName + ": " + message, this);
            }

            chatLog.put(nickName + " disconnected from chat", this);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ServerListener.removeClient(this);
        }
    }

    public void sendMessageToClient(String msg) throws IOException {
        if (!clientSocket.isOutputShutdown()) {
            out.write(msg);
            out.newLine();
            out.flush();
        }
    }
}

class ChatLog {
    private final ArrayList<String> chatHistory;
    private int pointer = 0;

    public ChatLog() {
        chatHistory = new ArrayList<>();
    }

    public synchronized void put(String message, ClientHandler clientSender) throws IOException {
        chatHistory.add(message);
        System.out.println(message);
        update(clientSender);
        pointer++;
    }

    private synchronized void update(ClientHandler clientSender) throws IOException {
        for (ClientHandler client : ServerListener.getClients()) {
            if (client != clientSender) {
                client.sendMessageToClient(chatHistory.get(pointer));
            }
        }
    }
}