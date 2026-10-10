package homework;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/** 泛型 + 反射 + 注解 + JDBC 实现的简单 ORM 工具。 */
public class JDBCTool {

    /** 方法 1：一条记录转换成一个对象，多条记录转换成 List<T>。 */
    public static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz) throws Exception {
        List<T> list = new ArrayList<T>();
        List<Field> fields = getColumnFields(clazz);
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            // 调用 JavaBean 的 public 无参构造方法。
            T obj = clazz.getConstructor().newInstance();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnLabel(i);
                for (Field field : fields) {
                    Column column = field.getAnnotation(Column.class);
                    if (column.name().equalsIgnoreCase(columnName)) {
                        // 字段已经通过 setAccessible(true) 设置为可访问。
                        field.set(obj, readValue(rs, i, field.getType()));
                        break;
                    }
                }
            }
            list.add(obj);
        }
        // rs 由调用者创建，因此由调用者关闭。
        return list;
    }

    /** 方法 2：保存对象，返回数据库实际插入的记录数。 */
    public static <T> int save(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        List<Field> fields = getColumnFields(clazz);
        checkId(obj, getPrimaryKey(fields));
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(getTableName(clazz)).append(" (");
        StringBuilder placeholders = new StringBuilder();
        List<Object> values = new ArrayList<Object>();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sql.append(", ");
                placeholders.append(", ");
            }
            Field field = fields.get(i);
            sql.append(getColumnName(field));
            placeholders.append("?");
            values.add(field.get(obj));
        }
        sql.append(") VALUES (").append(placeholders).append(")");
        return executeUpdate(sql.toString(), values, connection);
    }

    /** 方法 3：按照主键更新其他字段，主键本身不修改。 */
    public static <T> int update(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        List<Field> fields = getColumnFields(clazz);
        Field primaryKey = getPrimaryKey(fields);
        Object id = checkId(obj, primaryKey);
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(getTableName(clazz)).append(" SET ");
        List<Object> values = new ArrayList<Object>();
        for (Field field : fields) {
            if (field.equals(primaryKey)) {
                continue;
            }
            if (!values.isEmpty()) {
                sql.append(", ");
            }
            sql.append(getColumnName(field)).append(" = ?");
            values.add(field.get(obj));
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("没有可更新的非主键字段");
        }
        sql.append(" WHERE ").append(getColumnName(primaryKey)).append(" = ?");
        values.add(id);
        return executeUpdate(sql.toString(), values, connection);
    }

    /** 方法 4：只按照对象的主键删除记录，不要求其他属性有值。 */
    public static <T> int delete(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        Field primaryKey = getPrimaryKey(getColumnFields(clazz));
        List<Object> values = new ArrayList<Object>();
        values.add(checkId(obj, primaryKey));
        String sql = "DELETE FROM " + getTableName(clazz)
                + " WHERE " + getColumnName(primaryKey) + " = ?";
        return executeUpdate(sql, values, connection);
    }

    /** 方法 5：按照主键查询，查不到时返回 null。 */
    public static <T> T getOneById(String id, Class<T> clazz, Connection connection) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("查询主键不能为空");
        }
        Field primaryKey = getPrimaryKey(getColumnFields(clazz));
        String sql = "SELECT * FROM " + getTableName(clazz)
                + " WHERE " + getColumnName(primaryKey) + " = ?";
        List<T> list = query(sql, clazz, connection, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /** 辅助方法：查询一张表的全部对象。 */
    public static <T> List<T> getAll(Class<T> clazz, Connection connection) throws Exception {
        return query("SELECT * FROM " + getTableName(clazz), clazz, connection);
    }

    /** 辅助方法：统一查询入口，参数通过 ? 占位符绑定。 */
    public static <T> List<T> query(String sql, Class<T> clazz,
                                  Connection connection, Object... params) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            ResultSet rs = statement.executeQuery();
            try {
                return resultSetToList(rs, clazz);
            } finally {
                rs.close();
            }
        } finally {
            statement.close();
        }
    }

    /** 辅助方法：统一执行 INSERT、UPDATE、DELETE。 */
    private static int executeUpdate(String sql, List<Object> values,
                                     Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            for (int i = 0; i < values.size(); i++) {
                statement.setObject(i + 1, values.get(i));
            }
            return statement.executeUpdate();
        } finally {
            statement.close();
        }
        // connection 由外部传入，工具方法不关闭它。
    }

    /** 只处理加了 @Column 的普通成员变量。 */
    private static List<Field> getColumnFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Column.class)) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("实体类必须包含 @Column 字段");
        }
        return fields;
    }

    private static Field getPrimaryKey(List<Field> fields) {
        Field primaryKey = null;
        for (Field field : fields) {
            if (field.getAnnotation(Column.class).isPrimaryKey()) {
                if (primaryKey != null) {
                    throw new IllegalArgumentException("本作业只支持一个主键");
                }
                primaryKey = field;
            }
        }
        if (primaryKey == null) {
            throw new IllegalArgumentException("必须用 @Column 标记主键");
        }
        return primaryKey;
    }

    private static Object checkId(Object obj, Field primaryKey) throws IllegalAccessException {
        Object id = primaryKey.get(obj);
        if (id == null || id.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("主键不能为空，请先设置 id");
        }
        return id;
    }

    /** 表名约定：Student 对应 student，College 对应 college。 */
    private static String getTableName(Class<?> clazz) {
        return "`" + clazz.getSimpleName().toLowerCase(Locale.ROOT) + "`";
    }

    private static String getColumnName(Field field) {
        String name = field.getAnnotation(Column.class).name();
        // 反引号保护 SQL 标识符，例如与关键字重名的字段。
        return "`" + name.replace("`", "``") + "`";
    }

    /** 使用 JDBC 的类型化读取，避免 BOOLEAN 被驱动返回为 Integer 等问题。 */
    private static Object readValue(ResultSet rs, int index, Class<?> type) throws SQLException {
        if (type == String.class) {
            return rs.getString(index);
        }
        if (type == Integer.class) {
            int value = rs.getInt(index);
            return rs.wasNull() ? null : Integer.valueOf(value);
        }
        if (type == Boolean.class) {
            boolean value = rs.getBoolean(index);
            return rs.wasNull() ? null : Boolean.valueOf(value);
        }
        if (type == Date.class) {
            return rs.getDate(index);
        }
        if (type == BigDecimal.class) {
            return rs.getBigDecimal(index);
        }
        throw new IllegalArgumentException("本作业未支持的属性类型：" + type.getName());
    }

    public static Connection getConnection() throws Exception {
        return openConnection("db.url");
    }

    /** 创建 DateTest 前，先连接 MySQL 服务器。 */
    public static Connection getServerConnection() throws Exception {
        return openConnection("db.serverUrl");
    }

    private static Connection openConnection(String urlKey) throws Exception {
        Properties properties = new Properties();
        InputStream input = JDBCTool.class.getResourceAsStream("/db.properties");
        if (input == null) {
            throw new IllegalStateException("找不到 db.properties");
        }
        try {
            properties.load(input);
        } finally {
            input.close();
        }
        File localFile = new File("db-local.properties");
        if (localFile.isFile()) {
            input = new FileInputStream(localFile);
            try {
                properties.load(input);
            } finally {
                input.close();
            }
        }
        Class.forName(properties.getProperty("db.driver"));
        return DriverManager.getConnection(properties.getProperty(urlKey),
                properties.getProperty("db.user"), properties.getProperty("db.password"));
    }
}
