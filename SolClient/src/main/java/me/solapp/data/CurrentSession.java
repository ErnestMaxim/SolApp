package me.solapp.data;

public class CurrentSession {
    private static int userId;
    private static boolean isInitialized = false;

    public static int getUserId() {
        if (!isInitialized) {
            System.out.println("WARNING: Accessing userId before initialization");
            return 0;
        }
        return userId;
    }

    public static void setUserId(int id) {
        userId = id;
        isInitialized = true;
        System.out.println("DEBUG: Session initialized with user ID: " + id);
    }

    public static boolean isLoggedIn() {
        return isInitialized && userId > 0;
    }

    public static void clearSession() {
        userId = 0;
        isInitialized = false;
        System.out.println("DEBUG: Session cleared");
    }
}
