package dev.visorcompat.tacz.server;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Keeps native delayed callbacks bound to their original weapon operation. */
public final class NativeOperationScope<T> {
    private final ThreadLocal<T> active = new ThreadLocal<>();
    public T current() { return active.get(); }
    public <R> R run(T context, Supplier<R> operation) {
        T previous = active.get();
        active.set(context);
        try { return operation.get(); }
        finally { if (previous == null) active.remove(); else active.set(previous); }
    }
    public BooleanSupplier capture(BooleanSupplier operation, Predicate<T> valid) {
        T captured = current();
        if (captured == null) return operation;
        return () -> valid.test(captured) && run(captured, operation::getAsBoolean);
    }
}
