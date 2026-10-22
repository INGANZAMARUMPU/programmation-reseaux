import java.io.*;

public final class Protocole {
    public static final short LOGIN     = 1;
    public static final short SEND_FILE = 2;
    public static final short CHUNK     = 3;
    public static final short END_FILE  = 4;
    public static final short ACK       = 5;
    public static final short ERROR     = 6;
    public static final short RESUME    = 7;
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
        if (longueur < 0 || longueur > LONGUEUR_MAX) {
            throw new IOException("longueur invalide : " + longueur);
        }
        byte[] corps = new byte[longueur];
        in.readFully(corps);
        return new Message(type, id, corps);
    }
}
