package dev.visorcompat.tacz;

import dev.visorcompat.tacz.server.NativeOperationScope;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

class NativeOperationScopeTest {
    @Test void deferredLeftRoundKeepsLeftContextWhileRightWeaponOperates() {
        var scope = new NativeOperationScope<String>();
        BooleanSupplier left = scope.run("left", () -> scope.capture(() -> {
            assertEquals("left", scope.current());
            return true;
        }, ignored -> true));
        scope.run("right", () -> {
            assertTrue(left.getAsBoolean());
            assertEquals("right", scope.current());
            return null;
        });
        assertNull(scope.current());
    }
    @Test void unequippingOriginalWeaponCancelsQueuedRoundBeforeAmmoCallback() {
        var scope = new NativeOperationScope<String>();
        var equipped = new AtomicBoolean(true);
        var consumed = new AtomicBoolean(false);
        BooleanSupplier round = scope.run("weapon", () -> scope.capture(() -> {
            consumed.set(true);
            return true;
        }, ignored -> equipped.get()));
        equipped.set(false);
        assertFalse(round.getAsBoolean());
        assertFalse(consumed.get());
        assertNull(scope.current());
    }
    @Test void nativeCallbackFailureRestoresOuterContext() {
        var scope = new NativeOperationScope<String>();
        scope.run("right", () -> {
            assertThrows(IllegalStateException.class, () -> scope.run("left", () -> { throw new IllegalStateException(); }));
            assertEquals("right", scope.current());
            return null;
        });
        assertNull(scope.current());
    }
    @Test void ordinaryNativeTasksKeepOriginalLifetime() {
        var scope = new NativeOperationScope<String>();
        BooleanSupplier task = () -> true;
        assertSame(task, scope.capture(task, ignored -> false));
    }
}
