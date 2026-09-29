package eu.pb4.loader;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * The slice of Fabric's event API this mod exposes to other mods.
 *
 * Forge has no equivalent generic callback bus, so the three content events the chat pipeline
 * publishes are backed by this instead. The shape is deliberately the same as the Fabric original:
 * {@link #register(Object)} adds a listener and {@link #invoker()} returns the combined callback,
 * so dependent mods keep using {@code EVENTS.X.invoker().onSomething(...)}.
 *
 * The listener list is copy-on-write because messages are formatted on the server thread while
 * mods may register during load.
 */
public class SimpleEvent<T> {
    private final List<T> listeners = new CopyOnWriteArrayList<>();
    private final Function<List<T>, T> invokerFactory;

    public SimpleEvent(Function<List<T>, T> invokerFactory) {
        this.invokerFactory = invokerFactory;
    }

    public void register(T listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    public T invoker() {
        return this.invokerFactory.apply(this.listeners);
    }

    public List<T> listeners() {
        return this.listeners;
    }
}
