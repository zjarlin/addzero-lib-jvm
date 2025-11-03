package site.addzero.stream_wrapper;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author zjarlin
 * @since 2023/2/15 15:02
 */
public class NotStreamWrapper<T> extends StreamWrapper<T> {

    @Override
    public StreamWrapper<T> eq(boolean condition, Function<T, ? extends CharSequence> getFun, CharSequence searchSeq) {
        if (!condition) {
            return this;
        }
        Predicate<T> predicate = getPredicate();
        Predicate<T> tPredicate = t -> StringUtils.equals(getFun.apply(t), searchSeq);

        Predicate<T> negate = tPredicate.negate();
        setPredicate(predicate.and(negate));

        return and();
    }

    @Override
    public StreamWrapper<T> like(boolean condition, Function<T, ? extends CharSequence> getFun, CharSequence searchSeq) {
        if (!condition) {
            return this;
        }
        Predicate<T> predicate = getPredicate();
        Predicate<T> tPredicate = t -> StringUtils.containsIgnoreCase(getFun.apply(t), searchSeq);
        Predicate<T> negate = tPredicate.negate();
        setPredicate(predicate.and(negate));
        return and();
    }

    @Override
    public StreamWrapper<T> in(boolean condition, Function<T, ? extends CharSequence> getFun, Collection<?> searchSeqs) {
        if (!condition) {
            return this;
        }
        Predicate<T> predicate = getPredicate();
        Predicate<T> tPredicate = e -> searchSeqs.contains(getFun.apply(e));
        Predicate<T> negate = tPredicate.negate();
        setPredicate(predicate.and(negate));
        return and();
    }
}
