package com.tangula.android.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Column definition annotation.
 * @author Dose &middot; King &lt;doss.king@outlook.com&gt;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column {
    /**
     * The column's name.
     * @return the column's name.
     * The default value is an empty string.
     */
    String value() default "";

    /**
     * The column's type.
     * @return The columns's type.
     * The default type is [com.tangula.android.orm.DbType.Text].
     * @see  DbType
     */
    DbType type() default DbType.TEXT;
}
