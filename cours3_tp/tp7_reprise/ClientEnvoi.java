import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ClientEnvoi {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("usage : ClientEnvoi <hote> <fichier>");
            return;
        }
        Path fichier = Paths.get(args[1]);
        try (Socket s = new Socket(args[0], 9011)) {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            Protocole.ecrire(out, Protocole.LOGIN, 1,
                             "client".getBytes(StandardCharsets.UTF_8));
            Protocole.lire(in);

            // 1) Tentative de reprise
            ByteArrayOutputStream demande = new ByteArrayOutputStream();
            new DataOutputStream(demande).writeUTF(fichier.getFileName().toString());
            Protocole.ecrire(out, Protocole.RESUME, 2, demande.toByteArray());
            Message rep = Protocole.lire(in);
            int debut = 0;
            if (rep.type() == Protocole.ACK) {
                DataInputStream c = new DataInputStream(
                    new ByteArrayInputStream(rep.corps()));
                debut = c.readInt();
                System.out.println("reprise possible au chunk " + debut);
            } else {
                System.out.println("pas de reprise : " + new String(
                    rep.corps(), StandardCharsets.UTF_8));
            }

            // 2) Si pas de reprise, envoyer SEND_FILE
            if (debut == 0) {
                byte[] hashGlobal = Sha256.de(fichier);
                ByteArrayOutputStream corps = new ByteArrayOutputStream();
                DataOutputStream c = new DataOutputStream(corps);
                c.writeUTF(fichier.getFileName().toString());
                c.writeLong(Files.size(fichier));
                c.write(hashGlobal);
                Protocole.ecrire(out, Protocole.SEND_FILE, 3, corps.toByteArray());
                Protocole.lire(in);
            }

            // 3) Chunks a partir de 'debut'
            byte[] buffer = new byte[Transfert.tailleChunk()];
            int numero = 0;
            int id = 10;
            try (InputStream fileIn = Files.newInputStream(fichier)) {
                int n;
                while ((n = fileIn.read(buffer)) != -1) {
                    if (numero >= debut) {
                        byte[] donnees = new byte[n];
                        System.arraycopy(buffer, 0, donnees, 0, n);
                        byte[] hashChunk = Sha256.de(donnees);
                        ByteArrayOutputStream cc = new ByteArrayOutputStream();
                        DataOutputStream cd = new DataOutputStream(cc);
                        cd.writeInt(numero);
                        cd.write(hashChunk);
                        cd.write(donnees);
                        Protocole.ecrire(out, Protocole.CHUNK, id++, cc.toByteArray());
                        Protocole.lire(in);
                    }
                    numero++;
                }
            }

            ByteArrayOutputStream fin = new ByteArrayOutputStream();
            new DataOutputStream(fin).writeInt(numero);
            Protocole.ecrire(out, Protocole.END_FILE, id, fin.toByteArray());
            Protocole.lire(in);
            System.out.println("transfert termine (debut " + debut + ")");
        }
    }
}
