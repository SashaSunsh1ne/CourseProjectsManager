package controller;

import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import model.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public TextArea textArea;
    public TableView<Student> studentsTable;
    public TableColumn<Student, Integer> ID;
    public TableColumn<Student, String> PERSONAL;
    public TableColumn<Student, String> EMAIL;
    public TableColumn<Student, String> FOLDER;
    public TableColumn<Student, String> STAGE;
    public TableColumn<Student, String> STATUS;
    public TableColumn<Student, String> FILES;
    public TextField toEmail;
    public TextField themeStage;
    public TextArea message;
    private DbHandler dataBase;
    private Mail yandex;
    private boolean signIn;
    private ObservableList<Student> students = null;
    private File rootFolder;

    @Override
    public void initialize( URL location, ResourceBundle resources) {
        rootFolder = new java.io.File("D:\\students\\");

        studentsTable.setOnMouseClicked(e -> {
            try {
                Student student = studentsTable.getSelectionModel().getSelectedItem();
                toEmail.setText(student.getEmailAddress());
                themeStage.setText(student.getStage());
            } catch (NullPointerException ignore) {
            }
        });

        textArea.setEditable(false);
        try {
            dataBase = DbHandler.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        signIn = dataBase.isLogged();
        if (!signIn)
            yandexLoginWindow();
        else {
            SignIn user = dataBase.getLogin();
            try {
                yandex = new YandexMail(user.getEmail(), user.getPassword(), user.getPersonal());
            } catch (MessagingException e) {
                newAlert(Alert.AlertType.ERROR, "Ошибка", "Нестабильное интернет соединение\n" +
                        "или неверно был ввелен логин или пароль");
                signIn = false;
            }
        }

        studentsTable.getItems().clear();
        students = dataBase.getAllStudents();
        ID.setCellValueFactory(new PropertyValueFactory<>("id"));
        PERSONAL.setCellValueFactory(new PropertyValueFactory<>("personal"));
        EMAIL.setCellValueFactory(new PropertyValueFactory<>("emailAddress"));
        FOLDER.setCellValueFactory(new PropertyValueFactory<>("folderPath"));
        STAGE.setCellValueFactory(new PropertyValueFactory<>("stage"));
        STATUS.setCellValueFactory(new PropertyValueFactory<>("status"));
        FILES.setCellValueFactory(new PropertyValueFactory<>("fileCount"));
        studentsTable.setItems(students);
    }

    private void newPersonalWindow(Student student) {
        GridPane grid = newGridScene(400, 150, 3, 3, 30);

        Scene personal = new Scene(grid, grid.getPrefWidth(), grid.getPrefHeight());

        Text sceneTitle = new Text("Введите новые данные");
        sceneTitle.setTextAlignment(TextAlignment.CENTER);
        sceneTitle.setWrappingWidth(200);
        sceneTitle.setFont(new Font(18));
        grid.add(sceneTitle, 1, 0);

        Label personalLbl = new Label("Введите ФИО:");
        grid.add(personalLbl, 0, 1);

        TextField personalTextField = new TextField();
        grid.add(personalTextField, 1, 1);

        Button btn = new Button("Изменить");
        grid.add(btn, 2, 3);

        Stage loginWindow = new Stage();
        loginWindow.setTitle("Вход");
        loginWindow.setScene(personal);
        loginWindow.setResizable(false);
        loginWindow.setOnCloseRequest(event -> signIn = false);

        btn.setOnAction(e -> {
            if (!personalTextField.getText().isEmpty()) {
                student.setPersonal(personalTextField.getText());
                dataBase.updateStudent(student);
                loginWindow.close();
                newAlert(Alert.AlertType.INFORMATION, "Информация", "Вы успешно изменили ФИО студента");
                showAllStudents();
            } else {
                newAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не ввели ФИО");
            }
        });
        loginWindow.showAndWait();
    }

    private Optional<ButtonType> newAlert(Alert.AlertType alertType, String title, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        return alert.showAndWait();
    }

    private GridPane newGridScene(double gridWidth, double gridHeight, int colsCount, int rowsCount, double rowHeight) {
        GridPane grid = new GridPane();
        grid.setPrefHeight(gridHeight);
        grid.setMaxHeight(gridHeight);
        grid.setMinHeight(gridHeight);
        grid.setPrefWidth(gridWidth);
        grid.setMaxWidth(gridWidth);
        grid.setMinWidth(gridWidth);
        grid.setVgap(5);
        grid.setHgap(5);
        if (colsCount == 3) {
            grid.getColumnConstraints().add(new ColumnConstraints(gridWidth / 4, gridWidth / 4, gridWidth / 4, Priority.SOMETIMES, HPos.RIGHT, true));
            grid.getColumnConstraints().add(new ColumnConstraints(gridWidth / 2, gridWidth / 2, gridWidth / 2, Priority.SOMETIMES, HPos.CENTER, true));
            grid.getColumnConstraints().add(new ColumnConstraints(gridWidth / 4, gridWidth / 4, gridWidth / 4, Priority.SOMETIMES, HPos.LEFT, true));
        }
        else if (colsCount == 2) {
            grid.getColumnConstraints().add(new ColumnConstraints(gridWidth * 0.4, gridWidth * 0.4, gridWidth * 0.4, Priority.SOMETIMES, HPos.RIGHT, true));
            grid.getColumnConstraints().add(new ColumnConstraints(gridWidth * 0.6, gridWidth * 0.6, gridWidth * 0.6, Priority.SOMETIMES, HPos.LEFT, true));
        }
        if (rowsCount >= 3) {
            grid.getRowConstraints().add(new RowConstraints(50, -1, -1, Priority.SOMETIMES, VPos.CENTER, true));
            for (int i = 0; i < rowsCount - 2; i++) {
                grid.getRowConstraints().add(new RowConstraints(rowHeight, rowHeight, rowHeight, Priority.SOMETIMES, VPos.CENTER, true));
            }
            grid.getRowConstraints().add(new RowConstraints(15, 15, 15, Priority.SOMETIMES, VPos.CENTER, true));
        }
        return grid;
    }

    private void chooseStageForDialog(Student student) {
        GridPane grid = newGridScene(600, 150,3, 4, 30);

        Scene chooseStage = new Scene(grid, grid.getPrefWidth(), grid.getPrefHeight());

        Text sceneTitle = new Text("Выбор темы сообщений");
        sceneTitle.setTextAlignment(TextAlignment.CENTER);
        sceneTitle.setWrappingWidth(200);
        sceneTitle.setFont(new Font(18));

        grid.add(sceneTitle, 1, 0);

        Label userName = new Label("Этапы:");
        grid.add(userName, 0, 1);

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Все этапы", "Задание", "Разработка программы", "Оформление пояснительной записки");
        choiceBox.setValue("Все этапы");
        grid.add(choiceBox, 1, 1);

        Button btn = new Button("Просмотреть");
        grid.add(btn, 2, 2);

        Stage stageWindow = new Stage();
        stageWindow.setTitle("Выбор темы");
        stageWindow.setScene(chooseStage);
        stageWindow.setResizable(false);

        btn.setOnAction(e -> {
            StringBuilder dialog = new StringBuilder();
            ArrayList<Message> dialogMessages = new ArrayList<>();
            switch (choiceBox.getValue()) {
                case "Все этапы":
                    dialogMessages = yandex.getInboxDialogMessages(student.getEmailAddress(), yandex.getFromEmail(), "Задание", "Разработка программы", "Оформление пояснительной записки");
                    break;
                case "Задание":
                    dialogMessages = yandex.getInboxDialogMessages(student.getEmailAddress(), yandex.getFromEmail(), "Задание");
                    break;
                case "Разработка программы":
                    dialogMessages = yandex.getInboxDialogMessages(student.getEmailAddress(), yandex.getFromEmail(), "Разработка программы");
                    break;
                case "Оформление пояснительной записки":
                    dialogMessages = yandex.getInboxDialogMessages(student.getEmailAddress(), yandex.getFromEmail(), "Оформление пояснительной записки");
                    break;
            }
            if (!dialogMessages.isEmpty()) {
                for (Message dialogMessage : dialogMessages)
                    dialog.append(EmailMessageReader.getShortMessage(dialogMessage));
                textArea.setText(dialog.toString());
            } else
                newAlert(Alert.AlertType.INFORMATION, "Информация", "Диалог пуст");
            stageWindow.close();
        });
        stageWindow.showAndWait();
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null)
                for (File file : files) {
                    if (file.isDirectory())
                        deleteDirectory(file);
                    else
                        System.out.println(file.delete());
                }
        }
        return (path.delete());
    }

    public void yandexLoginWindow() {
        if (!signIn) {
            GridPane grid = newGridScene(600, 200, 3, 6, 30);

            Scene login = new Scene(grid, grid.getPrefWidth(), grid.getPrefHeight());

            Text sceneTitle = new Text("Войти в почтовый аккаунт");
            sceneTitle.setTextAlignment(TextAlignment.CENTER);
            sceneTitle.setWrappingWidth(200);
            sceneTitle.setFont(new Font(18));

            grid.add(sceneTitle, 1, 0);

            Label userName = new Label("Username:");
            grid.add(userName, 0, 1);

            TextField userTextField = new TextField();
            grid.add(userTextField, 1, 1);

            Label yandexRu = new Label("@yandex.ru");
            grid.add(yandexRu, 2, 1);

            Label personal = new Label("Фамилия и Имя:");
            grid.add(personal, 0, 2);

            TextField personalTextField = new TextField();
            grid.add(personalTextField, 1, 2);

            Label pw = new Label("Password:");
            grid.add(pw, 0, 3);

            PasswordField pwBox = new PasswordField();
            grid.add(pwBox, 1, 3);

            Button btn = new Button("Войти");
            grid.add(btn, 2, 4);

            Stage loginWindow = new Stage();
            loginWindow.setTitle("Вход");
            loginWindow.setScene(login);
            loginWindow.setResizable(false);
            loginWindow.setOnCloseRequest(event -> signIn = false);

            btn.setOnAction(e -> {
                try {
                    SignIn user = new SignIn(userTextField.getText() + yandexRu.getText(), pwBox.getText(), personalTextField.getText());
                    yandex = new YandexMail(user.getEmail(), user.getPassword(), user.getPersonal());
                    dataBase.signIn(yandex.getSignIn());
                    signIn = true;
                    loginWindow.close();
                    newAlert(Alert.AlertType.INFORMATION, "Информация", "Вы успешно вошли в почтовый аккаунт");
                } catch (MessagingException ex) {
                    signIn = false;
                    newAlert(Alert.AlertType.ERROR, "Ошибка", "Неверный логин / пароль\n" +
                            "или нестабильное интернет соединение");
                }
            });
            loginWindow.showAndWait();
        } else {
            Optional<ButtonType> option = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Вы уже вошли в аккаут электронной почты.\n" +
                    "Вы уверены, что хотите сменить пользователя?");
            if (option.isPresent())
                if (option.get() == ButtonType.OK) {
                    dataBase.clearSignIn();
                    signIn = dataBase.isLogged();
                    yandexLoginWindow();
                }
        }
    }

    public void close() {
        Optional<ButtonType> option = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Вы уверены, что хотите завершить работу программы?\n");
        if (option.isPresent())
            if (option.get() == ButtonType.OK)
                System.exit(0);
    }

    public void inboxCount() {
        if (signIn)
            newAlert(Alert.AlertType.INFORMATION, "Информация", "У вас " + yandex.inboxMessagesCount() + " входящих сообщений.\n" +
                    "Из них " + yandex.notSeenMessagesCount() + " непрочитанных.");
        else
            yandexLoginWindow();
    }

    public void inboxNotSeenCount() {
        if (signIn)
            newAlert(Alert.AlertType.INFORMATION, "Информация", "У вас " + yandex.notSeenMessagesCount() + " непрочитанных сообщений");
        else
            yandexLoginWindow();
    }

    public void showNotSeenMessages() {
        if (signIn) {
            if (yandex.notSeenMessagesCount() > 0) {
                ArrayList<Message> messages = yandex.getNotSeenInboxMessages();
                textArea.setText("");
                for (Message value : messages) {
                    textArea.setText(textArea.getText() + EmailMessageReader.getAllMessage(value));
                    textArea.setText(textArea.getText() + "\n");
                }
            } else
                newAlert(Alert.AlertType.ERROR, "Ошибка", "У вас нет непрочитанных сообщений\n" +
                        "или нестабильное интернет соединение");
        } else {
            yandexLoginWindow();
        }
    }

    public void loadNotSeenMessages() {
        if (signIn) {
            if (yandex.notSeenMessagesCount() > 0) {
                yandex.loadNotSeenInboxMessage(dataBase, rootFolder);
            } else
                newAlert(Alert.AlertType.ERROR, "Ошибка", "У вас нет непрочитанных сообщений\n" +
                        "или нестабильное интернет соединение");
            showAllStudents();
        } else {
            yandexLoginWindow();
        }
    }

    public void showAllStudents() {
        studentsTable.getItems().clear();
        students = dataBase.getAllStudents();
        if (students.isEmpty())
            newAlert(Alert.AlertType.INFORMATION, "Инфортация", "В базе данных нет студентов.");
        else
            studentsTable.setItems(students);
    }

    public void yandexQuit() {
        if (signIn) {
            Optional<ButtonType> option = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Вы уверены, что хотите выйти из почтового аккаунта?");
            if (option.isPresent())
                if (option.get() == ButtonType.OK) {
                    dataBase.clearSignIn();
                    signIn = dataBase.isLogged();
                    yandexLoginWindow();
                }
        } else {
            newAlert(Alert.AlertType.INFORMATION, "Инфортация", "Вы не вошли в почтовый аккаунт");
        }
    }

    public void sendEmail() {
        if (message.getText().isEmpty()) {
            newAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не ввели сообщение");
        } else if (toEmail.getText().isEmpty() || themeStage.getText().isEmpty()) {
            newAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не выбрали студента из таблицы");
        } else if (!signIn) {
            newAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не вошли в почтовый аккаунт (Яндекс почта -> Войти в почту)");
        } else {
            try {
                yandex.sendMessage(toEmail.getText(), themeStage.getText(), message.getText());
                newAlert(Alert.AlertType.INFORMATION, "Информация", "Сообщение успешно отправлено");
            } catch (MessagingException e) {
                newAlert(Alert.AlertType.INFORMATION, "Ошибка", "Сообщение не было отправлено");
            }
        }
    }

    public void deleteStudent() {
        Student student = studentsTable.getSelectionModel().getSelectedItem();
        if (student != null) {
            File file = new File(student.getFolderPath());
            if (file.exists()) {
                Optional<ButtonType> option1 = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Удалить папку студента?");
                if (option1.isPresent())
                    if (option1.get() == ButtonType.OK)
                        System.out.println(deleteDirectory(file));
            }
            Optional<ButtonType> option2 = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Удалить переписку со студентом?");
            if (option2.isPresent())
                if (option2.get() == ButtonType.OK)
                    yandex.deleteInboxDialogMessages(student.getEmailAddress());
            if (new File(student.getFolderPath()).exists() && yandex.getInboxDialogMessages(student.getEmailAddress()).isEmpty()) {
                dataBase.deleteStudent(student.getEmailAddress());
                showAllStudents();
            }
        } else {
            newAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не выбрали студента");
        }
    }

    public void changeDir() {
        Optional<ButtonType> option = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Вы уверены, что хотите изменить директорию для загрузки файлов?\n" +
                "Данная директория изменится для всех существующих студентов");
        if (option.isPresent())
            if (option.get() == ButtonType.OK) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(rootFolder);
                fileChooser.setDialogTitle("Выберите папку");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(false);

                if (fileChooser.showOpenDialog(new JButton()) == JFileChooser.APPROVE_OPTION) {
                    rootFolder = fileChooser.getSelectedFile();
                    if (rootFolder != null) {
                        dataBase.changeDir(rootFolder);
                        showAllStudents();
                    }
                }
            }

    }

    public void openDir() {
        Student student = studentsTable.getSelectionModel().getSelectedItem();
        if (student != null) {
            try {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(new File(student.getFolderPath()));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            newAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не выбрали студента");
        }
    }

    public void changeStatus() {
        if (signIn) {
            Student student = studentsTable.getSelectionModel().getSelectedItem();
            if (student != null) {
                Optional<ButtonType> option = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Вы действительно хотите изменить статус выбранного студента на " + student.getNextStatus() + "?");
                if (option.isPresent()) {
                    if (option.get() == ButtonType.OK) {
                        String curStat = student.getStatus();
                        String nextStat = student.getNextStatus();
                        if (!curStat.trim().toLowerCase().equals(nextStat.trim().toLowerCase())) {
                            student.setStatus(student.getNextStatusInt());
                            if (!nextStat.trim().toLowerCase().equals("завершено")) {
                                try {
                                    yandex.sendMessage(student.getEmailAddress(), student.getStage(), "Статус этапа '" + student.getStage() + "' изменен на '" + student.getStatus() + "'.");
                                } catch (MessagingException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                String curStage = student.getStage();
                                String nextStage = student.getNextStage();
                                if (!curStage.trim().toLowerCase().equals(nextStage.trim().toLowerCase())) {
                                    try {
                                        yandex.sendMessage(student.getEmailAddress(), student.getStage(), "Статус этапа '" + student.getStage() + "' изменен на '" + student.getStatus() + "'.\n" +
                                                "Можете переходить на этап '" + nextStage + "'.");
                                    } catch (MessagingException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        yandex.sendMessage(student.getEmailAddress(), student.getStage(), "Статус этапа '" + student.getStage() + "' изменен на '" + student.getStatus() + "'.\n" +
                                                "Вы завершили курсовой проект.");
                                    } catch (MessagingException e) {
                                        e.printStackTrace();
                                    }
                                    option = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Вы хотите удалить все данные данного студента? (почту, файлы)");
                                    if (option.isPresent())
                                        if (option.get() == ButtonType.OK) {
                                            File file = new File(student.getFolderPath());
                                            System.out.println(file.delete());

                                        }
                                }
                            }
                            dataBase.updateStudent(student);
                            showAllStudents();
                        }
                    }
                }
            } else {
                newAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не выбрали студента");
            }
        } else
            yandexLoginWindow();
    }

    public void changePersonal() {
        Student student = studentsTable.getSelectionModel().getSelectedItem();
        if (student != null) {
            Optional<ButtonType> option = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Вы действительно хотите изменить персональные данные студента?");
            if (option.isPresent())
                if (option.get() == ButtonType.OK) {
                    newPersonalWindow(student);
                }
        } else {
            newAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не выбрали студента");
        }
    }

    public void getDialogWithStudent() {
        if (signIn) {
            Student student = studentsTable.getSelectionModel().getSelectedItem();
            if (student != null) {
                chooseStageForDialog(student);
            } else
                newAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не выбрали студента");
        } else
            yandexLoginWindow();
    }

    public void changeConstants() {
        GridPane grid = newGridScene(1000, 720, 2, 9, 100);
        grid.getRowConstraints().clear();
        grid.getRowConstraints().add(new RowConstraints(50, -1, -1, Priority.SOMETIMES, VPos.CENTER, true));
        grid.getRowConstraints().add(new RowConstraints(50, 50, 50, Priority.SOMETIMES, VPos.CENTER, true));
        grid.getRowConstraints().add(new RowConstraints(50, 50, 50, Priority.SOMETIMES, VPos.CENTER, true));
        for (int i = 0; i < 4; i++)
            grid.getRowConstraints().add(new RowConstraints(100, 100, 100, Priority.SOMETIMES, VPos.CENTER, true));
        grid.getRowConstraints().add(new RowConstraints(30, 30, 30, Priority.SOMETIMES, VPos.CENTER, true));
        grid.getRowConstraints().add(new RowConstraints(15, 15, 15, Priority.SOMETIMES, VPos.CENTER, true));

        Scene autoMessages = new Scene(grid, grid.getPrefWidth(), grid.getPrefHeight());

        Text sceneTitle = new Text("Здесь вы можете изменить темы и сообщения автоматических ответов студентам");
        sceneTitle.setTextAlignment(TextAlignment.CENTER);
        sceneTitle.setWrappingWidth(300);
        sceneTitle.setFont(new Font(18));

        grid.add(sceneTitle, 1, 0);

        Label THEME_WRONG_STAGE = new Label("Неверный выбор этапа (тема): ");
        THEME_WRONG_STAGE.setPrefHeight(50);
        grid.add(THEME_WRONG_STAGE, 0, 1);

        TextField THEME_WRONG_STAGE1 = new TextField();
        THEME_WRONG_STAGE1.setText(YandexMail.getThemeWrongStage());
        grid.add(THEME_WRONG_STAGE1, 1, 1);

        Label THEME_WRONG_FORMAT = new Label("Неверный формат (тема): ");
        THEME_WRONG_FORMAT.setPrefHeight(50);
        grid.add(THEME_WRONG_FORMAT, 0, 2);

        TextField THEME_WRONG_FORMAT1 = new TextField();
        THEME_WRONG_FORMAT1.setText(YandexMail.getThemeWrongFormat());
        grid.add(THEME_WRONG_FORMAT1, 1, 2);

        Label TEXT_STAGE_IS_ALREADY_COMPLETED = new Label("Этап уже завершен (текст): ");
        TEXT_STAGE_IS_ALREADY_COMPLETED.setPrefHeight(100);
        grid.add(TEXT_STAGE_IS_ALREADY_COMPLETED, 0, 3);

        TextArea TEXT_STAGE_IS_ALREADY_COMPLETED1 = new TextArea();
        TEXT_STAGE_IS_ALREADY_COMPLETED1.setText(YandexMail.getTextStageIsAlreadyCompleted());
        grid.add(TEXT_STAGE_IS_ALREADY_COMPLETED1, 1, 3);

        Label TEXT_YOU_HAVE_NOT_DONE_STAGE = new Label("Не завершен один из предыдущих этапов (текст): ");
        TEXT_YOU_HAVE_NOT_DONE_STAGE.setPrefHeight(100);
        grid.add(TEXT_YOU_HAVE_NOT_DONE_STAGE, 0, 4);

        TextArea TEXT_YOU_HAVE_NOT_DONE_STAGE1 = new TextArea();
        TEXT_YOU_HAVE_NOT_DONE_STAGE1.setText(YandexMail.getTextYouHaveNotDoneStage());
        grid.add(TEXT_YOU_HAVE_NOT_DONE_STAGE1, 1, 4);

        Label TEXT_WRONG_FORMAT = new Label("Неверный формат (текст): ");
        TEXT_WRONG_FORMAT.setPrefHeight(100);
        grid.add(TEXT_WRONG_FORMAT, 0, 5);

        TextArea TEXT_WRONG_FORMAT1 = new TextArea();
        TEXT_WRONG_FORMAT1.setText(YandexMail.getTextWrongFormat());
        grid.add(TEXT_WRONG_FORMAT1, 1, 5);

        Label TEXT_ANSWERED_ON_DATE = new Label("Приписка после сообщения (ответ на + дата): ");
        TEXT_ANSWERED_ON_DATE.setPrefHeight(100);
        grid.add(TEXT_ANSWERED_ON_DATE, 0, 6);

        TextArea TEXT_ANSWERED_ON_DATE1 = new TextArea();
        TEXT_ANSWERED_ON_DATE1.setText(YandexMail.getTextAnsweredOnDate());
        grid.add(TEXT_ANSWERED_ON_DATE1, 1, 6);

        Button btn = new Button("Изменить");
        grid.add(btn, 1, 7);

        Stage changeConstants = new Stage();
        changeConstants.setTitle("Изменение автоматических сообщений");
        changeConstants.setScene(autoMessages);
        changeConstants.setResizable(false);

        btn.setOnAction(e -> {
            Optional<ButtonType> option = newAlert(Alert.AlertType.CONFIRMATION, "Подтверждение", "Вы действительно хотите изменить текст и темы автоматических сообщений?");
            if (option.isPresent())
                if (option.get() == ButtonType.OK) {
                    YandexMail.setTextAnsweredOnDate(TEXT_ANSWERED_ON_DATE1.getText());
                    YandexMail.setTextStageIsAlreadyCompleted(TEXT_STAGE_IS_ALREADY_COMPLETED1.getText());
                    YandexMail.setTextWrongFormat(TEXT_WRONG_FORMAT1.getText());
                    YandexMail.setTextYouHaveNotDoneStage(TEXT_YOU_HAVE_NOT_DONE_STAGE1.getText());
                    YandexMail.setThemeWrongFormat(THEME_WRONG_FORMAT1.getText());
                    YandexMail.setThemeWrongStage(THEME_WRONG_STAGE1.getText());
                    changeConstants.close();
                }
        });
        changeConstants.showAndWait();
    }
}