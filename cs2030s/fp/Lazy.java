package cs2030s.fp;

/**
 * This class encapsulates lazy evalation.
 *
 * @author Zhiyang Lu Lab 14H
 * @version CS2030S AY21/22 Semester 2
 */
public class Lazy<T> {
  /**
   * Producer that is to be lazily evaluated.
   */
  private Producer<? extends T> producer;
  
  /**
   * Value of type T encapsulated in Maybe.
   */
  private Maybe<T> value;

  /**
   * Constructor that takes in concrete value.
   *
   * @param value The concrete value that is to be encapsulated in Maybe.
   */
  private Lazy(T value) {
    this.value = Maybe.some(value);
  }

  /**
   * Constructor that takes in producer.
   *
   * @param producer The producer that is to be lazily evaluated.
   */
  private Lazy(Producer<? extends T> producer) {
    this.producer = producer;
    this.value = Maybe.none();
  }

  /**
   * Static factory method that takes in value.
   *
   * @param <U>   Type parameter.
   * @param value The value that is to be encapsulated in Maybe.
   * @return Instance of Lazy.
   */
  public static <U> Lazy<U> of(U value) {
    return new Lazy<U>(value);
  }

  /**
   * Static factory method that takes in value.
   *
   * @param <U>      Type parameter.
   * @param producer The producer that is to be lazily evaluated.
   * @return Instance of Lazy.
   */
  public static <U> Lazy<U> of(Producer<? extends U> producer) {
    return new Lazy<U>(producer);
  }

  /**
   * Evaluate the value if not yet done so and then return it.
   *
   * @return Content of type T.
   */
  public T get() {
    this.value = Maybe.some(this.value.orElseGet(this.producer));
    return this.value.orElse(null);
  }

  /**
   * Lazily transforms the content.
   *
   * @param <U>   Type parameter.
   * @param transformer Transformer that takes in type T and returns some type U.
   * @return Instance of Lazy of type U.
   */
  public <U> Lazy<U> map(Transformer<? super T, ? extends U> transformer) {
    return Lazy.<U>of(() -> transformer.transform(this.get()));
  }

  /**
   * Lazily transforms the content.
   *
   * @param <U>   Type parameter.
   * @param transformer Transformer that takes in type T and returns Lazy of some type U.
   * @return Instance of Lazy of type U.
   */
  public <U> Lazy<U> flatMap(Transformer<? super T, ? extends Lazy<? extends U>> transformer) {
    return Lazy.<U>of(() -> transformer.transform(this.get()).get());
  }

  /**
   * Lazily tests the content.
   *
   * @param booleanCondition booleanCondition that takes in type T.
   * @return A boolean value encapsulated in Lazy.
   */
  public Lazy<Boolean> filter(BooleanCondition<? super T> booleanCondition) {
    return Lazy.<Boolean>of(() -> booleanCondition.test(this.get()));
  }

  /**
   * Combine two instances of Lazy.
   *
   * @param <U>   Type parameter.
   * @param <V>   Type parameter.
   * @param otherLazy Secondary instance of Lazy to combine with.
   * @param combiner Combiner that specifies the return type.
   *
   * @return returns an instance of Lazy.
   */
  public <U, V> Lazy<V> combine(Lazy<? extends U> otherLazy,
                                Combiner<? super T, ? super U, ? extends V> combiner) {
    return Lazy.<V>of(() -> combiner.combine(this.get(), otherLazy.get()));
  }

  /**
   * Checks if given object is equal to this instance.
   *
   * @param obj Object that is to be compared to.
   * @return Boolean of whether they are equal.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof Lazy<?>) {
      Lazy<?> lazy = (Lazy<?>) obj;
      return this.get() == lazy.get()
        ? true
        : this.get() == null || lazy.get() == null
        ? false
        : this.get().equals(lazy.get());
    } else {
      return false;
    }
  }

  /**
   * Returns the string representation of this instance.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return this.value.map(x -> String.valueOf(x)).orElse("?");
  }
}
