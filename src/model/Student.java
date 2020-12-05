package model;

public class Student {
    private int id;
    private String personal;
    private String emailAddress;
    private String folderPath;
    private int stage;
    private int status;
    private int fileCount;

    public Student(int id, String personal, String emailAddress, String folderPath, int stage, int status, int fileCount) {
        this.id = id;
        this.personal = personal;
        this.emailAddress = emailAddress;
        this.folderPath = folderPath;
        this.stage = stage;
        this.status = status;
        this.fileCount = fileCount;
    }

    public Student(String personal, String emailAddress, String folderPath, int stage, int status, int fileCount) {
        this.personal = personal;
        this.emailAddress = emailAddress;
        this.folderPath = folderPath;
        this.stage = stage;
        this.status = status;
        this.fileCount = fileCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public int getStageInt() {
        return stage;
    }

    public String getStage() {
        return getStage(stage);
    }

    private String getStage(int stage) {
        if (stage == 1)
            return "задание";
        else if (stage == 2)
            return "разработка программы";
        else if (stage == 3)
            return "оформление пояснительной записки";
        else
            return "1";
    }

    public String getNextStage() {
        int nextStage = 0;
        if (stage != 3)
            nextStage = stage + 1;
        else
            nextStage = stage;
        return getStage(nextStage);
    }

    public int getNextStageInt() {
        int nextStage;
        if (stage != 3)
            nextStage = stage + 1;
        else
            nextStage = stage;
        return nextStage;
    }

    public void setStageString(String stage) {
        switch (stage) {
            case "задание":
                this.stage = 1;
                break;
            case "разработка программы":
                this.stage = 2;
                break;
            case "оформление пояснительной записки":
                this.stage = 3;
                break;
        }
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getStatusInt() {
        return status;
    }

    public String getStatus() {
        return getStatus(status);
    }

    private String getStatus(int status) {
        if (status == 1)
            return "ожидание";
        else if (status == 2)
            return "рецензирование";
        else if (status == 3)
            return "на исправлении";
        else if (status == 4)
            return "завершено";
        else
            return null;
    }

    public String getNextStatus() {
        int nextStatus;
        if (status != 4)
            nextStatus = status + 1;
        else
            nextStatus = status;
        return getStatus(nextStatus);
    }

    public int getNextStatusInt() {
        int nextStatus;
        if (status != 4)
            nextStatus = status + 1;
        else
            nextStatus = status;
        return nextStatus;
    }

    public void setStatusString(String status) {
        switch (status) {
            case "ожидание":
                this.status = 1;
                break;
            case "рецензирование":
                this.status = 2;
                break;
            case "на исправлении":
                this.status = 3;
                break;
            case "завершено":
                this.status = 4;
                break;
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

}
