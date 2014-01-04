package de.lemonknight.xmpp;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Startup;

/**
 *
 * @author Lemon Knight
 */
@Singleton
@Startup
@Remote(ILolChatConnetion.class)
public class LolChatConnection implements ILolChatConnetion {

    private static final Logger LOGGER = Logger.getLogger(LolChatConnection.class.getPackage().getName());

    private XMPPConnection connection;

    private static final String USER = "LemonBot";
    private static final String PASSWORD = System.getProperty("LEMON_BOT_PW");
    
    private MultiUserChat[] mucs;

    @PostConstruct
    public void init() {
        mucs = new MultiUserChat[2];
        login();
        //LemonBot
        String chatRoomJID = "pu~86dfae4f8c66d614cad0a54bb11917614acd2822@lvl.pvp.net";
        //Liga der Gentlemen
        //String chatRoomJID = "pu~e43a3608c1c717ad341c2582fa3794fe2d105703@lvl.pvp.net";
        mucs[0] = joinChat(chatRoomJID);
        //LemonBot2
        chatRoomJID = "pu~b8957397629571092cdddf2ef033426e04ddc542@lvl.pvp.net";
        mucs[1] = joinChat(chatRoomJID);
    }

    @PreDestroy
    private void close() {
        connection.disconnect();
    }

    private void login() {
        SASLAuthentication.supportSASLMechanism("PLAIN", 0);

        ConnectionConfiguration config = new ConnectionConfiguration("chat.eu.lol.riotgames.com", 5223, "pvp.net");
        config.setSelfSignedCertificateEnabled(true);
        config.setVerifyChainEnabled(false);

        config.setSocketFactory(new DummySSLSocketFactory());
        config.setSASLAuthenticationEnabled(true);
        config.setSendPresence(true);

        connection = new XMPPConnection(config);

        try {
            connection.connect();
            connection.login(USER, PASSWORD, "home");
        } catch (XMPPException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private MultiUserChat joinChat(String chatRoomJID) {
        try {
            final MultiUserChat muc = new MultiUserChat(connection, chatRoomJID);
            muc.join("LemonBot");
            muc.addMessageListener(new MessageListener(muc));
            return muc;
        } catch (XMPPException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void synchChat(String message, String fromJID) {
        for (MultiUserChat muc : mucs) {
            if (muc == null || fromJID.equals(muc.getRoom())) continue;
            try {
                muc.sendMessage(message);
            } catch (XMPPException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
}
