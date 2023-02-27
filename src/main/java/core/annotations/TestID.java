package core.annotations;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation allows assigning test ID to a test. It maps to a PractiTest ID.
 * Every test must have a TestID annotation defined.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@LabelAnnotation(name = "practiTestId")
public @interface TestID {
    String value();
}
