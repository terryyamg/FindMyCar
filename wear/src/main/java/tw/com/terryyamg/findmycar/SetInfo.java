package tw.com.terryyamg.findmycar;

import android.os.Environment;

public class SetInfo {
	public static String FOLDER_NAME = "findmycar";
	public static String DB_NAME = "find_my_car.db"; // 保存的資料庫檔案名
	public static String PACKAGE_NAME = "tw.com.terryyamg."+FOLDER_NAME;
	public static String DB_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME;// 在手機裡存放資料庫的位置(/data/data/tw.com.terryyamg)
}
