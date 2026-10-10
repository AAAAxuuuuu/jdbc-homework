package homework;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

/** 运行 schema.sql，创建作业所需的数据库和两张表。 */
public class DatabaseInit {
    public static void main(String[] args) throws Exception {
        Connection connection = JDBCTool.getServerConnection();
        try {
            InputStream input = DatabaseInit.class.getResourceAsStream("/schema.sql");
            if (input == null) {
                throw new IllegalStateException("找不到 schema.sql");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            try {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("--")) {
                        sql.append(line).append('\n');
                    }
                }
                Statement statement = connection.createStatement();
                try {
                    // 此脚本只有简单建库建表语句，可以按分号分开执行。
                    for (String command : sql.toString().split(";")) {
                        if (!command.trim().isEmpty()) {
                            statement.execute(command);
                        }
                    }
                } finally {
                    statement.close();
                }
            } finally {
                reader.close();
            }
        } finally {
            connection.close();
        }
        System.out.println("DateTest 数据库、student 表、college 表已准备好。");
    }
}
