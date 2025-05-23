import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 27015);
            System.out.println("Connected to server. Enter your nickname:");

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String nickname = consoleReader.readLine();

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write(nickname);
            out.newLine();
            out.flush();

            ExecutorService executorService = Executors.newFixedThreadPool(2);

            // Поток для чтения сообщений от сервера
            executorService.execute(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server");
                }
            });

            // Поток для отправки сообщений на сервер
            executorService.execute(() -> {
                try {
                    String userInput;
                    while ((userInput = consoleReader.readLine()) != null) {
                        out.write(userInput);
                        out.newLine();
                        out.flush();
                    }
                } catch (IOException e) {
                    System.out.println("Connection error");
                }
            });

        } catch (IOException e) {
            System.out.println("Could not connect to server");
        }
    }
}
