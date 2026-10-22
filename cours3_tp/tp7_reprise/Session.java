import java.io.*;
import java.nio.charset.StandardCharsets;

public class Session {
    private final DataOutputStream out;
    private Etat etat = Etat.CONNECTE;
    private String pseudo;
    private Transfert transfert;

    public Session(DataOutputStream out) { this.out = out; }

    public void traiter(Message msg) throws IOException {
        switch (etat) {
            case CONNECTE    -> traiterConnecte(msg);
            case AUTHENTIFIE -> traiterAuthentifie(msg);
            case TRANSFERANT -> traiterTransferant(msg);
            case TERMINE     -> erreur(msg.id(), "session terminee");
        }
    }

    private void traiterConnecte(Message msg) throws IOException {
        if (msg.type() != Protocole.LOGIN) { erreur(msg.id(), "LOGIN attendu"); return; }
        pseudo = new String(msg.corps(), StandardCharsets.UTF_8);
        etat = Etat.AUTHENTIFIE;
        ack(msg.id());
    }

    private void traiterAuthentifie(Message msg) throws IOException {
        switch (msg.type()) {
            case Protocole.SEND_FILE -> {
                transfert = Transfert.demarrer(this, msg);
                if (transfert != null) { etat = Etat.TRANSFERANT; ack(msg.id()); }
            }
            case Protocole.RESUME -> {
                transfert = Transfert.reprendre(this, msg);
                if (transfert != null) { etat = Etat.TRANSFERANT; }
            }
            default -> erreur(msg.id(), "SEND_FILE ou RESUME attendu");
        }
    }

    private void traiterTransferant(Message msg) throws IOException {
        switch (msg.type()) {
            case Protocole.CHUNK    -> transfert.recevoirChunk(this, msg);
            case Protocole.END_FILE -> transfert.terminer(this, msg);
            default -> erreur(msg.id(), "CHUNK ou END_FILE attendu");
        }
    }

    void retourAuthentifie() { this.transfert = null; this.etat = Etat.AUTHENTIFIE; }

    public void ack(int id) throws IOException {
        Protocole.ecrire(out, Protocole.ACK, id, new byte[0]);
    }

    public void ack(int id, byte[] corps) throws IOException {
        Protocole.ecrire(out, Protocole.ACK, id, corps);
    }

    public void erreur(int id, String message) throws IOException {
        Protocole.ecrire(out, Protocole.ERROR, id,
                         message.getBytes(StandardCharsets.UTF_8));
    }
}
