import java.io.*;
import java.nio.file.*;

public final class EtatTransfert {
    private EtatTransfert() {}
    private static Path fichier(String nom) {
        return Paths.get("receptions", nom + ".etat");
    }
    public static void ecrire(String nom, int chunks) throws IOException {
        Path tmp = Paths.get("receptions", nom + ".etat.tmp");
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            out.writeInt(chunks);
        }
        Files.move(tmp, fichier(nom), StandardCopyOption.REPLACE_EXISTING,
                   StandardCopyOption.ATOMIC_MOVE);
    }
    public static int lire(String nom) throws IOException {
        Path p = fichier(nom);
        if (!Files.exists(p)) return 0;
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(p)))) {
            return in.readInt();
        }
    }
}
