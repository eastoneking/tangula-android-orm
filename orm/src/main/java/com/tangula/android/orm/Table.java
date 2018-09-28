package com.tangula.android.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The table annotaion.
 *
 * You can define the table's name using this annotation's value property.
 *
 * @author Dose &middot; King &lt;doss.king@outlook.com&gt;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Table {
    /**
     * The table's name.
     * @return The table's name.
     */
    String value();
}
