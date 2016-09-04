package info.einverne.guesswords.fcm;

/**
 * Created by einverne on 9/4/16.
 */

public class NotificationData {
    public static final String KEY = "KEY";
    private String imageName;
    private int id;
    private String title;
    private String message;
    private String sound;

    public NotificationData(String imageName, int id, String title, String message, String sound) {
        this.imageName = imageName;
        this.id = id;
        this.title = title;
        this.message = message;
        this.sound = sound;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
}
