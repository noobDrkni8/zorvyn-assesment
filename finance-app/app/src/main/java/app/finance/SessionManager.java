package app.finance;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "FinanceAppSession";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE = "userRole";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String role) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        editor.commit();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "1"); // Default Admin
    }

    public String getUserRole() {
        return pref.getString(KEY_ROLE, "Admin");
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}
