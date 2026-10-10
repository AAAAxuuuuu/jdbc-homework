package homework;

import java.math.BigDecimal;
import java.sql.Date;

/** 学生 JavaBean：属性私有、public 无参构造、完整 getter/setter。 */
public class Student {
    @Column(name = "id", isPrimaryKey = true)
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "major")
    private String major;
    @Column(name = "age")
    private Integer age;
    @Column(name = "enrollment_date")
    private Date enrollmentDate;
    @Column(name = "graduated")
    private Boolean graduated;
    @Column(name = "tuition")
    private BigDecimal tuition;

    public Student() {
    }

    public Student(String id, String name, String major, Integer age,
                   Date enrollmentDate, Boolean graduated, BigDecimal tuition) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.age = age;
        this.enrollmentDate = enrollmentDate;
        this.graduated = graduated;
        this.tuition = tuition;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMajor() {
        return major;
    }
    public void setMajor(String major) {
        this.major = major;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public Date getEnrollmentDate() {
        return enrollmentDate;
    }
    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    public Boolean getGraduated() {
        return graduated;
    }
    public void setGraduated(Boolean graduated) {
        this.graduated = graduated;
    }
    public BigDecimal getTuition() {
        return tuition;
    }
    public void setTuition(BigDecimal tuition) {
        this.tuition = tuition;
    }

    @Override
    public String toString() {
        return "Student{id='" + id + "', name='" + name + "', major='" + major
                + "', age=" + age + ", enrollmentDate=" + enrollmentDate
                + ", graduated=" + graduated + ", tuition=" + tuition + "}";
    }
}
