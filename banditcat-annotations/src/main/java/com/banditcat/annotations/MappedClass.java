

package com.banditcat.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to configure you mappable objects.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MappedClass {

    /**
     * Use this property to establish the linked object type.
     */
    Class<?> with();

    /**
     * 是否启用源到目的的转换
     *
     * @return
     */
    boolean hasToModel() default true;

    /**
     * 是否启用目的到源的转换
     *
     * @return
     */
    boolean hasToEntity() default true;
}
