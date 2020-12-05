package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.sqlite.JDBC;

import javax.mail.PasswordAuthentication;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class DbHandler {
    private static final String CON_STR = "jdbc:sqlite:C:/Users/sanor/IdeaProjects/CourseProjectsManager/src/model/DataBase";
    private static DbHandler instance = null;
    private Connection connection;

    public static synchronized DbHandler getInstance() throws SQLException {
        if (instance == null)
            instance = new DbHandler();
        return instance;
    }

    private DbHandler() throws SQLException {
        DriverManager.registerDriver(new JDBC());
        this.connection = DriverManager.getConnection(CON_STR);
    }

    Student getStudent(String emailAddress) {
        try (Statement statement = this.connection.createStatement()) {
            Student student = null;
            ResultSet resultSet = statement.executeQuery("SELECT id, personal, emailAddress, folderPath, id_stage, id_status, fileCount FROM Student where emailAddress = '" + emailAddress + "'");
            while (resultSet.next()) {
                student = new Student(resultSet.getInt("id"),
                        resultSet.getString("personal"),
                        resultSet.getString("emailAddress"),
                        resultSet.getString("folderPath"),
                        resultSet.getInt("id_stage"),
                        resultSet.getInt("id_status"),
                        resultSet.getInt("fileCount"));
            }
            return student;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ObservableList<Student> getAllStudents() {
        try (Statement statement = this.connection.createStatement()) {
            ObservableList<Student> students = FXCollections.observableArrayList();
            ResultSet resultSet = statement.executeQuery("SELECT id, personal, emailAddress, folderPath, id_stage, id_status, fileCount FROM Student");
            while (resultSet.next()) {
                students.add(new Student(resultSet.getInt("id"),
                        resultSet.getString("personal"),
                        resultSet.getString("emailAddress"),
                        resultSet.getString("folderPath"),
                        resultSet.getInt("id_stage"),
                        resultSet.getInt("id_status"),
                        resultSet.getInt("fileCount")));
            }
            return students;

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    void addStudent(Student student) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO Student(`personal`, `emailAddress`, `folderPath`, `id_stage`, `id_status`) " +
                        "VALUES(?, ?, ?, ?, ?)")) {
            statement.setObject(1, student.getPersonal());
            statement.setObject(2, student.getEmailAddress());
            statement.setObject(3, student.getFolderPath());
            statement.setObject(4, student.getStageInt());
            statement.setObject(5, student.getStatusInt());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteStudent(String emailAddress) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM Student WHERE emailAddress = ?")) {
            statement.setObject(1, emailAddress);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStudent(Student student) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "UPDATE Student SET `personal` = ?, `folderPath` = ?, `id_stage` = ?, `id_status` = ?, fileCount = ? WHERE `emailAddress` = ?;")) {
            statement.setObject(1, student.getPersonal());
            statement.setObject(2, student.getFolderPath());
            statement.setObject(3, student.getStageInt());
            statement.setObject(4, student.getStatusInt());
            statement.setObject(5, student.getFileCount());
            statement.setObject(6, student.getEmailAddress());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void signIn(SignIn user) {
        clearSignIn();
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO SignIn(`username`, `password`, `personal`) " +
                        "VALUES(?, ?, ?)")) {
            statement.setObject(1, user.getEmail());
            statement.setObject(2, user.getPassword());
            statement.setObject(3, user.getPersonal());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearSignIn() {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "DELETE FROM SignIn")) {
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isLogged() {
        ArrayList<PasswordAuthentication> login = new ArrayList<>();
        try (Statement statement = this.connection.createStatement()) {
            login = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery("SELECT username, password FROM SignIn");
            while (resultSet.next()) {
                login.add(new PasswordAuthentication(resultSet.getString("username"),
                        resultSet.getString("password")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return login.size() > 0;
    }

    public SignIn getLogin() {
        SignIn signIn = new SignIn();
        try (Statement statement = this.connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT username, password, personal FROM SignIn");
            while (resultSet.next()) {
                signIn.setEmail(resultSet.getString("username"));
                signIn.setPassword(resultSet.getString("password"));
                signIn.setPersonal(resultSet.getString("personal"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return signIn;
    }

    public void changeDir(File file) {
        ObservableList<Student> students = getAllStudents();
        students.forEach(student -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "UPDATE Student SET `folderPath` = ? WHERE `emailAddress` = ?;")) {
                statement.setObject(1, file.getPath() + "\\" + student.getEmailAddress());
                statement.setObject(2, student.getEmailAddress());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}