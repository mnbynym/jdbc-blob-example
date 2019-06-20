# jdbc-blob-example
### JavaのJDBCでBLOB型を扱うサンプル･プログラム

画像や動画、PDFなど、データベースに直接バイナリ形式のデータを格納する場合には、データ型としてBLOB型 (Binary Large Object)を使用する。

- [MariaDBのBLOB型ドキュメント](https://mariadb.com/kb/en/library/blob/)
- [MySQL 5.6のBLOB型ドキュメント(日本語)](https://dev.mysql.com/doc/refman/5.6/ja/blob.html)

注意）「Data too long for column」という例外メッセージが表示される場合にはファイル･サイズを確認して適切なデータ型を選択すること。
- TINYBLOB   :     maximum length of 255 bytes  
- BLOB       :     maximum length of 65,535 bytes  
- MEDIUMBLOB :     maximum length of 16,777,215 bytes  
- LONGBLOB   :     maximum length of 4,294,967,295 bytes  

## JDBCを使用したBLOBデータの保存
- 保存するデータを扱うJava側のデータ型は `java.io.InputStream` 型を使用
- ファイルを扱う場合には `InputStream` のサブ・クラスである `FileInputStream` クラスを使用

## JDBCを使用したBLOBデータの取り出し
- 取り出すデータを扱うJava側のデータ型は `java.io.OutputStream` 型を使用
- ファイルとして書き出すのであれば `OutputStream` のサブ・クラスである `FileOutputStream` クラスを使用
- JDBCの `ResultSet` オブジェクトから `OutputStream` 型データを取り出す場合には `getBinaryStream` メソッドを使用
- ループを使用して取得した `byte` 列をファイルに書き出す必要がある

