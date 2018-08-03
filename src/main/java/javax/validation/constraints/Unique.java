package javax.validation.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = { UniqueValidator.class})
public @interface Unique {
    Class<?> clazz();
    String primaryKey();
    String[] fields() default {};
    String message() default "Such %s already exists!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
