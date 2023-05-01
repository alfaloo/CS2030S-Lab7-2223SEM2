package cs2030s.fp;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * This class encapsulates a lazy infinite list.
 *
 * @author Zhiyang Lu Lab 14H
 * @version CS2030S AY21/22 Semester 2
 */
public class InfiniteList<T> {

  /**
   * Head of the list.
   */
  private final Lazy<Maybe<T>> head;
  
  /**
   * Tail of the list.
   */
  private final Lazy<InfiniteList<T>> tail;
  
  /**
   * A cached SENTINEL to mark the end of a finite list.
   */
  private static final InfiniteList<Object> SENTINEL = new Sentinel();

  /**
   * Constructor for Sentinel.
   */
  private InfiniteList() { 
    this.head = null; 
    this.tail = null;
  }

  /**
   * Static method that creates an InfiniteList from the producer.
   *
   * @param <T> Parameterised type.
   * @param producer Producer that produces every item in the list.
   * @return InfiniteList.
   */
  public static <T> InfiniteList<T> generate(Producer<T> producer) {
    return new InfiniteList<>(Lazy.of(() -> Maybe.some(producer.produce())),
                              Lazy.of(() -> InfiniteList.generate(producer)));
  }

  /**
   * Static method that creates an InfiniteList from an initial seed and a transformer.
   *
   * @param <T> Parameterised type.
   * @param seed Initial item.
   * @param next Transformer that produces subsequent items.
   * @return InfiniteList.
   */
  public static <T> InfiniteList<T> iterate(T seed, Transformer<T, T> next) {
    return new InfiniteList<>(seed, () -> InfiniteList.iterate(next.transform(seed), next));
  }

  /**
   * Constructor for iterate.
   *
   * @param head First element in the list.
   * @param tail Producer that produces the tail.
   */
  private InfiniteList(T head, Producer<InfiniteList<T>> tail) {
    this.head = Lazy.of(Maybe.some(head));
    this.tail = Lazy.of(tail);
  }

  /**
   * Constructor for generate.
   *
   * @param head Lazy of a Maybe of an item.
   * @param tail Lazy of an InfiniteList.
   */
  private InfiniteList(Lazy<Maybe<T>> head, Lazy<InfiniteList<T>> tail) {
    this.head = head;
    this.tail = tail;
  }

  /**
   * Method to retrieve the non-None head item.
   *
   * @return Item of parameterised type.
   */
  public T head() {
    return this.head.get().orElseGet(() -> this.tail.get().head());
  }

  /**
   * Method to retrieve the tail after the first none-None item.
   *
   * @return InfiniteList of parameterised type.
   */
  public InfiniteList<T> tail() {
    return this.head.get().map(head -> this.tail.get()).orElseGet(() -> this.tail.get().tail());
  }

  /**
   * Maps the InfiniteList.
   *
   * @param <R> parameterised type.
   * @param mapper Transformer that transforms each item in the list.
   * @return InfiniteList of type R.
   */
  public <R> InfiniteList<R> map(Transformer<? super T, ? extends R> mapper) {
    return new InfiniteList<>(Lazy.of(() -> this.head.get().map(mapper)),
                              Lazy.of(() -> this.tail.get().map(mapper)));
  }

  /**
   * Filters the InfiniteList.
   *
   * @param predicate BooleanCondition that filters each item in the list.
   * @return InfiniteList.
   */
  public InfiniteList<T> filter(BooleanCondition<? super T> predicate) {
    return new InfiniteList<>(Lazy.of(() -> this.head.get().filter(predicate)),
                              Lazy.of(() -> this.tail.get().filter(predicate)));
  }

  /**
   * Static nested class used to mark the end of a finite list.
   */
  private static final class Sentinel extends InfiniteList<Object> {
    /**
     * Private constructor for the creation of a Sentinel.
     */
    private Sentinel() {
      super();
    }

    @Override
    public Object head() {
      throw new NoSuchElementException();
    }

    @Override
    public InfiniteList<Object> tail() {
      throw new NoSuchElementException();
    }

    @Override
    public <R> InfiniteList<R> map(Transformer<? super Object, ? extends R> mapper) {
      return InfiniteList.sentinel();
    }

    @Override
    public InfiniteList<Object> filter(BooleanCondition<? super Object> predicate) {
      return InfiniteList.sentinel();
    }

    @Override
    public boolean isSentinel() {
      return true;
    }

    @Override
    public InfiniteList<Object> limit(long n) {
      return InfiniteList.sentinel();
    }

    @Override
    public List<Object> toList() {
      return new ArrayList<>();
    }

    @Override
    public InfiniteList<Object> takeWhile(BooleanCondition<? super Object> predicate) {
      return InfiniteList.sentinel();
    }

    @Override
    public <U> U reduce(U identity, Combiner<U, ? super Object, U> accumulator) {
      return identity;
    }

    @Override
    public long count() {
      return 0;
    }

    @Override
    public String toString() {
      return "-";
    }
  }

  /**
   * Static factory method to create an end-marker for a finite list.
   *
   * @param <R> Parameterised type.
   * @return An instance of Sentinel.
   */
  public static <R> InfiniteList<R> sentinel() {
    // Since class Sentinel is non-generic and does not have a parameterised field,
    // we can safely cast without worrying about user giving unsupported type.
    @SuppressWarnings("unchecked")
    InfiniteList<R> sentinel = (InfiniteList<R>) InfiniteList.SENTINEL;
    return sentinel;
  }

