package com.tangula.android.orm;

import android.content.Context;

/**
 * The android context supplier function interface definition.
 *
 * Because the initial version is under JDK version 1.7, there is no jdk's supplier functions,
 * so we defined this interface to substitute it.
 *
 * @author Dose &middot; King &lt;doss.king@outlook.com&gt;
 */
@FunctionalInterface
public interface ContextSupplier {
    Context get();
}
