import java.io.*;

public final class Protocole {
    public static final short LOGIN = 1, SEND_FILE = 2, CHUNK = 3, END_FILE = 4;
    public static final short ACK = 5, ERROR = 6, RESUME = 7;
    public static final int LONGUEUR_MAX = 1024 * 1024;
    private Protocole() {}
    public static void ecrire(DataOutputStream out, short type, int id, byte[] corps)
            throws IOException {
        out.writeShort(type);
        out.writeInt(corps.length);
        out.writeInt(id);
        out.write(corps);
        out.flush();
    }
    public static Message lire(DataInputStream in) throws IOException {
        short type = in.readShort();
        int longueur = in.readInt();
        int id = in.readInt();
        if (longueur < 0 || longueur > LONGUEUR_MAX)
            throw new IOException("longueur invalide : " + longueur);
        byte[] corps = new byte[longueur];
        in.readFully(corps);
        return new Message(type, id, corps);
    }
}
