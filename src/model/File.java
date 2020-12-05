package model;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;

public class File {

    private MimeBodyPart mimeBodyPart;

    File(MimeBodyPart mimeBodyPart) {
        this.mimeBodyPart = mimeBodyPart;
    }

    String getFileName() {
        String file = null;
        try {
            file = mimeBodyPart.getFileName();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return  file;
    }

    void saveFile(String folderPath) {
        try {
            mimeBodyPart.saveFile(folderPath + java.io.File.separator + mimeBodyPart.getFileName());
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        String file = null;
        try {
            file = mimeBodyPart.getFileName();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return file;
    }
}
