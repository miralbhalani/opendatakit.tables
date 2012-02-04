package yoonsung.odk.spreadsheet.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A class for managing preferences.
 * 
 * @author hkworden
 */
public class Preferences {
    
    private static final String FILE_NAME = "odktables_preferences";
    private static final String DEFAULT_TABLE_KEY = "defaultTable";
    private static final String PREFERRED_VIEW_TYPE_BASE_KEY = "viewType-";
    
    public class ViewType {
        public static final int TABLE = 0;
        public static final int LIST = 1;
        public static final int LINE_GRAPH = 2;
    }
    
    private final SharedPreferences prefs;
    
    public Preferences(Context context) {
        prefs = context.getSharedPreferences(FILE_NAME, 0);
    }
    
    public long getDefaultTableId() {
        return prefs.getLong(DEFAULT_TABLE_KEY, -1);
    }
    
    public void setDefaultTableId(long tableId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(DEFAULT_TABLE_KEY, tableId);
        editor.commit();
    }
    
    public int getPreferredViewType(long tableId) {
        return prefs.getInt(PREFERRED_VIEW_TYPE_BASE_KEY + tableId,
                ViewType.TABLE);
    }
    
    public void setPreferredViewType(long tableId, int type) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFERRED_VIEW_TYPE_BASE_KEY + tableId, type);
        editor.commit();
    }
    
    public void clearTablePreferences(long tableId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PREFERRED_VIEW_TYPE_BASE_KEY + tableId);
        editor.commit();
    }
}
