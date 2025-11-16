package site.addzero.mybatis.auto_wrapper;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class PackField {
    protected Field field;

    protected String fieldName;

    public PackField(Field field) {
        this.field = field;
        this.fieldName = field.getName();
    }

    abstract <T, R> List<ColumnInfo<T, R>> getColumnInfoList(Object object, BiFunction<Class<T>, String, R> columnProcess);

    abstract Annotation getAnnotation();

    abstract String getGroupName();

    abstract boolean getOuterJoin();

    abstract boolean getInnerJoin();

    Object getValue(Object object) {
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected <T, R> List<ColumnInfo<T, R>> createListColumnInfo(Where[] ans, Object value, BiFunction<Class<T>, String, R> columnProcess) {
        final List<ColumnInfo<T, R>> list = new ArrayList<>();
        for (Where where : ans) {
            ColumnInfo<T, R> columnInfo;
            if (where == null) {
                columnInfo = new ColumnInfo<>("=", fieldName, value, false, columnProcess);
            } else {
                final String column = where.column();
                columnInfo = new ColumnInfo<>(where.value(), StringUtils.isBlank(column) ? fieldName : column, value, where.join(), columnProcess);
            }
            list.add(columnInfo);
        }
        return list;
    }

}

class WherePackField extends PackField {
    private final Where an;

    public WherePackField(Field field) {
        super(field);
        an = field.getAnnotation(Where.class);
    }

    @Override
    <T, R> List<ColumnInfo<T, R>> getColumnInfoList(Object object, BiFunction<Class<T>, String, R> columnProcess) {
        final Where annotation = field.getAnnotation(Where.class);
        return createListColumnInfo(new Where[]{annotation}, getValue(object), columnProcess);
    }

    @Override
    Where getAnnotation() {
        return an;
    }

    @Override
    String getGroupName() {
        return fieldName;

    }

    @Override
    boolean getOuterJoin() {
        return false;
    }

    @Override
    boolean getInnerJoin() {
        return false;
    }
}

class WheresPackField extends PackField {
    private final Wheres an;

    public WheresPackField(Field field) {
        super(field);
        an = field.getAnnotation(Wheres.class);
    }

    @Override
    <T, R> List<ColumnInfo<T, R>> getColumnInfoList(Object object, BiFunction<Class<T>, String, R> columnProcess) {

        final Where[] ans = this.getAnnotation().value();
        final Object value = getValue(object);
        return createListColumnInfo(ans, value, columnProcess);
    }

    @Override
    Wheres getAnnotation() {
        return an;
    }

    @Override
    String getGroupName() {
        return Optional.of(field.getAnnotation(Wheres.class).group()).filter(StringUtils::isNotBlank).orElse(fieldName);
    }

    @Override
    boolean getOuterJoin() {
        return this.getAnnotation().outerJoin();
    }

    @Override
    boolean getInnerJoin() {
        return this.getAnnotation().innerJoin();
    }
}

class GroupInfoBuilder<T, R> {

    Class<T> clazz;

    BiFunction<Class<T>, Class<?>, Set<Field>> packFieldPostProcess;

    BiFunction<Class<T>, String, R> columnProcess;

    public GroupInfoBuilder(Class<T> clazz, BiFunction<Class<T>, Class<?>, Set<Field>> packFieldPostProcess, BiFunction<Class<T>, String, R> columnProcess) {
        this.clazz = clazz;
        this.packFieldPostProcess = packFieldPostProcess;
        this.columnProcess = columnProcess;
    }

    private static Function<Field, PackField> fieldConvertPack(Class<? extends Annotation>[] annotations) {
        return field -> {
            final Optional<Class<? extends Annotation>> first = Stream.of(annotations)
                    .filter(field::isAnnotationPresent).findFirst();
            return first.map(an -> {
                if (an == Wheres.class) {
                    return new WheresPackField(field);
                } else {
                    return new WherePackField(field);
                }
            }).orElse(null);
        };
    }

    @SuppressWarnings("unchecked")
    public static List<PackField> findFieldsToPackField(Set<Field> eqField, Class<?> clazz) {
        if (clazz == null) return new ArrayList<>();
        final List<Field> list = new ArrayList<>();
        while (clazz != null) {
            final Field[] declaredFields = clazz.getDeclaredFields();
            list.addAll(Arrays.asList(declaredFields));
            clazz = clazz.getSuperclass();
        }
        //----------
        final Class<? extends Annotation>[] classes = new Class[]{Wheres.class, Where.class};
        final Set<Field> collect = list.stream().filter(field -> Stream.of(classes)
                .anyMatch(field::isAnnotationPresent)).collect(Collectors.toSet());

        eqField.removeAll(collect);

        final List<PackField> retList = collect.stream()
                .map(fieldConvertPack(classes)).filter(Objects::nonNull).collect(Collectors.toList());
        retList.addAll(eqField.stream().map(WherePackField::new).collect(Collectors.toList()));
        return retList;
    }

    public List<WheresGroupInfo<T, R>> buildGroupInfo(Object obj) {

        final Class<?> dtoClass = obj.getClass();
        Set<Field> eqFields = packFieldPostProcess.apply(clazz, dtoClass);

        final List<PackField> packFieldList = findFieldsToPackField(eqFields, dtoClass);

        final Map<String, WheresGroupInfo<T, R>> map = new HashMap<>();
        List<WheresGroupInfo<T, R>> list = new ArrayList<>();
        packFieldList.forEach(packField -> {
            final String groupName = packField.getGroupName();
            final boolean outerJoin = packField.getOuterJoin();
            final boolean innerJoin = packField.getInnerJoin();

            final WheresGroupInfo<T, R> wheresGroupInfo = map.computeIfAbsent(groupName, (k) -> {
                final WheresGroupInfo<T, R> groupInfo = new WheresGroupInfo<>(groupName, outerJoin, innerJoin);
                list.add(groupInfo);
                return groupInfo;
            });

            wheresGroupInfo.outerJoin = outerJoin || wheresGroupInfo.outerJoin;
            wheresGroupInfo.innerJoin = innerJoin || wheresGroupInfo.innerJoin;

            final List<ColumnInfo<T, R>> columnInfoList = packField.getColumnInfoList(obj, columnProcess);
            for (ColumnInfo<T, R> columnInfo : columnInfoList) {
                final String column = columnInfo.column;
                final ColumnGroupInfo<T, R> columnGroupInfo = wheresGroupInfo.columnGroupInfoMap.computeIfAbsent(column, (k) -> new ColumnGroupInfo<>());
                columnGroupInfo.columnInfos.add(columnInfo);
            }
        });
        list.sort(WheresGroupInfo::compareTo);
        return list;
    }
}

interface JoinAndNested<T, R> {
    default boolean getInnerJoin() {
        return false;
    }

    default boolean getCondition() {
        return false;
    }

    void process(Class<T> clazz, AbstractWrapper<T, R, ?> wrapper);
}

class WheresGroupInfo<T, R> implements Comparable<WheresGroupInfo<?, ?>>, JoinAndNested<T, R> {
    String groupName;

    Map<String, ColumnGroupInfo<T, R>> columnGroupInfoMap;

    boolean outerJoin;

    boolean innerJoin;

    public WheresGroupInfo(String groupName, boolean outerJoin, boolean innerJoin) {
        this.groupName = groupName;
        this.columnGroupInfoMap = new HashMap<>();
        this.outerJoin = outerJoin;
        this.innerJoin = innerJoin;
    }

    @Override
    public String toString() {
        return "WheresGroupInfo{" +
               "groupName='" + groupName + '\'' +
               ", columnGroupInfoMap=" + columnGroupInfoMap +
               ", outerJoin=" + outerJoin +
               ", innerJoin=" + innerJoin +
               '}';
    }

    @Override
    public int compareTo(WheresGroupInfo group) {
        final int compare = Boolean.compare(this.outerJoin, group.outerJoin);
        return compare != 0 ? compare : this.groupName.compareTo(group.groupName);
    }

    @Override
    public boolean getInnerJoin() {
        return innerJoin;
    }

    @Override
    public boolean getCondition() {
        return columnGroupInfoMap.values().stream().anyMatch(ColumnGroupInfo::getCondition);
    }

    @SuppressWarnings("all")
    public BiConsumer<Boolean, Consumer<AbstractWrapper<T, R, ?>>> getBiConsumer(boolean join, AbstractWrapper wrapper) {
        if (join) {
            return wrapper::or;
        }
        return wrapper::and;
    }

    @Override
    public void process(Class<T> clazz, AbstractWrapper<T, R, ?> wrapper) {
        this.getBiConsumer(this.outerJoin, wrapper).accept(this.getCondition(), innerWrapper -> {

            for (ColumnGroupInfo<T, R> columnGroupInfo : this.columnGroupInfoMap.values()) {
                this.getBiConsumer(this.innerJoin, innerWrapper)
                        .accept(columnGroupInfo.getCondition(), iInnerWrapper -> columnGroupInfo.process(clazz, iInnerWrapper));
            }
        });

    }

}

class ColumnGroupInfo<T, R> implements JoinAndNested<T, R> {
    List<ColumnInfo<T, R>> columnInfos;

    Boolean condition;

    public ColumnGroupInfo() {
        this.columnInfos = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "FieldInfo{" +
               "columnInfos=" + columnInfos +
               '}';
    }

    @Override
    public boolean getInnerJoin() {
        return columnInfos.stream().anyMatch(columnInfo -> columnInfo.join);
    }

    @Override
    public boolean getCondition() {
        return Optional.ofNullable(condition).orElseGet(() -> {
            final boolean flag = columnInfos.stream().anyMatch(ColumnInfo::getCondition);
            this.condition = flag;
            return flag;
        });
    }

    @Override
    public void process(Class<T> clazz, AbstractWrapper<T, R, ?> wrapper) {
        for (ColumnInfo<T, R> columnInfo : this.columnInfos) {
            if (this.getInnerJoin()) {
                wrapper.or();
            }
            columnInfo.process(clazz, wrapper);
        }
    }

}

class ColumnInfo<T, R> implements JoinAndNested<T, R> {
    String symbol;

    String column;

    Object value;

    boolean join;

    BiFunction<Class<T>, String, R> columnProcess;

    public ColumnInfo(String symbol, String column, Object value, boolean join, BiFunction<Class<T>, String, R> columnProcess) {
        this.symbol = symbol;
        this.column = column;
        this.value = value;
        this.join = join;
        this.columnProcess = columnProcess;
    }

    @Override
    public boolean getCondition() {
        // 对于 null 和 notNull 操作符，逻辑相反
        if ("null".equals(symbol)) {
            // null 操作符：当值为 null 时才生成 IS NULL 条件
            return value == null;
        }
        if ("notNull".equals(symbol)) {
            // notNull 操作符：当值不为 null 时才生成 IS NOT NULL 条件
            return ObjUtil.isNotEmpty(value);
        }

        // 其他操作符：当值不为空时才生成条件
        if (value instanceof String) {
            return StringUtils.isNotBlank((String) value);
        }
        if (value instanceof Collection) {
            return CollectionUtils.isNotEmpty((Collection<?>) value);
        }
        return value != null;
    }

    @Override
    public void process(Class<T> clazz, AbstractWrapper<T, R, ?> wrapper) {
        final R r = columnProcess.apply(clazz, this.column);
        whereSwitch(wrapper, r, this);
    }

    private static <T, R, W extends AbstractWrapper<T, R, W>> void whereSwitch(AbstractWrapper<T, R, W> queryWrapper, R r, ColumnInfo<T, R> columnInfo) {
        switch (columnInfo.symbol) {
            case "=": {
                queryWrapper.eq(columnInfo.getCondition(), r, columnInfo.value);
                break;
            }
            case "!=": {
                queryWrapper.ne(columnInfo.getCondition(), r, columnInfo.value);
                break;
            }
            case "null": {
                queryWrapper.isNull(columnInfo.getCondition(), r);
                break;
            }
            case "notNull": {
                queryWrapper.isNotNull(columnInfo.getCondition(), r);
                break;
            }
            case "in": {
                queryWrapper.in(columnInfo.getCondition(), r, (Collection<?>) columnInfo.value);
                break;
            }
            case "notIn": {
                queryWrapper.notIn(columnInfo.getCondition(), r, (Collection<?>) columnInfo.value);
                break;
            }
            case "like": {
                queryWrapper.like(columnInfo.getCondition(), r, columnInfo.value);
                break;
            }
            case "like%": {
                queryWrapper.likeRight(columnInfo.getCondition(), r, columnInfo.value);
                break;
            }
            case "<": {
                queryWrapper.lt(columnInfo.getCondition(), r, columnInfo.value);
                break;
            }
            case "<=": {
                queryWrapper.le(columnInfo.getCondition(), r, columnInfo.value);
                break;
            }
            case ">": {
                queryWrapper.gt(columnInfo.getCondition(), r, columnInfo.value);
                break;
            }
            case ">=": {
                queryWrapper.ge(columnInfo.getCondition(), r, columnInfo.value);
                break;
            }
            default:
        }
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
               "symbol='" + symbol + '\'' +
               ", column='" + column + '\'' +
               ", value=" + value +
               ", join=" + join +
               '}';
    }

    static class A implements Comparable<A> {
        Integer a;

        public A(Integer a) {
            this.a = a;
        }

        @Override
        public int compareTo(A o) {
            return this.a.compareTo(o.a);
        }

        @Override
        public String toString() {
            return "A{" +
                   "a=" + a +
                   '}';
        }
    }

    public static void main(String[] args) {
        final TreeSet<Object> set = new TreeSet<>();
        final A e = new A(4);
        set.add(e);
        set.add(new A(2));
        set.add(new A(3));
        System.out.println(set);
        e.a = 0;
        System.out.println(set);
    }

}
