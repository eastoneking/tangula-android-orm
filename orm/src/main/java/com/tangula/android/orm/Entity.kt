package com.tangula.android.orm

import java.io.Serializable

interface Entity<T> : Serializable {

    var id: T

}
