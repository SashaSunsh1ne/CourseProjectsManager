package model;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.File;
import java.util.ArrayList;

public interface Mail {
    int notSeenMessagesCount();
    int inboxMessagesCount();
    void sendMessage(String toEmail, String subject, String body) throws MessagingException;
    ArrayList<Message> getNotSeenInboxMessages();
    ArrayList<Message> getInboxMessages();
    ArrayList<Message> getInboxDialogMessages(String emailTo, String... themes);
    void deleteInboxDialogMessages(String emailTo);
    void loadNotSeenInboxMessage(DbHandler dataBase, File rootFolder);
    void messagesSetSeen();
    SignIn getSignIn();
    String getFromEmail();
}