  /**
   * Method to check whether this instance is a Sentinel.
   *
   * @return True if this instance is a Sentinel, false otherwise.
   */
  public boolean isSentinel() {
    return false;
  }

  /**
   * Method that concatinates an InfiniteList and only takes the first n
   * items from an InfiniteList.
   *
   * @param n Specifies how many items are kept.
   * @return A finite InfiniteList containing these items.
   */
  public InfiniteList<T> limit(long n) {
    return n <= 0
         ? InfiniteList.sentinel()
         : new InfiniteList<>(this.head, Lazy.of(() -> this.head.get()
                                          .map(head -> this.tail.get().limit(n - 1))
                                          .orElseGet(() -> this.tail.get().limit(n))));
  }

  /**
   * Method used to convert a finite InfiniteList into a List of type T.
   *
   * @return A List containing the items in a finite InfiniteList.
   */
  public List<T> toList() {
    List<T> list = new ArrayList<T>();
    return this.head.get().map(existsHead -> {
      list.add(existsHead);
      list.addAll(this.tail.get().toList());
      return list;
    }).orElseGet(() -> this.tail.get().toList()); 
  
    /*
     * Valid alternative solution that uses loops and ifPresent/consumeWith.
     */
//    List<T> list = new ArrayList<T>();
//    InfiniteList<T> curr = this;
//    while (!curr.isSentinel()) {
//      curr.head.get().ifPresent(list::add);
//      curr = curr.tail.get();
//    }
//    return list;
  }

  /**
   * Method that truncates the list as soon as it finds an element
   * that evaluates the condition to false.
   *
   * @param predicate BooleanCondition that checks each element.
   * @return A truncated finite InfiniteList.
   */
  public InfiniteList<T> takeWhile(BooleanCondition<? super T> predicate) {
    Lazy<Maybe<T>> cached = Lazy.of(() -> this.head.get().filter(predicate));
    return new InfiniteList<>(cached, Lazy.of(() -> this.head.get()
                                              .map(existsHead -> cached.get()
                                                   .map(filteredHead ->
                                                        this.tail.get().takeWhile(predicate))
                                                   .orElseGet(() -> InfiniteList.sentinel()))
                                              .orElseGet(() -> this.tail
                                                         .get().takeWhile(predicate))));

  /*
   * Iteration 1.
   *
   * @issue Calling on [N, 1, 2, 3, ...] would concatinate immediately and return [N, S].
   */
//  return new InfiniteList<>(Lazy.of(() -> this.head.get().filter(predicate)),
//                            Lazy.of(() -> this.head.get().filter(predicate)
//                                    .map(head -> this.tail.get().takeWhile(predicate))
//                                    .orElseGet(() -> InfiniteList.sentinel())));

  /*
   * Iteration 2.
   *
   * @issue Calling on [N, S] causes head() being called on Sentinel
   * and NoSuchElementException thrown.
   */
//  return new InfiniteList<>(Lazy.of(() -> Maybe.some(this.head()).filter(predicate)),
//                            Lazy.of(() -> Maybe.some(this.head()).filter(predicate)
//                                    .map(head -> this.tail().takeWhile(predicate))
//                                    .orElseGet(() -> InfiniteList.sentinel())));

  /*
   * Iteration 3.
   *
   * @issue if-else statements discouraged.
   */
//  if (this.head.get().equals(Maybe.none())) {
//    return new InfiniteList<>(Lazy.of(Maybe.none()),
//                              Lazy.of(() -> this.tail.get().takeWhile(predicate)));
//  } else if (this.head.get().filter(predicate).equals(Maybe.none())) {
//    return InfiniteList.sentinel();
//  } else {
//    return new InfiniteList<>(this.head, Lazy.of(() -> this.tail.get().takeWhile(predicate)));
//  }

  /*
   * Iteration 4.
   *
   * @issue Flawless logic, but causes filter(predicate) to be evaluated twice.
   */
//  return new InfiniteList<>(Lazy.of(() -> this.head.get().filter(predicate)),
//                            Lazy.of(() -> this.head.get()
//                                    .map(existsHead -> this.head.get().filter(predicate)
//                                         .map(filteredHead -> this.tail.get()
//                                              .takeWhile(predicate))
//                                         .orElseGet(() -> InfiniteList.sentinel()))
//                                    .orElseGet(() -> this.tail.get().takeWhile(predicate))));
  }

  /**
   * Method that applies a lambda repeatedly on the elements of the
   * InfiniteList to reduce it into a single value.
   *
   * @param <U> Parameterised type.
   * @param identity Given value to start the operation.
   * @param accumulator Combiner that combines elements in the InfiniteList.
   * @return A single value of type U.
   */
  public <U> U reduce(U identity, Combiner<U, ? super T, U> accumulator) {
    return this.head.get().map(existsHead -> this.tail.get()
                               .reduce(accumulator.combine(identity, existsHead), accumulator))
                               .orElse(this.tail.get().reduce(identity, accumulator));
  }

  /**
   * Method that calculates the length of the InfiniteList.
   *
   * @return The length of the InfiniteList.
   */
  public long count() {
    return this.head.get().map(head -> 1 + this.tail.get().count()).orElse(this.tail.get().count());
  }

  @Override
  public String toString() {
    return "[" + this.head + " " + this.tail + "]";
  }
}
