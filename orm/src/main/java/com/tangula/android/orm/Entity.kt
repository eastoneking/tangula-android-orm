package com.tangula.android.orm

import java.io.Serializable

/**
 * The entity interface.
 *
 * It constrain sub type must be extends from Serializable interface and have an id property.
 *
 * @param[T] the id property'ss type.
 *
 * This interface allow any type as the entity's primary key type, but we advise it should be
 * Stirng.
 *
 * @author Dose &middot; King &lt;doss.king@outlook.com&gt;
 */
interface Entity<T> : Serializable {

    /**
     * The Id property.
     */
    var id: T

}
