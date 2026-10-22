import java.io.*;
import java.nio.file.*;

public class Transfert {
    private static final int TAILLE_CHUNK = 16 * 1024;
    private final String nomFichier;
    private final long tailleAttendue;
    private final Path chemin;
    private final OutputStream fichierOut;
    private int prochainChunk = 0;

    private Transfert(String nom, long taille, Path chemin, OutputStream out) {
        this.nomFichier = nom;
        this.tailleAttendue = taille;
        this.chemin = chemin;
        this.fichierOut = out;
    }

    public static Transfert demarrer(Session session, Message msg) throws IOException {
        DataInputStream corps = new DataInputStream(new ByteArrayInputStream(msg.corps()));
        String nom = corps.readUTF();
        long taille = corps.readLong();
        byte[] hashGlobal = new byte[32];
        corps.readFully(hashGlobal);
        Path dossier = Paths.get("receptions");
        Files.createDirectories(dossier);
        Path chemin = dossier.resolve(nom);
        OutputStream out = new BufferedOutputStream(Files.newOutputStream(chemin));
        System.out.println("[transfert] demarrage " + nom + " (" + taille + " octets)");
        return new Transfert(nom, taille, chemin, out);
    }

    public static Transfert reprendre(Session session, Message msg) throws IOException {
        DataInputStream corps = new DataInputStream(new ByteArrayInputStream(msg.corps()));
        String nom = corps.readUTF();
        Path chemin = Paths.get("receptions").resolve(nom);
        if (!Files.exists(chemin)) {
            session.erreur(msg.id(), "aucun transfert en cours pour " + nom);
            return null;
        }
        int dejaRecus = EtatTransfert.lire(nom);
        OutputStream out = new BufferedOutputStream(
            Files.newOutputStream(chemin, StandardOpenOption.APPEND));
        Transfert t = new Transfert(nom, -1, chemin, out);
        t.prochainChunk = dejaRecus;
        System.out.println("[transfert] reprise " + nom + " au chunk " + dejaRecus);
        return t;
    }

    public void recevoirChunk(Session session, Message msg) throws IOException {
        DataInputStream corps = new DataInputStream(new ByteArrayInputStream(msg.corps()));
        int numero = corps.readInt();
        byte[] hashChunk = new byte[32];
        corps.readFully(hashChunk);
        int tailleDonnees = msg.corps().length - 4 - 32;
        byte[] donnees = new byte[tailleDonnees];
        corps.readFully(donnees);
        if (numero != prochainChunk) {
            session.erreur(msg.id(), "chunk " + numero + " recu, attendu " + prochainChunk);
            return;
        }
        fichierOut.write(donnees);
        prochainChunk++;
        EtatTransfert.ecrire(nomFichier, prochainChunk);
        session.ack(msg.id());
    }

    public void terminer(Session session, Message msg) throws IOException {
        fichierOut.close();
        long taille = Files.size(chemin);
        System.out.println("[transfert] fin " + nomFichier + " : "
                           + prochainChunk + " chunks, " + taille + " octets");
        if (tailleAttendue > 0 && taille != tailleAttendue) {
            session.erreur(msg.id(), "taille incorrecte : " + taille);
            return;
        }
        session.ack(msg.id());
        session.retourAuthentifie();
    }

    public static int tailleChunk() { return TAILLE_CHUNK; }
}
