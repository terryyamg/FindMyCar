package tw.com.terryyamg.findmycar;

import android.content.Context;
import android.content.SharedPreferences;
import static tw.com.terryyamg.findmycar.SetInfo.FOLDER_NAME;

public class Function {
	private SharedPreferences sp;
	private Context context;

	public Function(Context context) {
		this.context = context;
	}

	/* 儲存int */
	public void setInt(String name, int i) {
		sp();
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(name, i);
		editor.commit();
	}

	/* 儲存string */
	public void setString(String name, String s) {
		sp();
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(name, s);
		editor.commit();
	}

	/* 儲存boolean */
	public void setBoolean(String name, Boolean b) {
		sp();
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(name, b);
		editor.commit();
	}

	/* 儲存float */
	public void setFloat(String name, Float f) {
		sp();
		SharedPreferences.Editor editor = sp.edit();
		editor.putFloat(name, f);
		editor.commit();
	}

	/* 儲存long */
	public void setLong(String name, Long f) {
		sp();
		SharedPreferences.Editor editor = sp.edit();
		editor.putLong(name, f);
		editor.commit();
	}

	/* 取出參數 */
	public String getString(String s) {
		sp();
		String value = sp.getString(s, "");
		return value;
	}

	public int getInt(String s) {
		sp();
		int value = sp.getInt(s, 0);
		return value;
	}

	public Boolean getBoolean(String s) {
		sp();
		Boolean value = sp.getBoolean(s, true);

		return value;
	}

	public Float getFloat(String s) {
		sp();
		Float value = sp.getFloat(s, 0);

		return value;
	}

	public Long getLong(String s) {
		sp();
		Long value = sp.getLong(s, 0);

		return value;
	}

	// 移除
	public void removePre(String s) {
		sp();
		SharedPreferences.Editor editor = sp.edit();
		editor.remove(s);
		editor.commit();
	}

	// 清空
	public void deletePre() {
		sp();
		SharedPreferences.Editor editor = sp.edit();
		editor.clear();
		editor.commit();
	}

	public void sp() {
		sp = context.getApplicationContext().getSharedPreferences(FOLDER_NAME,
				android.content.Context.MODE_PRIVATE);

	}
}
