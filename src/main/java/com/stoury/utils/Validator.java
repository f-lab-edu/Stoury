package com.stoury.utils;

import java.util.ArrayDeque;
import java.util.function.Predicate;

public class Validator<T> {
    private final T obj;
    private Class<? extends RuntimeException> exceptionClass = RuntimeException.class;
    private final ArrayDeque<Predicate<T>> validators = new ArrayDeque<>();
    private final String EXCEPTION_CONSTRUCT_FAIL_MESSAGE = "Invoking exception constructor was failed.";
    private String dataInvalidMessage = null;

    private Validator(T obj) {
        this.obj = obj;
    }

    public static <T> Validator<T> of(T t) {
        return new Validator<>(t);
    }

    public Validator<T> willCheck(Predicate<T> validator) {
        this.validators.add(validator);
        return this;
    }

    public Validator<T> ifFailThrows(Class<? extends RuntimeException> exceptionClass) {
        this.exceptionClass = exceptionClass;
        return this;
    }

    public Validator<T> ifFailThrowsWithMessage(Class<? extends RuntimeException> exceptionClass, String message){
        return this.ifFailThrows(exceptionClass).withMessage(message);
    }

    public Validator<T> withMessage(String message) {
        this.dataInvalidMessage = message;
        return this;
    }

    public void validate() {
        while (!validators.isEmpty()) {
            try {
                Predicate<T> validator = validators.pollFirst();
                if (validator.test(obj)) {
                    continue;
                }
                if (dataInvalidMessage == null) {
                    throw exceptionClass.getConstructor().newInstance();
                } else {
                    throw exceptionClass.getConstructor(String.class).newInstance(dataInvalidMessage);
                }
            } catch (RuntimeException e) {
                if (e.getClass().equals(exceptionClass)) {
                    throw e;
                }
            } catch (Exception e) {
                throw new RuntimeException(EXCEPTION_CONSTRUCT_FAIL_MESSAGE, e);
            }
        }
    }
}