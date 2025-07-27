package android.os;

public class UserHandleHidden extends UserHandle {
    public UserHandleHidden(int userId) {
        super(userId);
    }
    
    public static UserHandleHidden of(int userId) {
        return new UserHandleHidden(userId);
    }
}
