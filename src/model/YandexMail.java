package model;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class YandexMail implements Mail {

    private static String THEME_WRONG_STAGE = "Неверный выбор этапа";
    private static String THEME_WRONG_FORMAT = "Прикрепленый файл имеет недопустимый формат";
    private static String TEXT_STAGE_IS_ALREADY_COMPLETED = "Этап, указанный вами в сообщении, указан неверно. \n" +
            "Вы уже завершили данный этап. \n" +
            "Можете переходить к следующему этапу.";
    private static String TEXT_YOU_HAVE_NOT_DONE_STAGE = "Этап, указанный вами в сообщении, указан неверно. \n" +
            "Вы не завершили один из предыдущих этапов.";
    private static String TEXT_WRONG_FORMAT = "Файл, который был прикрепен к сообщению имеет недопустимый формат. \n" +
            "Допускаются форматы '.pdf, .docx, .doc, .txt, .c'.";
    private static String TEXT_ANSWERED_ON_DATE = "Отвечено на сообщение, отправленное: ";

    private final String IMAP_host = "imap.yandex.ru";

    private String fromEmail;
    private String password;
    private String personal;

    public YandexMail(String fromEmail, String password, String personal) throws MessagingException {
        this.fromEmail = fromEmail;
        this.password = password;
        this.personal = personal;
        getInbox();
    }

    private Folder getInbox() throws MessagingException {
        Properties props = getInboxProps();
        Authenticator authenticator = getAuthenticator();
        Session session = Session.getDefaultInstance(props, authenticator);
        session.setDebug(false);
        Store store = session.getStore();
        store.connect(IMAP_host, fromEmail, password);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        return inbox;
    }

    private Session createSession(){
        Properties props = getInboxProps();
        Authenticator authenticator = getAuthenticator();
        Session session = Session.getDefaultInstance(props, authenticator);
        session.setDebug(false);
        return session;
    }

    private Properties getSendProps() {
        Properties props = System.getProperties();
        String host = "smtp.yandex.ru";
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", fromEmail);
        props.put("mail.smtp.password", password);
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.quitwait", "false");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.debug", "false");
        return props;
    }

    private Properties getInboxProps() {
        Properties props = new Properties();
        props.put("mail.imap.port", "993");
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.debug", "false");
        return props;
    }

    private Authenticator getAuthenticator() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };
    }

    private void saveFile(Student student, Message message, DbHandler dataBase) throws Exception {
        File folder = new File(student.getFolderPath() + "\\" + student.getStageInt() + "\\" + student.getStatusInt());
        System.out.println(folder.mkdir());
        System.out.println(folder.mkdirs());
        MimeMultipart mp;
        try {
            mp = (MimeMultipart) message.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                model.File file = EmailMessageReader.getFileFromMimeBodyPart((MimeBodyPart) mp.getBodyPart(i));
                if (file != null) {
                    if (file.getFileName().contains(".pdf")
                            || file.getFileName().contains(".docx")
                            || file.getFileName().contains(".doc")
                            || file.getFileName().contains(".txt")
                            || file.getFileName().contains(".c")) {
                        file.saveFile(folder.getPath());
                        student.setFileCount(student.getFileCount() + 1);
                        dataBase.updateStudent(student);
                        File thisFile = new File(folder + "\\" + file.getFileName());
                        System.out.println(thisFile.renameTo(new File(folder.getPath() + "\\" + student.getFileCount() + file.getFileName())));
                    }
                    else
                        throw new Exception();
                }

            }
        }
        catch (ClassCastException | MessagingException | IOException e) { e.printStackTrace(); }
    }

    @Override
    public int notSeenMessagesCount() {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            Folder inbox = getInbox();
            messages.addAll(Arrays.asList(inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false))));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return messages.size();
    }

    @Override
    public int inboxMessagesCount() {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            Folder inbox = getInbox();
            messages.addAll(Arrays.asList(inbox.getMessages()));
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        return messages.size();
    }

    @Override
    public void sendMessage(String toEmail, String subject, String body) throws MessagingException {
        Properties props = getSendProps();
        Session session = Session.getInstance(props);
        session.setDebug(false);
        MimeMessage message = new MimeMessage(session);
        try {
            InternetAddress address = new InternetAddress(fromEmail);
            address.setPersonal(personal);
            message.setFrom(address);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(fromEmail)); //ЭТО ВАЖНО, ТАК КАК ЯНДЕКС ОТПРАВЛЯЕТ СООБЩЕНИЯ ПО SMTP, А ДАННЫЙ ПРОТОКОЛ НЕ ОСТАВЛЯЕТ ПОСЛЕ СЕБЯ КОПИИ СООБЩЕНИЙ. НЕ ДОБАВЛЯЕТ ИХ В ОТПРАВЛЕННЫЕ
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();
            BodyPart textBody = new MimeBodyPart();
            textBody.setContent(body, "text/plain; charset=utf-8");
            multipart.addBodyPart(textBody);
            message.setContent(multipart);
            Transport transport = session.getTransport("smtp");
            transport.connect(props.getProperty("mail.smtp.host"), fromEmail, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<Message> getNotSeenInboxMessages() {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            Folder inbox = getInbox();
            messages.addAll(Arrays.asList(inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false))));
        } catch (MessagingException ignored) { }
        return messages;
    }

    @Override
    public ArrayList<Message> getInboxMessages() {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            Folder inbox = getInbox();
            messages.addAll(Arrays.asList(inbox.getMessages()));
        } catch (MessagingException ignored) { }
        return messages;
    }

    @Override
    public ArrayList<Message> getInboxDialogMessages(String emailTo, String... themes) {
        ArrayList<Message> allMessages = getInboxMessages();
        ArrayList<Message> dialogMessages = new ArrayList<>();
        allMessages.forEach(message -> {
            Address[] addresses = null;
            try { addresses = message.getRecipients(Message.RecipientType.TO); } catch (MessagingException e) { e.printStackTrace(); }
            if (addresses != null) {
                boolean isEmailTo = false;
                boolean isEmailFrom = false;
                for (Address address : addresses) {
                    if (address.toString().equals(emailTo))
                        isEmailTo = true;
                    if (address.toString().equals(fromEmail))
                        isEmailFrom = true;
                }
                if (themes.length > 0) {
                    for (String theme : themes)
                        if ((EmailMessageReader.getFromEmailAddress(message).equals(emailTo) || (isEmailTo && isEmailFrom)) && (EmailMessageReader.getSubject(message).trim().toLowerCase().equals(theme.trim().toLowerCase()))) {
                            try {
                                if (!message.getFlags().contains(Flags.Flag.DELETED) )
                                    dialogMessages.add(message);
                            } catch (MessagingException e) {
                                e.printStackTrace();
                            }
                        }
                } else
                if (EmailMessageReader.getFromEmailAddress(message).equals(emailTo) || (isEmailTo && isEmailFrom)) {
                    try {
                        if (!message.getFlags().contains(Flags.Flag.DELETED))
                            dialogMessages.add(message);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return dialogMessages;
    }

    @Override
    public void deleteInboxDialogMessages(String emailTo) {
        ArrayList<Message> dialogMessages = getInboxDialogMessages(emailTo);
        dialogMessages.forEach(message -> {
            try {
                Message[] inboxMessages = getInbox().getMessages();
                for (Message inboxMessage : inboxMessages) {
                    if (inboxMessage == message) {
                        inboxMessage.setFlag(Flags.Flag.DELETED, true);
                        inboxMessage.setFlag(Flags.Flag.SEEN, true);
                        getInbox().expunge();
                    }
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void loadNotSeenInboxMessage(DbHandler dataBase, File rootFolder) {
        try {
            Folder inbox = getInbox();
            ArrayList<Message> messages = new ArrayList<>(Arrays.asList(inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false))));
            for (Message message : messages) {
                if (EmailMessageReader.getSubject(message).toLowerCase().trim().equals("задание") || EmailMessageReader.getSubject(message).toLowerCase().trim().equals("разработка программы") || EmailMessageReader.getSubject(message).toLowerCase().trim().equals("оформление пояснительной записки")) {
                    Student student = dataBase.getStudent(EmailMessageReader.getFromEmailAddress(message));
                    if (student != null) {
                        String messageStage = EmailMessageReader.getSubject(message);
                        String studentStage = student.getStage();
                        boolean stageEquals = messageStage.toLowerCase().trim().equals(studentStage);
                        if (stageEquals) {
                            if (!student.getStatus().equals("завершено")) {
                                //скачать в существующую папку
                                try {
                                    saveFile(student, message, dataBase);
                                } catch (Exception e) {
                                    sendMessage(student.getEmailAddress(), THEME_WRONG_FORMAT, TEXT_WRONG_FORMAT + "\n\n" + TEXT_ANSWERED_ON_DATE + EmailMessageReader.getDate(message));
                                }
                            } else if (student.getStatus().equals("завершено")) {
                                //отправить сообщение о том, что данный этап завершен
                                sendMessage(student.getEmailAddress(), THEME_WRONG_STAGE, TEXT_STAGE_IS_ALREADY_COMPLETED + "\n\n" + TEXT_ANSWERED_ON_DATE + EmailMessageReader.getDate(message));
                            }
                        } else {
                            if (!student.getStatus().equals("завершено") || !messageStage.toLowerCase().trim().equals(student.getNextStage())) {
                                //отправить сообщение о том, что данный студент не завершил один из предыдущих этапов (student.getStage())
                                sendMessage(student.getEmailAddress(), THEME_WRONG_STAGE, TEXT_YOU_HAVE_NOT_DONE_STAGE + "\n\n" + TEXT_ANSWERED_ON_DATE + EmailMessageReader.getDate(message));
                            } else if (student.getStatus().equals("завершено") && messageStage.toLowerCase().trim().equals(student.getNextStage())) {
                                //изменить этап на новый и скачать файлы в новую папку, изменить в бд
                                student.setStageString(student.getNextStage());
                                student.setStatus(1);
                                dataBase.updateStudent(student);
                                try {
                                    saveFile(student, message, dataBase);
                                } catch (Exception e) {
                                    sendMessage(student.getEmailAddress(), THEME_WRONG_FORMAT, TEXT_WRONG_FORMAT + "\n\n" + TEXT_ANSWERED_ON_DATE + EmailMessageReader.getDate(message));
                                }
                            }
                        }
                    } else {
                        if (!EmailMessageReader.getFromEmailAddress(message).equals(fromEmail)) {
                            String stage = EmailMessageReader.getSubject(message);
                            if (stage.toLowerCase().trim().equals("задание")) {
                                //создать папку, скачать, добавить в бд.
                                String personal = EmailMessageReader.getFromPersonal(message);
                                String emailAddress = EmailMessageReader.getFromEmailAddress(message);
                                File folder = new File(rootFolder.getPath() + "\\" + EmailMessageReader.getFromEmailAddress(message) + "\\");
                                String folderPath = folder.getPath();
                                int studentStage = 1;
                                int studentStatus = 1;
                                int fileCount = 0;
                                dataBase.addStudent(new Student(personal, emailAddress, folderPath, studentStage, studentStatus, fileCount));
                                student = dataBase.getStudent(emailAddress);
                                try {
                                    saveFile(student, message, dataBase);
                                } catch (Exception e) {
                                    sendMessage(student.getEmailAddress(), THEME_WRONG_FORMAT, TEXT_WRONG_FORMAT + "\n\n" + TEXT_ANSWERED_ON_DATE + EmailMessageReader.getDate(message));
                                }
                            } else if (stage.toLowerCase().trim().equals("разработка программы") || stage.toLowerCase().trim().equals("оформление пояснительной записки")) {
                                //отправить сообщение, что студент не сдал первый этап (задание)
                                sendMessage(EmailMessageReader.getFromEmailAddress(message), THEME_WRONG_STAGE, TEXT_YOU_HAVE_NOT_DONE_STAGE + "\n\n" + TEXT_ANSWERED_ON_DATE + EmailMessageReader.getDate(message));
                            }
                        }
                    }
                }
            }
            messagesSetSeen();
        } catch (MessagingException ignored) {
        }
    }

    @Override
    public void messagesSetSeen() {
        Session session = createSession();
        try {
            Store store = session.getStore();
            store.connect(IMAP_host, fromEmail, password); // Подключение к почтовому серверу
            Folder inbox = store.getFolder("INBOX"); // Папка входящих сообщений

            inbox.open(Folder.READ_WRITE); // Открываем папку в режиме для чтения и записи
            ArrayList<Message> messages = new ArrayList<>(Arrays.asList(inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false))));
            for (Message message : messages) message.setFlag(Flags.Flag.SEEN, true);
        } catch (MessagingException ignored) { }
    }

    @Override
    public SignIn getSignIn() {
        return new SignIn(fromEmail, password, personal);
    }

    @Override
    public String getFromEmail() {
        return fromEmail;
    }

    public static String getThemeWrongStage() {
        return THEME_WRONG_STAGE;
    }

    public static void setThemeWrongStage(String themeWrongStage) {
        THEME_WRONG_STAGE = themeWrongStage;
    }

    public static String getThemeWrongFormat() {
        return THEME_WRONG_FORMAT;
    }

    public static void setThemeWrongFormat(String themeWrongFormat) {
        THEME_WRONG_FORMAT = themeWrongFormat;
    }

    public static String getTextStageIsAlreadyCompleted() {
        return TEXT_STAGE_IS_ALREADY_COMPLETED;
    }

    public static void setTextStageIsAlreadyCompleted(String textStageIsAlreadyCompleted) {
        TEXT_STAGE_IS_ALREADY_COMPLETED = textStageIsAlreadyCompleted;
    }

    public static String getTextYouHaveNotDoneStage() {
        return TEXT_YOU_HAVE_NOT_DONE_STAGE;
    }

    public static void setTextYouHaveNotDoneStage(String textYouHaveNotDoneStage) {
        TEXT_YOU_HAVE_NOT_DONE_STAGE = textYouHaveNotDoneStage;
    }

    public static String getTextWrongFormat() {
        return TEXT_WRONG_FORMAT;
    }

    public static void setTextWrongFormat(String textWrongFormat) {
        TEXT_WRONG_FORMAT = textWrongFormat;
    }

    public static String getTextAnsweredOnDate() {
        return TEXT_ANSWERED_ON_DATE;
    }

    public static void setTextAnsweredOnDate(String textAnsweredOnDate) {
        TEXT_ANSWERED_ON_DATE = textAnsweredOnDate;
    }
}

