package eu.pb4.styledchat.other;

import java.util.function.Function;

import eu.pb4.loader.SimpleEvent;

/**
 * Wraps another event so a mod can present a renamed or pre-filtered view of it without the
 * original listeners having to know about the wrapper.
 */
public final class WrappedEvent<T> {
   public final SimpleEvent<T> event;
   private final T invoker;

   public WrappedEvent(SimpleEvent<T> event, Function<SimpleEvent<T>, T> argModifier) {
      this.event = event;
      this.invoker = argModifier.apply(event);
   }

   public void register(T listener) {
      this.event.register(listener);
   }

   public T invoker() {
      return this.invoker;
   }
}
