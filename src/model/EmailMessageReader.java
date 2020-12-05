package model;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class EmailMessageReader {

    private static String getTextFromBodyPart(BodyPart bp) throws MessagingException, IOException {
        String text = null;
        if (bp.getFileName() == null && !(bp.getContent() instanceof MimeMultipart) && bp.isMimeType("text/plain")) {
            text = bp.getContent().toString().trim();
        }
        else if (bp.getContent() instanceof MimeMultipart) {
            text = getTextFromMimeMultipart((MimeMultipart) bp.getContent());
        }
        return text;
    }

    private static String getTextFromMimeMultipart (MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String text = null;
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bp = mimeMultipart.getBodyPart(i);
            if (bp.getFileName() == null && !(bp.getContent() instanceof MimeMultipart) && bp.isMimeType("text/plain")) {
                text = bp.getContent().toString().trim();
            }
            else if (bp.getContent() instanceof MimeMultipart)
                text = getTextFromMimeMultipart((MimeMultipart) bp.getContent());
        }
        return text;
    }

    public static File getFileFromMimeBodyPart(MimeBodyPart bp) throws MessagingException, IOException {
        File file = null;
        if (bp.getFileName() != null) {
            file = new File(bp);
        }
        else if (bp.getContent() instanceof MimeMultipart)
            file = getFileFromMimeMultipart((MimeMultipart) bp.getContent());
        return file;
    }

    private static File getFileFromMimeMultipart (MimeMultipart mimeMultipart) throws MessagingException, IOException {
        File file = null;
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            file = getFileFromMimeBodyPart((MimeBodyPart) mimeMultipart.getBodyPart(i));
        }
        return file;
    }

    static String getFromEmailAddress(Message message) {
        String email = null;
        try {
            Address[] addresses = message.getFrom();
            email = addresses == null ? null : ((InternetAddress) addresses[0]).getAddress();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return email;
    }

    static String getFromPersonal(Message message) {
        String personal = null;
        try {
            Address[] addresses = message.getFrom();
            personal = addresses == null ? null : ((InternetAddress) addresses[0]).getPersonal();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return personal;
    }

    static String getSubject(Message message) {
        String subject = null;
        try {
            subject = message.getSubject();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return subject;
    }

    private static String getText(Message message) {
        String text = null;
        MimeMultipart mp;
        try {
            mp = (MimeMultipart) message.getContent();
            for (int j = 0; j < mp.getCount(); j++) {
                text = getTextFromBodyPart(mp.getBodyPart(j));
                if (text != null)
                    break;
            }
        }
        catch (ClassCastException | MessagingException | IOException ignored) { }
        return text;
    }

    private static ArrayList<File> getFiles(Message message) {
        ArrayList<File> files = new ArrayList<>();
        MimeMultipart mp;
        try {
            mp = (MimeMultipart) message.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                File file = getFileFromMimeBodyPart((MimeBodyPart) mp.getBodyPart(i));
                if (file != null)
                    files.add(file);
            }
        }
        catch (ClassCastException | MessagingException | IOException ignored) { }
        return files;
    }

    static Date getDate(Message message) {
        Date date = null;
        try {
            date = message.getSentDate();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getAllMessage(Message message) {
        StringBuilder text = new StringBuilder();
        text.append("------------------------------------------------------\n");
        text.append("От: ").append(getFromPersonal(message)).append(" (").append(getFromEmailAddress(message)).append(")\n");
        text.append("Дата: ").append(getDate(message)).append("\n");
        text.append("Тема: '").append(getSubject(message)).append("'\n");
        text.append("Сообщение: '").append(getText(message)).append("'\n");
        ArrayList<File> files = getFiles(message);
        for (File file : files) {
            text.append("Файл: '").append(file.toString()).append("'\n");
        }
        text.append("------------------------------------------------------\n");
        return text.toString();
    }

    public static String getShortMessage(Message message) {
        StringBuilder text = new StringBuilder();
        if (!getText(message).isEmpty())
            text.append(String.format("[%s] (%s) %s\n", getFromPersonal(message), getSubject(message), getText(message)));
        ArrayList<File> files = getFiles(message);
        for (File file : files) {
            text.append(String.format("[%s] (%s) %s (Вложение)\n", getFromPersonal(message), getSubject(message), file.getFileName()));
        }
        return text.toString();
    }

}
