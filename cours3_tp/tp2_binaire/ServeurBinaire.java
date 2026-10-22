import java.io.*;
import java.net.*;

public class ServeurBinaire {
    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(9010)) {
            System.out.println("[serveur] ecoute sur 9010");
            try (Socket conn = server.accept()) {
                DataInputStream in = new DataInputStream(conn.getInputStream());
                int i = in.readInt();
                long l = in.readLong();
                String s = in.readUTF();
                int len = in.readInt();
                byte[] data = new byte[len];
                in.readFully(data);
                System.out.println("int    = " + i);
                System.out.println("long   = " + l);
                System.out.println("string = " + s);
                System.out.println("data   = " + data.length + " octets");
            }
        }
    }
}
