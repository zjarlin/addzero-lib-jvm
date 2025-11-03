package site.addzero.stream_wrapper;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author zjarlin
 * @since 2023/2/15 15:25
 */
public class OrStreamWrapper<T> extends StreamWrapper<T> {

    @Override
    public StreamWrapper<T> eq(boolean condition, Function<T, ? extends CharSequence> getFun, CharSequence searchSeq) {
        if (!condition) {
            return this;
        }
        setPredicate(getPredicate().or(t -> StringUtils.equals(getFun.apply(t), searchSeq)));
        return and();
    }

    @Override
    public StreamWrapper<T> like(boolean condition, Function<T, ? extends CharSequence> getFun, CharSequence searchSeq) {
        if (!condition) {
            return this;
        }
        Predicate<T> predicate = getPredicate();
        setPredicate(predicate.or(t -> StringUtils.containsIgnoreCase(getFun.apply(t), searchSeq)));
        return and();
    }

    @Override
    public StreamWrapper<T> in(boolean condition, Function<T, ? extends CharSequence> getFun, Collection<?> searchSeqs) {
        if (!condition) {
            return this;
        }
        Predicate<T> predicate = getPredicate();
        setPredicate(predicate.or(e -> searchSeqs.contains(getFun.apply(e))));
        return and();
    }

}
