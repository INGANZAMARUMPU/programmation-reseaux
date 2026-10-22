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
        String hote = args[0];
        Path fichier = Paths.get(args[1]);
        try (Socket s = new Socket(hote, 9011)) {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            Protocole.ecrire(out, Protocole.LOGIN, 1,
                             "client".getBytes(StandardCharsets.UTF_8));
            Protocole.lire(in);
            ByteArrayOutputStream corps = new ByteArrayOutputStream();
            DataOutputStream c = new DataOutputStream(corps);
            c.writeUTF(fichier.getFileName().toString());
            c.writeLong(Files.size(fichier));
            c.write(new byte[32]);
            Protocole.ecrire(out, Protocole.SEND_FILE, 2, corps.toByteArray());
            Protocole.lire(in);
            byte[] buffer = new byte[Transfert.tailleChunk()];
            int numero = 0;
            int id = 3;
            try (InputStream fileIn = Files.newInputStream(fichier)) {
                int n;
                while ((n = fileIn.read(buffer)) != -1) {
                    ByteArrayOutputStream cc = new ByteArrayOutputStream();
                    DataOutputStream cd = new DataOutputStream(cc);
                    cd.writeInt(numero);
                    cd.write(new byte[32]);
                    cd.write(buffer, 0, n);
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
