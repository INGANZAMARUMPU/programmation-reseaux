import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class EtatTransfert {
    private EtatTransfert() {}
    private static Path fichier(String nom) {
        return Paths.get("receptions", nom + ".etat");
    }

    public record Etat(int chunks, byte[] hash) {}

    public static void ecrire(String nom, int chunks, byte[] hashPartiel)
            throws IOException {
        Path tmp = Paths.get("receptions", nom + ".etat.tmp");
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            out.writeInt(chunks);
            out.write(hashPartiel);
        }
        Files.move(tmp, fichier(nom), StandardCopyOption.REPLACE_EXISTING,
                   StandardCopyOption.ATOMIC_MOVE);
    }

    public static Etat lire(String nom) throws IOException {
        Path p = fichier(nom);
        if (!Files.exists(p)) return new Etat(0, new byte[32]);
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(p)))) {
            int chunks = in.readInt();
            byte[] hash = new byte[32];
            in.readFully(hash);
            return new Etat(chunks, hash);
        }
    }

    public static byte[] hashPartiel(Path fichier) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(fichier)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) md.update(buf, 0, n);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
