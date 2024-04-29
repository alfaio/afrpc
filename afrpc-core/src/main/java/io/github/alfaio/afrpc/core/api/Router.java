package io.github.alfaio.afrpc.core.api;

import java.util.List;

/**
 * @author LimMF
 * @since 2024/3/16
 **/
public interface Router<T> {

    List<T> choose(List<T> providers);

    Router Default = p -> p;
}
