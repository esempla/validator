package javax.validation.constraints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Objects;

import static java.lang.String.format;

public class UniqueValidator implements ConstraintValidator<Unique, Object> {

    final Logger log = LoggerFactory.getLogger(UniqueValidator.class);

    @Autowired
    private EntityManager entityManager;

    private String primaryKey;
    private String[] fields;
    private String message;
    private Class clazz;

    @Override
    public void initialize(Unique annotation) {
        this.primaryKey = annotation.primaryKey();
        this.fields = annotation.fields();
        this.message = annotation.message();
        this.clazz = annotation.clazz();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        if (Objects.isNull(entityManager)) {
            return true;
        }
        boolean isValid = false;
        int validationCount = fields.length;
        try {
            for (String field : fields) {
                Field fieldId = ReflectionUtils.findField(clazz, primaryKey);
                Field fieldName = ReflectionUtils.findField(clazz, field);
                fieldId.setAccessible(true);
                fieldName.setAccessible(true);
                Long fieldIdValue = (Long) fieldId.get(object);
                String fieldNameValue = (String) fieldName.get(object);

                Object result = null;
                try {
                    result = entityManager
                            .createQuery(format("FROM %s m WHERE m.%s = :field", clazz.getSimpleName(), field))
                            .setParameter("field", fieldNameValue)
                            .getSingleResult();
                } catch (Exception ex) {
                }

                if (Objects.isNull(fieldIdValue)) {
                    isValid = Objects.nonNull(result) ? false : true;
                } else {
                    if (Objects.nonNull(result)) {
                        Field fieldResult = ReflectionUtils.findField(clazz, primaryKey);
                        fieldResult.setAccessible(true);
                        Long fieldResultId = (Long) fieldResult.get(result);
                        if (fieldIdValue == fieldResultId) {
                            isValid = true;
                        } else {
                            isValid = false;
                        }
                    } else {
                        isValid = true;
                    }
                }
                if (!isValid) {
                    validationCount--;
                    context.disableDefaultConstraintViolation();
                    context
                            .buildConstraintViolationWithTemplate(format(message, field))
                            .addPropertyNode(field).addConstraintViolation();
                }
            }
            return validationCount == fields.length;
        } catch (Exception e) {
            log.error(e.getMessage());
            return isValid;
        }
    }
}