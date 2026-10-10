package homework;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;

/** 控制台操作入口：用户通过菜单操作学生表和学院表。 */
public class Main {
    public static void main(String[] args) throws Exception {
        Connection connection = JDBCTool.getConnection();
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("已连接 DateTest 数据库，欢迎使用作业操作程序。");
            while (true) {
                System.out.println("\n========== 作业主菜单 ==========");
                System.out.println("1. 学生表    2. 学院表    0. 退出程序");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String table = input(scanner, "请选择：");
                if ("0".equals(table)) {
                    break;
                }
                if (!"1".equals(table) && !"2".equals(table)) {
                    System.out.println("请输入 1、2 或 0。");
                    continue;
                }
                try {
                    tableMenu(scanner, connection, "1".equals(table));
                } catch (NoSuchElementException e) {
                    System.out.println("输入已结束。");
                    break;
                }
            }
        } finally {
            connection.close();
            scanner.close();
        }
        System.out.println("程序已退出，数据库连接已关闭。");
    }

    private static void tableMenu(Scanner scanner, Connection connection, boolean studentTable) {
        while (true) {
            System.out.println("\n========== " + (studentTable ? "学生表" : "学院表") + "操作菜单 ==========");
            System.out.println("1. 新增记录    2. 按主键查询    3. 查询全部");
            System.out.println("4. 修改记录    5. 删除记录      0. 返回主菜单");
            String choice = input(scanner, "请选择操作：");
            if ("0".equals(choice)) {
                return;
            }
            try {
                if ("1".equals(choice)) {
                    if (studentTable) {
                        Student student = editStudent(scanner, new Student(), false);
                        student.setId(readNewId(scanner));
                        System.out.println("新增成功，影响行数：" + JDBCTool.save(student, connection));
                        printStudent(student);
                    } else {
                        College college = editCollege(scanner, new College(), false);
                        college.setId(readNewId(scanner));
                        System.out.println("新增成功，影响行数：" + JDBCTool.save(college, connection));
                        printCollege(college);
                    }
                } else if ("2".equals(choice) || "4".equals(choice) || "5".equals(choice)) {
                    String id = required(scanner, "请输入记录的主键 ID：");
                    if (studentTable) {
                        Student student = JDBCTool.getOneById(id, Student.class, connection);
                        if (student == null) {
                            System.out.println("未找到该学生。");
                            continue;
                        }
                        printStudent(student);
                        if ("4".equals(choice)) {
                            editStudent(scanner, student, true);
                            System.out.println("修改完成，影响行数：" + JDBCTool.update(student, connection));
                            printStudent(JDBCTool.getOneById(id, Student.class, connection));
                        } else if ("5".equals(choice) && confirmDelete(scanner)) {
                            System.out.println("删除完成，影响行数：" + JDBCTool.delete(student, connection));
                            System.out.println("删除后查询：" + JDBCTool.getOneById(id, Student.class, connection));
                        }
                    } else {
                        College college = JDBCTool.getOneById(id, College.class, connection);
                        if (college == null) {
                            System.out.println("未找到该学院。");
                            continue;
                        }
                        printCollege(college);
                        if ("4".equals(choice)) {
                            editCollege(scanner, college, true);
                            System.out.println("修改完成，影响行数：" + JDBCTool.update(college, connection));
                            printCollege(JDBCTool.getOneById(id, College.class, connection));
                        } else if ("5".equals(choice) && confirmDelete(scanner)) {
                            System.out.println("删除完成，影响行数：" + JDBCTool.delete(college, connection));
                            System.out.println("删除后查询：" + JDBCTool.getOneById(id, College.class, connection));
                        }
                    }
                } else if ("3".equals(choice)) {
                    if (studentTable) {
                        List<Student> students = JDBCTool.getAll(Student.class, connection);
                        System.out.println("学生列表，共 " + students.size() + " 条：");
                        for (Student student : students) {
                            printStudent(student);
                        }
                    } else {
                        List<College> colleges = JDBCTool.getAll(College.class, connection);
                        System.out.println("学院列表，共 " + colleges.size() + " 条：");
                        for (College college : colleges) {
                            printCollege(college);
                        }
                    }
                } else {
                    System.out.println("请输入菜单中的编号。");
                }
            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                System.out.println("操作失败：" + e.getMessage());
            }
        }
    }

    private static Student editStudent(Scanner scanner, Student student, boolean updating) {
        if (updating) {
            System.out.println("依次输入新值，直接回车保留原值；主键保持不变。");
        }
        String name = updating ? input(scanner, "姓名：") : required(scanner, "姓名（必填）：");
        if (!updating || !name.isEmpty()) student.setName(name);
        String major = input(scanner, "专业：");
        if (!updating || !major.isEmpty()) student.setMajor(major.isEmpty() ? null : major);
        Integer age = readAge(scanner);
        if (!updating || age != null) student.setAge(age);
        Date date = readDate(scanner);
        if (!updating || date != null) student.setEnrollmentDate(date);
        Boolean graduated = readGraduated(scanner);
        if (!updating || graduated != null) student.setGraduated(graduated);
        BigDecimal tuition = readTuition(scanner);
        if (!updating || tuition != null) student.setTuition(tuition);
        return student;
    }

    private static College editCollege(Scanner scanner, College college, boolean updating) {
        if (updating) {
            System.out.println("输入新值，直接回车保留原值；主键保持不变。");
        }
        String name = updating ? input(scanner, "学院名称：") : required(scanner, "学院名称（必填）：");
        if (!updating || !name.isEmpty()) college.setName(name);
        String code = input(scanner, "学院代码：");
        if (!updating || !code.isEmpty()) college.setCode(code.isEmpty() ? null : code);
        return college;
    }

    private static String readNewId(Scanner scanner) {
        while (true) {
            String id = input(scanner, "主键 ID（可自行填写，直接回车自动生成）：");
            if (id.isEmpty()) return UUID.randomUUID().toString();
            if (id.length() <= 36) return id;
            System.out.println("ID 不能超过 36 个字符。");
        }
    }

    private static Integer readAge(Scanner scanner) {
        while (true) {
            String text = input(scanner, "年龄（可留空）：");
            if (text.isEmpty()) return null;
            try {
                int age = Integer.parseInt(text);
                if (age >= 0) return age;
            } catch (NumberFormatException e) {
                // 提示后继续输入。
            }
            System.out.println("年龄请输入非负整数。");
        }
    }

    private static Date readDate(Scanner scanner) {
        while (true) {
            String text = input(scanner, "入学时间（yyyy-MM-dd，可留空）：");
            if (text.isEmpty()) return null;
            try {
                Date date = Date.valueOf(text);
                if (date.toString().equals(text)) return date;
            } catch (IllegalArgumentException e) {
                // 非法格式或日期时继续输入。
            }
            System.out.println("请输入有效日期，例如 2024-09-01。");
        }
    }

    private static Boolean readGraduated(Scanner scanner) {
        while (true) {
            String text = input(scanner, "是否毕业（1/是，0/否，可留空）：");
            if (text.isEmpty()) return null;
            if ("1".equals(text) || "是".equals(text) || "true".equalsIgnoreCase(text)) return true;
            if ("0".equals(text) || "否".equals(text) || "false".equalsIgnoreCase(text)) return false;
            System.out.println("请输入 1、0、是或否。");
        }
    }

    private static BigDecimal readTuition(Scanner scanner) {
        while (true) {
            String text = input(scanner, "学费（可留空，最多两位小数）：");
            if (text.isEmpty()) return null;
            try {
                BigDecimal value = new BigDecimal(text);
                if (value.signum() >= 0 && value.scale() <= 2
                        && value.compareTo(new BigDecimal("99999999.99")) <= 0) return value;
            } catch (NumberFormatException e) {
                // 提示后继续输入。
            }
            System.out.println("学费须为 0 到 99999999.99 的金额，最多两位小数。");
        }
    }

    private static boolean confirmDelete(Scanner scanner) {
        if ("是".equals(input(scanner, "确认删除请输入“是”，其他输入取消："))) return true;
        System.out.println("已取消删除。");
        return false;
    }

    private static String required(Scanner scanner, String prompt) {
        while (true) {
            String value = input(scanner, prompt);
            if (!value.isEmpty()) return value;
            System.out.println("此项不能为空。");
        }
    }

    private static String input(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static void printStudent(Student student) {
        System.out.println("----------------------------------------");
        System.out.println("ID：" + student.getId());
        System.out.println("姓名：" + student.getName() + "，专业：" + student.getMajor() + "，年龄：" + student.getAge());
        System.out.println("入学时间：" + student.getEnrollmentDate() + "，是否毕业："
                + (student.getGraduated() == null ? "未填写" : student.getGraduated() ? "是" : "否")
                + "，学费：" + student.getTuition());
    }

    private static void printCollege(College college) {
        System.out.println("----------------------------------------");
        System.out.println("ID：" + college.getId());
        System.out.println("学院名称：" + college.getName() + "，学院代码：" + college.getCode());
    }
}
