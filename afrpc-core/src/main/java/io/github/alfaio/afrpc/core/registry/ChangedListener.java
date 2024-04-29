package io.github.alfaio.afrpc.core.registry;

/**
 * @author LimMF
 * @since 2024/3/19
 **/
public interface ChangedListener {
    void fire(Event event);
}
