package tw.com.terryyamg.findmycar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static tw.com.terryyamg.findmycar.SetInfo.DB_NAME;
import static tw.com.terryyamg.findmycar.SetInfo.DB_PATH;

public class DBManager {

	private final int BUFFER_SIZE = 400000;
	
	private SQLiteDatabase database;
	private Context context;

	public DBManager(Context context) {
		this.context = context;
	}

	public SQLiteDatabase getDatabase() {
		return database;
	}

	public void setDatabase(SQLiteDatabase database) {
		this.database = database;
	}

	public void openDatabase() {
		this.database = this.openDatabase(DB_PATH + "/databases/" + DB_NAME);
	}

	private SQLiteDatabase openDatabase(String dbfile) {
		
		File picDir = new File(DB_PATH, "databases");
		if (!picDir.exists()) {
			picDir.mkdir();
		}
		
		try {
			if (!(new File(dbfile).exists())) {
				// 判斷資料庫檔案是否存在，若不存在則執行導入，否則直接打開資料庫
				InputStream is = this.context.getResources().openRawResource(
						R.raw.find_my_car); // 欲導入的資料庫
				FileOutputStream fos = new FileOutputStream(dbfile);
				byte[] buffer = new byte[BUFFER_SIZE];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile,
					null);
			return db;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void closeDatabase() {
		this.database.close();

	}
}

