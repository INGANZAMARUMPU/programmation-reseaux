import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClientProto {
    public static void main(String[] args) throws IOException {
        try (Socket s = new Socket("127.0.0.1", 9011)) {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            short[] types = { Protocole.LOGIN, Protocole.SEND_FILE, Protocole.CHUNK };
            int[] ids = { 1, 2, 3 };
            for (int i = 0; i < types.length; i++) {
                byte[] corps = ("payload " + i).getBytes(StandardCharsets.UTF_8);
                Protocole.ecrire(out, types[i], ids[i], corps);
            }
            for (int i = 0; i < types.length; i++) {
                Message ack = Protocole.lire(in);
                if (ack.type() != Protocole.ACK || ack.id() != ids[i]) {
                    System.err.println("ACK inattendu : type=" + ack.type() + " id=" + ack.id());
                } else {
                    System.out.println("ACK " + ids[i] + " recu");
                }
            }
        }
    }
}
