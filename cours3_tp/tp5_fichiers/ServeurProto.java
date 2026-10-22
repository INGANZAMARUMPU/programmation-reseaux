import java.io.*;
import java.net.*;

public class ServeurProto {
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(9011)) {
            System.out.println("[serveur] ecoute sur 9011");
            while (true) {
                Socket conn = server.accept();
                new Thread(() -> {
                    try (conn;
                         DataInputStream in = new DataInputStream(conn.getInputStream());
                         DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                        Session session = new Session(out);
                        while (true) {
                            Message msg;
                            try { msg = Protocole.lire(in); }
                            catch (EOFException e) { break; }
                            session.traiter(msg);
                        }
                    } catch (IOException e) {
                        System.err.println("erreur : " + e.getMessage());
                    }
                }).start();
            }
        }
    }
}
