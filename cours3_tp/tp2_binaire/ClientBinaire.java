import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClientBinaire {
    public static void main(String[] args) throws IOException {
        try (Socket s = new Socket("127.0.0.1", 9010)) {
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeInt(42);
            out.writeLong(1234567890123L);
            out.writeUTF("bonjour");
            byte[] data = "payload".getBytes(StandardCharsets.UTF_8);
            out.writeInt(data.length);
            out.write(data);
            out.flush();
        }
    }
}
