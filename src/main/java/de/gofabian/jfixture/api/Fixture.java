package de.gofabian.jfixture.api;

import de.gofabian.jfixture.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Fixture {
    Scope scope() default Scope.METHOD;

    boolean autoUse() default false;
}
