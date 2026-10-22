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
            byte[] hashGlobal = Sha256.de(fichier);
            ByteArrayOutputStream corps = new ByteArrayOutputStream();
            DataOutputStream c = new DataOutputStream(corps);
            c.writeUTF(fichier.getFileName().toString());
            c.writeLong(Files.size(fichier));
            c.write(hashGlobal);
            Protocole.ecrire(out, Protocole.SEND_FILE, 2, corps.toByteArray());
            Protocole.lire(in);
            byte[] buffer = new byte[Transfert.tailleChunk()];
            int numero = 0;
            int id = 3;
            try (InputStream fileIn = Files.newInputStream(fichier)) {
                int n;
                while ((n = fileIn.read(buffer)) != -1) {
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
                    numero++;
                }
            }
            ByteArrayOutputStream fin = new ByteArrayOutputStream();
            new DataOutputStream(fin).writeInt(numero);
            Protocole.ecrire(out, Protocole.END_FILE, id, fin.toByteArray());
            Protocole.lire(in);
            System.out.println("transfert termine : " + numero + " chunks");
        }
    }
}
