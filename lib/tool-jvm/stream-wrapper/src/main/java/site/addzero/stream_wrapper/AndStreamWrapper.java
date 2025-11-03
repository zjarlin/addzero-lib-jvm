package site.addzero.stream_wrapper;// package com.addzero.jlstarter.common.util.stream_wrapper;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author zjarlin
 * @since 2023/2/15 15:02
 */
public class AndStreamWrapper<T> extends StreamWrapper<T> {

    @Override
    public StreamWrapper<T> eq(boolean condition, Function<T, ? extends CharSequence> getFun, CharSequence searchSeq) {
        if (!condition) {
            return this;
        }
        Predicate<T> predicate = getPredicate();
        setPredicate(predicate.and(t -> StringUtils.equals(getFun.apply(t), searchSeq)));
        return this;
    }

    @Override
    public StreamWrapper<T> like(boolean condition, Function<T, ? extends CharSequence> getFun, CharSequence searchSeq) {
        if (!condition) {
            return this;
        }
        Predicate<T> predicate = getPredicate();
        setPredicate(predicate.and(t -> StringUtils.containsIgnoreCase(getFun.apply(t), searchSeq)));
        return this;
    }

    @Override
    public StreamWrapper<T> in(boolean condition, Function<T, ? extends CharSequence> getFun, Collection<?> searchSeqs) {
        if (!condition) {
            return this;
        }
        Predicate<T> predicate = getPredicate();
        setPredicate(predicate.and(e -> searchSeqs.contains(getFun.apply(e))));
        return this;
    }

}
