

public class ModelUser {

    private String userMail;

    public ModelUser(String userMail) {
        this.userMail = userMail;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public static ModelUser annonymous() {
        return new ModelUser(null);
    }
}
