import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TestLimitesTexte {
    public static void main(String[] args) throws IOException {
        try (Socket s = new Socket("127.0.0.1", 9000)) {
            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);

            out.println("ligne1");
            out.println("ligne2");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) sb.append('x');
            out.println(sb);

            out.println("\u00e9\u00e0\u00fc \ud83d\ude00");

            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
    }
}
