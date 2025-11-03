package site.addzero.stream_wrapper;

import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// @Data
@SuppressWarnings("unused")
@Data
public abstract class StreamWrapper<T> {
    private Stream<T> stream;

    private Predicate<T> predicate;

    public static <T> StreamWrapper<T> lambdaquery(Collection<T> collection) {
        return lambdaquery(collection.stream());
    }

    public static <T> StreamWrapper<T> lambdaquery(Stream<T> stream) {
        return new AndStreamWrapper<T>() {{
            setStream(stream);
            setPredicate(t -> true);
        }};
    }

    public abstract StreamWrapper<T> eq(boolean condition, Function<T, ? extends CharSequence> getFun, CharSequence searchSeq);

    public abstract StreamWrapper<T> like(boolean condition, Function<T, ? extends CharSequence> getFun, CharSequence searchSeq);

    public abstract StreamWrapper<T> in(boolean condition, Function<T, ? extends CharSequence> getFun, Collection<?> searchSeqs);

  public   List<T> list() {
        return stream.filter(predicate).collect(Collectors.toList());
    }

   public T one() {
        return stream.filter(predicate).findAny().orElse(null);
    }

  public   StreamWrapper<T> or() {
        return new OrStreamWrapper<T>() {{
            setPredicate(predicate);
            setStream(stream);
        }};
    }

  StreamWrapper<T> and() {
        return new AndStreamWrapper<T>() {{
            setPredicate(predicate);
            setStream(stream);
        }};
    }

    // StreamWrapper<T> and(Predicate<? super T> other) {
    //     return new AndStreamWrapper<>() {{
    //         setPredicate(predicate.and(other));
    //         setStream(stream);
    //     }};
    // }
    public StreamWrapper<T> negate() {
        setPredicate(getPredicate().negate());
        return this;
    }

    /**
     * 对下个条件否定
     *
     * @return {@link StreamWrapper }<{@link T }>
     * @author zjarlin
     * @since 2023/02/16
     */
 public    StreamWrapper<T> not() {
        return new NotStreamWrapper<T>() {{
            setPredicate(predicate);
            setStream(stream);
        }};
    }


//     // abstract StreamWrapper<T> eq( Function<T, ? extends CharSequence> getFun, CharSequence searchSeq);
//
//     // abstract StreamWrapper<T> like(Function<T, ? extends CharSequence> getFun, CharSequence searchSeq);
//
//     // abstract StreamWrapper<T> in(Function<T, ? extends CharSequence> getFun, Collection<?> searchSeqs);
//
//     // abstract StreamWrapper<T> gt(boolean condition, Function<T, ? extends Comparable<?>> getFun, Comparable<?> value);
//
//     // abstract StreamWrapper<T> gt(Function<T, ? extends Comparable<?>> getFun, Comparable<?> value);
//
//     // abstract StreamWrapper<T> lt(boolean condition, Function<T, ? extends Comparable<?>> getFun, Comparable<?> value);
//     // abstract StreamWrapper<T> lt(Function<T, ? extends Comparable<?>> getFun, Comparable<?> value);
//
//     // abstract StreamWrapper<T> ge(boolean condition, Function<T, ? extends Comparable<?>> getFun, Comparable<?> value);
//
//     // abstract StreamWrapper<T> ge(Function<T, ? extends Comparable<?>> getFun, Comparable<?> value);
//
//     // abstract StreamWrapper<T> le(boolean condition, Function<T, ? extends Comparable<?>> getFun, Comparable<?> value);
//     // abstract StreamWrapper<T> le(Function<T, ? extends Comparable<?>> getFun, Comparable<?> value);
}
