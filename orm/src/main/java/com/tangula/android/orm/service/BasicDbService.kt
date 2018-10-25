package com.tangula.android.orm.service

import android.content.Intent
import android.content.ServiceConnection
import com.tangula.android.base.TglLocalServiceBinder
import com.tangula.android.base.TglService
import com.tangula.android.orm.BaseDataBaseHelper
import com.tangula.android.orm.Entity
import com.tangula.android.utils.ApplicationUtils
import com.tangula.android.utils.TaskUtils
import com.tangula.utils.ConcurrentUtils
import java.io.Closeable


class DbBinder : TglLocalServiceBinder()

/**
 * 基础的数据库服务.
 *
 * 需要自己定义操作接口，然后在 [execSql]中执行，这个方法会在后台线程中执行，参数中的task方法,之后会
 * 关闭当前的binder连接(或者叫做执行unbind).
 */
class BasicDbService : TglService<DbBinder>() {
    override fun onCreateBinder(intent: Intent?, flags: Int, startId: Int): DbBinder {
        return DbBinder()
    }

    companion object {
        fun exec(callback: () -> Unit): Closeable {
            lateinit var conn: ServiceConnection
            var closed = false
            val res = Closeable {
                ConcurrentUtils.doubleCheckRun(conn, { !closed }) {
                    closed = true
                    ApplicationUtils.APP.unbindService(conn)
                }
            }

            conn = TglService.bind2BackgroundService(ApplicationUtils.APP, BasicDbService::class.java, { _ ->
                TaskUtils.runInBackground {
                    callback()
                    res.close()
                }
            }, { _ -> }) {}
            return res
        }
    }


    fun <E:Entity<String>> execSql(callback: (BaseDataBaseHelper<E>) -> Unit): Closeable {
        return exec{callback.invoke(object:BaseDataBaseHelper<E>(){})}
    }

}