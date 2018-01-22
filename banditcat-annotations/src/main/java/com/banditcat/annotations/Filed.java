

package com.banditcat.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to configure the data mapping between two fields.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Filed {

    /**
     * Use this property to establish a specific field name, for example, if the two fields does not have the same name.
     * @return
     */
     String toField() default "";
}
