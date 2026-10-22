import java.io.*;
import java.net.*;

public class ServeurProto {
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(9011)) {
            System.out.println("[serveur proto] ecoute sur 9011");
            try (Socket conn = server.accept()) {
                DataInputStream in = new DataInputStream(conn.getInputStream());
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                while (true) {
                    Message msg;
                    try {
                        msg = Protocole.lire(in);
                    } catch (EOFException e) {
                        System.out.println("[serveur] client deconnecte");
                        break;
                    }
                    System.out.printf("[serveur] type=%d id=%d corps=%d octets%n",
                                      msg.type(), msg.id(), msg.corps().length);
                    Protocole.ecrire(out, Protocole.ACK, msg.id(), new byte[0]);
                }
            }
        }
    }
}
