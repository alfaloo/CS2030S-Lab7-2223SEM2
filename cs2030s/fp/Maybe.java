/**
 * CS2030S Lab 6.
 * AY22/23 Semester 2
 *
 * @author Zhiyang Lu (Lab 14H)
 */

package cs2030s.fp;

import cs2030s.fp.BooleanCondition;
import cs2030s.fp.Transformer;
import java.util.NoSuchElementException;

public abstract class Maybe<T> {

  private static final None empty = new None();

  /*
   * Static nested class.
   */
  private static final class None extends Maybe<Object> {
  
    @Override
    public boolean equals(Object obj) {
      if (None.this == obj) {
        return true;
      }

      if (obj instanceof None) {
        return true;
      } else {
        return false;
      }
    }

    @Override
    protected Object get() {
      throw new NoSuchElementException();
    }

    @Override
    public Maybe<Object> filter(BooleanCondition<? super Object> booleanCondition) {
      return Maybe.none();
    }

    @Override
    public <U> Maybe<U> map(Transformer<? super Object, ? extends U> transformer) {
      return Maybe.<U>none();
    }

    @Override
    public <U> Maybe<U> flatMap(Transformer<? super Object,
                                ? extends Maybe<? extends U>> transformer) {
      return Maybe.<U>none();
    }

    @Override
    public Object orElse(Object obj) {
      return obj;
    }

    @Override
    public Object orElseGet(Producer<? extends Object> producer) {
      return producer.produce();
    }

    @Override
    public void ifPresent(Consumer<? super Object> consumer) {
      return;
    }

    /**
     * If the value within this Maybe is missing, do nothing.
     * Otherwise, consume the value with the given consumer.
     *
     * @param consumer The consumer to consume the value
     */
    public void consumeWith(Consumer<? super Object> consumer) {
      return;
    }

    @Override
    public String toString() {
      return "[]";
    }

  }

  /*
   * Immutable static nested class.
   */
  private static final class Some<T> extends Maybe<T> {
    private final T item;
 
    private Some(T item) {
      this.item = item;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (obj instanceof Some<?>) {
        Some<?> some = (Some<?>) obj;
        return this.item == some.item
          ? true
          : this.item == null || some.item == null
          ? false
          : this.item.equals(some.item);
      } else {
        return false;
      }

    }

    @Override
    protected T get() {
      return Some.this.item;
    }

    @Override
    public Maybe<T> filter(BooleanCondition<? super T> booleanCondition) {
      if (Some.this.item == null) {
        return Maybe.some(null);
      } else if (booleanCondition.test(Some.this.item)) {
        return Maybe.<T>some(Some.this.item);
      } else {
        return Maybe.<T>none();
      }
    }

    @Override
    public <U> Maybe<U> map(Transformer<? super T, ? extends U> transformer) {
      return Maybe.<U>some(transformer.transform(Some.this.item));
    }

    @Override
    public <U> Maybe<U> flatMap(Transformer<? super T, ? extends Maybe<? extends U>> transformer) {
      Maybe<? extends U> oneLayer = transformer.transform(Some.this.item);
      if (oneLayer instanceof None) {
        return Maybe.<U>none();
      } else {
        return Maybe.<U>some(oneLayer.get());
      }
    }

    @Override
    public T orElse(T t) {
      return Some.this.item;
    }

    @Override
    public T orElseGet(Producer<? extends T> producer) {
      return Some.this.item;
    }

    @Override
    public void ifPresent(Consumer<? super T> consumer) {
      consumer.consume(Some.this.item);
    }

    /**
     * If the value within this Maybe is missing, do nothing.
     * Otherwise, consume the value with the given consumer.
     *
     * @param consumer The consumer to consume the value
     */
    @Override
    public void consumeWith(Consumer<? super T> consumer) {
      consumer.consume(Some.this.item);
    } 

    @Override
    public String toString() {
      return "[" + this.item + "]";
    }
  }

  public static <U> Maybe<U> none() {
    // Since class None is non-generic and does not have a parameterised field,
    // we can safely cast without worrying about user giving unsupported type.
    @SuppressWarnings("unchecked")
    Maybe<U> none = (Maybe<U>) Maybe.empty;
    return none;
  }

  public static <U> Maybe<U> some(U u) {
    return new Some<U>(u);
  }

  public static <U> Maybe<U> of(U u) {
    if (u == null) {
      return Maybe.<U>none();
    } else {
      return Maybe.<U>some(u);
    }
  }

  public abstract boolean equals(Object obj);

  protected abstract T get();

  public abstract Maybe<T> filter(BooleanCondition<? super T> booleanCondition);

  public abstract <U> Maybe<U> map(Transformer<? super T, ? extends U> transformer);

  public abstract <U> Maybe<U> flatMap(Transformer<? super T,
                                       ? extends Maybe<? extends U>> transformer);

  public abstract T orElse(T t);

  public abstract T orElseGet(Producer<? extends T> producer);

  public abstract void ifPresent(Consumer<? super T> consumer);

  /**
   * If the value within this Maybe is missing, do nothing.
   * Otherwise, consume the value with the given consumer.
   *
   * @param consumer The consumer to consume the value
   */
  public abstract void consumeWith(Consumer<? super T> consumer);
}
