import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClientProto {
    public static void main(String[] args) throws IOException {
        try (Socket s = new Socket("127.0.0.1", 9011)) {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            Protocole.ecrire(out, Protocole.SEND_FILE, 1, "x".getBytes(StandardCharsets.UTF_8));
            lireReponse(in, "avant login");
            Protocole.ecrire(out, Protocole.LOGIN, 2, "alice".getBytes(StandardCharsets.UTF_8));
            lireReponse(in, "login");
            Protocole.ecrire(out, Protocole.LOGIN, 3, "bob".getBytes(StandardCharsets.UTF_8));
            lireReponse(in, "second login");
            Protocole.ecrire(out, Protocole.SEND_FILE, 4, "x".getBytes(StandardCharsets.UTF_8));
            lireReponse(in, "send_file");
        }
    }

    static void lireReponse(DataInputStream in, String etape) throws IOException {
        Message rep = Protocole.lire(in);
        String nom = rep.type() == Protocole.ACK ? "ACK" : "ERROR";
        String corps = rep.type() == Protocole.ERROR
            ? new String(rep.corps(), StandardCharsets.UTF_8) : "";
        System.out.printf("[%s] %s id=%d %s%n", etape, nom, rep.id(), corps);
    }
}
