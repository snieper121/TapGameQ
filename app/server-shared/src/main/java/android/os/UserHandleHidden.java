package android.os;

public class UserHandleHidden {
    private int userId;
    
    public UserHandleHidden(int userId) {
        this.userId = userId;
    }
    
    public static UserHandleHidden of(int userId) {
        return new UserHandleHidden(userId);
    }
    
    public int getIdentifier() {
        return userId;
    }
}
