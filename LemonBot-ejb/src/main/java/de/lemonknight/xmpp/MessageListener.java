package de.lemonknight.xmpp;

import de.lemonknight.bot.BotBehavior;
import static de.lemonknight.util.Utils.unwrapCDATA;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 *
 * @author Lemon Knight
 */
public class MessageListener implements PacketListener {

    private final MultiUserChat muc;

    private static final Logger LOGGER = Logger.getLogger(MessageListener.class.getPackage().getName());

    private static final String[] GREETINGS = {"Hallo ", "Sei gegrüßt ", "Ahoyhoy ", "Es ist mir eine Freude dich zu sehen "};
    private static final String[] FAREWELLS = {"Auf Wiedersehen ", "Tschüss ", "Es war mir eine Ehre mit dir zu sprechen "};

    public MessageListener(MultiUserChat muc) {
        this.muc = muc;
    }

    @Override
    public void processPacket(Packet packet) {
        try {
            final Message message = (Message) packet;
            String text = unwrapCDATA(message.getBody()).trim();
            String user = StringUtils.parseResource(message.getFrom());
            if (!text.startsWith("!")) {
                handleNoCommand(user, text);
                return;
            }
            switch (text.split(" ")[0]) {
                case "!hello":
                    handleHello(user);
                    break;
                case "!bye":
                    handleBye(user);
                    break;
                case "!stream":
                    handleStream();
                    break;
                case "!website":
                    handleWebsite();
                    break;
                case "!url":
                    handleWebsite();
                    break;
                case "!forum":
                    handleForum();
                    break;
                case "!synch_chats":
                    handleSynchChat(user, text);
                    break;
                default:
                    handleNoCommand(user, text);
                    break;
            }
        } catch (XMPPException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private boolean checkLemonKnight(String user) {
        return "Lemon Knight".equals(user) || "lurkane".equals(user);
    }

    private void handleHello(String user) throws XMPPException {
        int rnd = (int) Math.floor(Math.random() * GREETINGS.length);
        muc.sendMessage(GREETINGS[rnd] + user);
    }

    private void handleBye(String user) throws XMPPException {
        int rnd = (int) Math.floor(Math.random() * FAREWELLS.length);
        muc.sendMessage(FAREWELLS[rnd] + user);
    }

    private void handleStream() throws XMPPException {
        muc.sendMessage("Den Liga der Gentlemen Stream findet ihr hier: http://twitch.tv/ligadergentlemen/");
    }

    private void handleWebsite() throws XMPPException {
        muc.sendMessage("Die  Liga der Gentlemen Website findet ihr hier: http://liga-der-gentlemen.de/");
    }

    private void handleForum() throws XMPPException {
        muc.sendMessage("Das  Liga der Gentlemen Forum findet ihr hier: http://forum.liga-der-gentlemen.de/index.php");
    }

    private void handleNoCommand(String user, String text) {
        if (!BotBehavior.SYNC_CHATS || "LemonBot".equals(user)) {
            return;
        }
        String message = user + ": " + text;
        if (message.length() <= 200) {
            getLolChatConnection().synchChat(message, muc.getRoom());
        } else {
            final ILolChatConnetion con = getLolChatConnection();
            String part1 = message.substring(0, 200);
            String part2 = message.substring(200);
            con.synchChat(part1, muc.getRoom());
            con.synchChat(part2, muc.getRoom());
        }

    }

    private ILolChatConnetion getLolChatConnection() {
        try {
            return (ILolChatConnetion) new InitialContext().lookup("de.lemonknight.xmpp.ILolChatConnetion");
        } catch (NamingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void handleSynchChat(String user, String text) throws XMPPException {
        if (!checkLemonKnight(user)) {
            return;
        }
        String bool = text.split(" ")[1];
        switch (bool) {
            case "true":
                if (BotBehavior.SYNC_CHATS) {
                    muc.sendMessage("Chats werden bereits synchronisiert.");
                } else {
                    BotBehavior.SYNC_CHATS = true;
                    muc.sendMessage("Chats werden nun synchronisiert.");
                }
                break;
            case "false":
                if (BotBehavior.SYNC_CHATS) {
                    BotBehavior.SYNC_CHATS = false;
                    muc.sendMessage("Chats werden nicht mehr synchronisiert.");
                } else {
                    muc.sendMessage("Chats werden bereits nicht synchronisiert.");
                }
                break;
            default:
                muc.sendMessage("Unbekannter Parameter. Usage: !synch_chats [true|false]");
                break;
        }
    }

}
