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
 * BLOB（Binary Large Object）を扱うサンプル・プログラム。
 * 事前に以下テーブルを作成しておくこと。 <br>
 * CREATE TABLE blobsample(id INT PRIMARY KEY, image BLOB);
 * サンプル画像はプロジェクト直下の「img」フォルダに配置しておく。
 * 指摘事項（リソース管理順序、バッファ利用）を反映済み。
 */
public class Main {

    // Specify your database configurations --------------------------
    private final static String URL = "jdbc:mariadb://localhost/test";
    private final static String USER = "root";
    private final static String PASS = "mariadb";
    //----------------------------------------------------------------

    /**
     * 最初にsaveImageToDbを実行してデータベースに画像を格納。
     * 次にsaveImageToDbをコメントアウト
     * 以下retrieveImageFromDbメソッドをコメントインして実行し、
     * データベースから格納されている画像を取り出し（コピーを作成して出力）
     * Eclipseのプロジェクトに表示されない場合にはF5キーでリフレッシュ
     */
    public static void main(String[] args) {
        saveImageToDb();
        // retrieveImageFromDb();
    }

    /**
     * 画像ファイルをDBに保存する。
     */
    static void saveImageToDb() {
        String sql = "INSERT INTO blobsample (id, image) VALUES (1, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             InputStream in = new FileInputStream("img/sample.png")) {

            pstmt.setBlob(1, in);
            pstmt.executeUpdate();
            
            System.out.println("Successfully saved the image to the database.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * DBから画像を取得し、ファイルとして書き出す。
     */
    static void retrieveImageFromDb() {
        String sql = "SELECT image FROM blobsample WHERE id = 1";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             OutputStream out = new FileOutputStream("img/sample_copy.png")) {

            if (rs.next()) {
                // ResultSetの後にInputStreamを取得することで、安全にリソースを扱う
                try (InputStream in = rs.getBinaryStream("image")) {
                    if (in != null) {
                        // 1バイトずつ読み書き（read() / write()）を行うと、
                        // ファイルサイズが大きい場合に処理が低速になる。
                        // バッファを利用して効率的に読み書きを実行
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
            
            System.out.println("Successfully retrieved the image and created the copy.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
