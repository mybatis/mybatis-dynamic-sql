package org.mybatis.dynamic.sql;

import java.util.function.Function;

@FunctionalInterface
public interface Callback {
    void call();

    static Callback runtimeExceptionThrowingCallback(String message) {
        return exceptionThrowingCallback(message, RuntimeException::new);
    }

    static Callback exceptionThrowingCallback(String message,
            Function<String, ? extends RuntimeException> exceptionBuilder) {
        return () -> {
            throw exceptionBuilder.apply(message);
        };
    }
}
