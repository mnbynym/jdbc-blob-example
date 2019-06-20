package example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <p>
 * BLOB（Binary Large Object）を扱うサンプル・プログラム。
  *   事前に以下テーブルを作成しておくこと。 <br>
 * CREATE TABLE blobsample(id INT PRIMARY KEY, image BLOB);
  *  サンプル画像はプロジェクト直下の「img」フォルダに配置済み
 * </p>
 * 
 * @author M.Yoneyama
 *
 */
public class Main {
	
	// specify your database configurations
	private final static String URL = "jdbc:mariadb://localhost/test";
	private final static String USER = "root";
	private final static String PASS = "mariadb";

	/**
	  *  最初にsaveImageToDbを実行してデータベースに画像を格納。
	  *  次にsaveImageToDbをコメントアウトしてからretrieveImageFromDbをコメントインして実行し、
	  *  データベースから格納されている画像を取り出し（コピーを作成して出力）
	  *  出力された画像ファイルはプロジェクトをF5でリフレッシュしないと表示されない点に注意
	 * @param args
	 */
	public static void main(String[] args) {
		saveImageToDb();
//		retrieveImageFromDb();
	}

	static void saveImageToDb() {

		String sql = "INSERT INTO blobsample VALUES(1, ?)";

		try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
			 PreparedStatement pstmt = conn.prepareStatement(sql);
			 InputStream in = new FileInputStream("img/sample.png");) {

			pstmt.setBlob(1, in);
			pstmt.execute();
			
			System.out.println("Successfully saves the image to the database.");

		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	static void retrieveImageFromDb() {

		String sql = "SELECT image FROM blobsample WHERE id = 1";

		try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
			 Statement stmt = conn.createStatement();
			 ResultSet rs = stmt.executeQuery(sql);
			 InputStream in = rs.getBinaryStream("image");
			 OutputStream out = new FileOutputStream("img/sample_copy.png")) {

			if (rs.next()) {
				int bytes;
				while ((bytes = in.read()) != -1) {
					out.write(bytes);
				}
			}
			
			System.out.println("Successfully retrieves the image from the database and create the copy.");

		} catch (SQLException | IOException e) {
			System.err.println(e.getMessage());
		}
	}
}
