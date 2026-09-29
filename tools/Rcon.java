import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Minimal Minecraft RCON client, just enough to run one command and read the reply.
 *
 * Framing is what vanilla uses: [int length][int id][int type][body][null][null], where length
 * counts everything after itself. The server interleaves an empty type-0 packet before the real
 * response, so the reader skips those.
 */
public class Rcon {
    private static final int SERVERDATA_AUTH = 3;
    private static final int SERVERDATA_COMMAND = 2;
    private static final int SERVERDATA_AUTH_RESPONSE = 2;
    private static final int SERVERDATA_RESPONSE = 0;

    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String password = args[2];
        String command = args[3];

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(20000);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            send(out, 0, SERVERDATA_AUTH, password);
            String authReply = awaitReply(in, SERVERDATA_AUTH_RESPONSE);
            if (authReply == null) {
                throw new IOException("RCON authentication failed");
            }

            System.out.println("authenticated");

            send(out, 2, SERVERDATA_COMMAND, command);
            String reply = awaitReply(in, SERVERDATA_RESPONSE);
            System.out.println("--- reply ---");
            System.out.println(reply == null ? "(empty)" : reply);
        }
    }

    private static void send(DataOutputStream out, int id, int type, String payload) throws IOException {
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        out.writeInt(data.length + 10);
        out.writeInt(id);
        out.writeInt(type);
        out.write(data);
        out.writeByte(0);
        out.writeByte(0);
        out.flush();
    }

    private static String awaitReply(DataInputStream in, int expectedType) throws IOException {
        while (true) {
            int length = in.readInt();
            if (length < 8 || length > 4 * 1024 * 1024) {
                throw new IOException("bad packet length " + length);
            }

            int id = in.readInt();
            int type = in.readInt();
            byte[] body = new byte[length - 8];
            in.readFully(body);

            if (type == expectedType) {
                return new String(body, StandardCharsets.UTF_8);
            }
        }
    }
}
