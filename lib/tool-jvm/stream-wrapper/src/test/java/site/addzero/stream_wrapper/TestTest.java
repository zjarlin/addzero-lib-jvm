package site.addzero.stream_wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class TestTest {

    private List<Stu> students;

    @BeforeEach
    void setUp() {
        students = Stream.of(
                createStudent("张三0", 0, "180"),
                createStudent("李四0", 0, "190"),
                createStudent("张三1", 0, "181"),
                createStudent("李四1", 0, "191"),
                createStudent("张三2", 0, "182"),
                createStudent("李四2", 0, "192"),
                createStudent("张三3", 0, "183"),
                createStudent("李四3", 0, "193")
        ).collect(Collectors.toList());
    }

    private Stu createStudent(String name, Integer age, String high) {
        Stu stu = new Stu();
        stu.setName(name);
        stu.setAge(age);
        stu.setHigh(high);
        return stu;
    }


    @Test
    void testStreamGenerationLogic() {
        // 测试示例中的流生成逻辑
        List<Stu> collect = Stream.of(0, 1, 2, 3).flatMap(i -> {
            Stu stu = createStudent("张三" + i, 0, "18" + i);
            Stu stu2 = createStudent("李四" + i, 0, "19" + i);
            return Stream.of(stu2, stu);
        }).sorted(Comparator.comparing(Stu::getName))
                .collect(Collectors.toList());

        assertEquals(8, collect.size());
        assertEquals("李四0", collect.get(0).getName());
        assertEquals("李四1", collect.get(1).getName());
        assertEquals("李四2", collect.get(2).getName());
        assertEquals("李四3", collect.get(3).getName());
        assertEquals("张三0", collect.get(4).getName());
        assertEquals("张三1", collect.get(5).getName());
        assertEquals("张三2", collect.get(6).getName());
        assertEquals("张三3", collect.get(7).getName());
    }

    @Test
    void testStreamWrapperQueryInMain() {
        // 测试main方法中的StreamWrapper查询逻辑
        List<Stu> list = StreamWrapper.lambdaquery(students)
                .like(true, Stu::getHigh, "181")
                .or()
                .like(true, Stu::getHigh, "191")
                .negate()
                .list();

        // 应该返回除了high为"181"和"191"以外的所有学生
        assertEquals(6, list.size());
        assertFalse(list.stream().anyMatch(s -> "181".equals(s.getHigh())));
        assertFalse(list.stream().anyMatch(s -> "191".equals(s.getHigh())));

        // 验证包含预期的学生
        assertTrue(list.stream().anyMatch(s -> "180".equals(s.getHigh())));
        assertTrue(list.stream().anyMatch(s -> "190".equals(s.getHigh())));
        assertTrue(list.stream().anyMatch(s -> "182".equals(s.getHigh())));
        assertTrue(list.stream().anyMatch(s -> "192".equals(s.getHigh())));
        assertTrue(list.stream().anyMatch(s -> "183".equals(s.getHigh())));
        assertTrue(list.stream().anyMatch(s -> "193".equals(s.getHigh())));
    }
}
