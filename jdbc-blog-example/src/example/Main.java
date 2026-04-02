package example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * BLOB（Binary Large Object）を扱うJava 26推奨サンプル・プログラム。
 * 仮想スレッド、NIO.2、およびStream転送APIを利用。
 * 以下Java26に対応した変更点
 * 仮想スレッド (Thread.ofVirtual):
 * DBやファイル操作は「ブロッキングI/O」であり、仮想スレッド内で実行することで、
 * OSスレッドを無駄に占有せず、リソース効率を最大化。
 * 
 * NIO.2 (java.nio.file.Path/Files):
 * FileInputStream 等の古いクラスを排し、現代的な Path と Filesを採用。
 * ファイルパスの扱いが安全になり、ディレクトリ作成なども容易。
 * 
 * Files.copy(InputStream, Path, ...):
 * 自分で byte[] バッファを作成して while ループを回す必要がない。
 * 内部で最適に実装されており、コードが極めて簡潔で読みやすい。
 * 
 * executeUpdate():
 * 単なる execute() ではなく、更新系（INSERT/UPDATE/DELETE）
 * に適したメソッドを明示的に使用。
 * 
 * ディレクトリの自動作成:
 * Files.createDirectories() を追加し、
 * 出力先フォルダが存在しない場合のエラーを防止。
 * 
 * リソース管理のネスト:
 * ResultSet の後に InputStream を取得する構造を維持しつつ、Files.copy で安全に処理を完結。 
 */
public class Main {
    
    // DB接続設定（実務では環境変数や設定ファイル管理を推奨）
    private final static String URL = "jdbc:mariadb://localhost/test";
    private final static String USER = "root";
    private final static String PASS = "mariadb";
    
    // ファイルパスの定義
    private final static Path INPUT_PATH = Path.of("img/sample.png");
    private final static Path OUTPUT_PATH = Path.of("img/sample_copy.png");

    /**
     * メインメソッド。
     * ブロッキングI/Oを伴うため、仮想スレッド上で実行することでスケーラビリティを確保。
     */
    public static void main(String[] args) {
        Thread.ofVirtual().start(() -> {
            saveImageToDb();
            retrieveImageFromDb();
        }).join(); // サンプルのため終了を待機
    }

    /**
     * 画像ファイルをDBに保存。
     * Files.newInputStream と PreparedStatement を使用。
     */
    static void saveImageToDb() {
        final String sql = "INSERT INTO blobsample (id, image) VALUES (1, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             InputStream in = Files.newInputStream(INPUT_PATH)) {

            pstmt.setBlob(1, in);
            pstmt.executeUpdate();
            
            System.out.println("Successfully saved the image to the database.");

        } catch (SQLException | IOException e) {
            // 実務ではロガー（SLF4J等）を使用
            e.printStackTrace();
        }
    }
    
    /**
     * DBから画像を取得し、ファイルとして書き出す。
     * Files.copy を用いて、ストリームからパスへ直接転送。
     */
    static void retrieveImageFromDb() {
        final String sql = "SELECT image FROM blobsample WHERE id = 1";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                try (InputStream in = rs.getBinaryStream("image")) {
                    if (in != null) {
                        // 出力先ディレクトリの存在確認と作成
                        if (OUTPUT_PATH.getParent() != null) {
                            Files.createDirectories(OUTPUT_PATH.getParent());
                        }
                        
                        // ストリームの内容を直接ファイルへコピー（バッファループ不要）
                        Files.copy(in, OUTPUT_PATH, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Successfully retrieved the image to: " + OUTPUT_PATH.toAbsolutePath());
                    } else {
                        System.out.println("Image data is NULL in the database.");
                    }
                }
            } else {
                System.out.println("No record found with the specified ID.");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
