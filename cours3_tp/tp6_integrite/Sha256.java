import java.io.*;
import java.nio.file.*;
import java.security.*;

public final class Sha256 {
    private Sha256() {}
    public static byte[] de(byte[] data) {
        try { return MessageDigest.getInstance("SHA-256").digest(data); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static byte[] de(Path fichier) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(fichier)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) md.update(buf, 0, n);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static boolean egal(byte[] a, byte[] b) { return MessageDigest.isEqual(a, b); }
}
