package homework;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

/** 作业演示入口：依次完成学生表和学院表的增、查、改、删。 */
public class Main {
    public static void main(String[] args) throws Exception {
        Connection connection = JDBCTool.getConnection();
        try {
            studentDemo(connection);
            collegeDemo(connection);
        } finally {
            connection.close();
        }
    }

    private static void studentDemo(Connection connection) throws Exception {
        System.out.println("========== 学生表增删改查 ==========");
        Student student = new Student(UUID.randomUUID().toString(), "张三", "软件工程", 20,
                Date.valueOf("2024-09-01"), false, new BigDecimal("6800.00"));
        Student temporary = new Student(UUID.randomUUID().toString(), "李四", "计算机科学与技术", 21,
                Date.valueOf("2023-09-01"), false, new BigDecimal("7200.50"));

        System.out.println("新增张三，影响行数：" + JDBCTool.save(student, connection));
        System.out.println("新增李四，影响行数：" + JDBCTool.save(temporary, connection));
        Student found = JDBCTool.getOneById(student.getId(), Student.class, connection);
        System.out.println("按主键查询：" + found);
        List<Student> students = JDBCTool.getAll(Student.class, connection);
        System.out.println("查询学生列表，共 " + students.size() + " 条：");
        for (Student item : students) {
            System.out.println(item);
        }

        student.setAge(21);
        student.setGraduated(true);
        student.setTuition(new BigDecimal("7000.00"));
        System.out.println("修改张三，影响行数：" + JDBCTool.update(student, connection));
        System.out.println("修改后查询：" + JDBCTool.getOneById(student.getId(), Student.class, connection));

        // 删除只需要主键，演示时删除本次新增的李四。
        Student toDelete = new Student();
        toDelete.setId(temporary.getId());
        System.out.println("删除李四，影响行数：" + JDBCTool.delete(toDelete, connection));
        System.out.println("删除后查询：" + JDBCTool.getOneById(temporary.getId(), Student.class, connection));
    }

    private static void collegeDemo(Connection connection) throws Exception {
        System.out.println("========== 学院表增删改查 ==========");
        College college = new College(UUID.randomUUID().toString(), "计算机学院", "CS");
        College temporary = new College(UUID.randomUUID().toString(), "外国语学院", "FL");

        System.out.println("新增计算机学院，影响行数：" + JDBCTool.save(college, connection));
        System.out.println("新增外国语学院，影响行数：" + JDBCTool.save(temporary, connection));
        System.out.println("按主键查询：" + JDBCTool.getOneById(college.getId(), College.class, connection));
        List<College> colleges = JDBCTool.getAll(College.class, connection);
        System.out.println("查询学院列表，共 " + colleges.size() + " 条：");
        for (College item : colleges) {
            System.out.println(item);
        }

        college.setName("计算机与信息学院");
        college.setCode("CIS");
        System.out.println("修改计算机学院，影响行数：" + JDBCTool.update(college, connection));
        System.out.println("修改后查询：" + JDBCTool.getOneById(college.getId(), College.class, connection));

        College toDelete = new College();
        toDelete.setId(temporary.getId());
        System.out.println("删除外国语学院，影响行数：" + JDBCTool.delete(toDelete, connection));
        System.out.println("删除后查询：" + JDBCTool.getOneById(temporary.getId(), College.class, connection));
        System.out.println("演示结束。两张表各保留一条本次新增并修改后的记录。");
    }
}
