package com.tangula.android.orm

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.util.Log
import org.apache.commons.lang3.StringUtils
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.*

interface DbCreator {

    fun getDataBaseName(): String

    fun getDataBaseVersion(): Int

    fun onCreate(db: SQLiteDatabase) {}

    fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}

abstract class BaseDataBaseHelper<T : Entity<String>> {


    companion object {

        @JvmStatic
        private var HELPER: SQLiteOpenHelper? = null

        @JvmStatic
        private var DB: SQLiteDatabase? = null

        @JvmStatic
        var CONTEXT_FAC: ContextSupplier? = null

        @JvmStatic
        var DB_CREATOR: DbCreator? = null

        @JvmStatic
        fun setDbCreator(creator: DbCreator){
            DB_CREATOR = creator
        }

        @JvmStatic
        private fun initDatabaseHelper() {
            synchronized(BaseDataBaseHelper::class.java) {
                if (HELPER == null) {
                    HELPER = object : SQLiteOpenHelper(CONTEXT_FAC!!.get(), DB_CREATOR!!.getDataBaseName(), null, DB_CREATOR!!.getDataBaseVersion()) {
                        override fun onCreate(db: SQLiteDatabase) {
                            DB_CREATOR?.onCreate(db)
                        }

                        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                            DB_CREATOR?.onUpgrade(db, oldVersion, newVersion)
                        }
                    }
                    if (DB != null) {
                        DB!!.close()
                    }
                    DB = HELPER!!.writableDatabase
                }
            }
        }

        @JvmStatic
        fun getDatabase(): SQLiteDatabase? {
            if (HELPER == null) {
                synchronized(BaseDataBaseHelper::class.java) {
                    if (HELPER == null) {
                        initDatabaseHelper()
                    }
                }
            }
            return DB
        }

        @JvmStatic
        protected fun beginTransaction() {
            getDatabase()!!.beginTransaction()
        }

        @JvmStatic
        protected fun beginTransactionNonExclusive() {
            getDatabase()!!.beginTransactionNonExclusive()
        }

        @JvmStatic
        protected fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) {
            getDatabase()!!.beginTransactionWithListener(transactionListener)
        }

        @JvmStatic
        protected fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) {
            getDatabase()!!.beginTransactionWithListenerNonExclusive(transactionListener)
        }

        @JvmStatic
        protected fun endTransaction() {
            getDatabase()!!.endTransaction()
        }

        @JvmStatic
        protected fun setTransactionSuccessful() {
            getDatabase()!!.setTransactionSuccessful()
        }

        @JvmStatic
        protected fun inTransaction(): Boolean {
            return getDatabase()!!.inTransaction()
        }

        @JvmStatic
        protected fun getVersion(): Int {
            return getDatabase()!!.version
        }

        @JvmStatic
        protected fun setVersion(version: Int) {
            getDatabase()!!.version = version
        }

        @JvmStatic
        @Throws(SQLException::class)
        protected fun compileStatement(sql: String): SQLiteStatement {
            return getDatabase()!!.compileStatement(sql)
        }

        @JvmStatic
        protected fun query(distinct: Boolean, table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String): Cursor {
            return getDatabase()!!.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        }

        @JvmStatic
        protected fun query(distinct: Boolean, table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String, cancellationSignal: CancellationSignal): Cursor {
            return getDatabase()!!.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal)
        }

        @JvmStatic
        protected fun queryWithFactory(cursorFactory: SQLiteDatabase.CursorFactory, distinct: Boolean, table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String): Cursor {
            return getDatabase()!!.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        }

        @JvmStatic
        protected fun queryWithFactory(cursorFactory: SQLiteDatabase.CursorFactory, distinct: Boolean, table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String, cancellationSignal: CancellationSignal): Cursor {
            return getDatabase()!!.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal)
        }

        @JvmStatic
        protected fun query(table: String?, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String): Cursor {
            return getDatabase()!!.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
        }

        @JvmStatic
        protected fun query(table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String): Cursor {
            return getDatabase()!!.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        }

        @JvmStatic
        protected fun rawQuery(sqlResId: Int, selectionArgs: Array<String>): Cursor {
            return rawQuery(CONTEXT_FAC!!.get().getString(sqlResId), selectionArgs)
        }

        @JvmStatic
        protected fun rawQuery(sql: String, selectionArgs: Array<String>): Cursor {
            return getDatabase()!!.rawQuery(sql, selectionArgs)
        }

        @JvmStatic
        protected fun rawQuery(sqlResId: Int, selectionArgs: Array<String>, cancellationSignal: CancellationSignal): Cursor {
            return rawQuery(CONTEXT_FAC!!.get().getString(sqlResId), selectionArgs, cancellationSignal)
        }

        @JvmStatic
        protected fun rawQuery(sql: String, selectionArgs: Array<String>, cancellationSignal: CancellationSignal): Cursor {
            return getDatabase()!!.rawQuery(sql, selectionArgs, cancellationSignal)
        }

        @JvmStatic
        protected fun rawQueryWithFactory(cursorFactory: SQLiteDatabase.CursorFactory, sql: String, selectionArgs: Array<String>, editTable: String): Cursor {
            return getDatabase()!!.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable)
        }

        @JvmStatic
        protected fun rawQueryWithFactory(cursorFactory: SQLiteDatabase.CursorFactory, sql: String, selectionArgs: Array<String>, editTable: String, cancellationSignal: CancellationSignal): Cursor {
            return getDatabase()!!.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable, cancellationSignal)
        }

        @JvmStatic
        protected fun insert(table: String, nullColumnHack: String, values: ContentValues): Long {
            return getDatabase()!!.insert(table, nullColumnHack, values)
        }

        @JvmStatic
        @Throws(SQLException::class)
        protected fun insertOrThrow(table: String, nullColumnHack: String?, values: ContentValues): Long {
            return getDatabase()!!.insertOrThrow(table, nullColumnHack, values)
        }

        @JvmStatic
        protected fun replace(table: String, nullColumnHack: String, initialValues: ContentValues): Long {
            return getDatabase()!!.replace(table, nullColumnHack, initialValues)
        }

        @JvmStatic
        @Throws(SQLException::class)
        protected fun replaceOrThrow(table: String, nullColumnHack: String, initialValues: ContentValues): Long {
            return getDatabase()!!.replaceOrThrow(table, nullColumnHack, initialValues)
        }

        @JvmStatic
        protected fun insertWithOnConflict(table: String, nullColumnHack: String, initialValues: ContentValues, conflictAlgorithm: Int): Long {
            return getDatabase()!!.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm)
        }

        @JvmStatic
        protected fun delete(table: String, whereClause: String, whereArgs: Array<String>): Int {
            return getDatabase()!!.delete(table, whereClause, whereArgs)
        }

        @JvmStatic
        protected fun update(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>): Int {
            return getDatabase()!!.update(table, values, whereClause, whereArgs)
        }

        @JvmStatic
        protected fun updateWithOnConflict(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>, conflictAlgorithm: Int): Int {
            return getDatabase()!!.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm)
        }

        @JvmStatic
        protected fun execSQL(resId: Int) {
            execSQL(CONTEXT_FAC!!.get().getString(resId))
        }

        @JvmStatic
        @Throws(SQLException::class)
        protected fun execSQL(sql: String) {
            getDatabase()!!.execSQL(sql)
        }

        @JvmStatic
        protected fun execSQL(resId: Int, bindArgs: Array<Any>) {
            execSQL(CONTEXT_FAC!!.get().getString(resId, *bindArgs))
        }

        @JvmStatic
        @Throws(SQLException::class)
        protected fun execSQL(sql: String, bindArgs: Array<Any>) {
            getDatabase()!!.execSQL(sql, bindArgs)
        }

        @JvmStatic
        protected fun validateSql(sql: String, cancellationSignal: CancellationSignal?) =//getDatabase().validateSql(sql, cancellationSignal);
                Unit

        @JvmStatic
        protected fun isReadOnly(): Boolean {
            return getDatabase()!!.isReadOnly
        }

        @JvmStatic
        protected fun isOpen(): Boolean {
            return getDatabase()!!.isOpen
        }

        @JvmStatic
        protected fun needUpgrade(newVersion: Int): Boolean {
            return getDatabase()!!.needUpgrade(newVersion)
        }

        @JvmStatic
        protected fun setLocale(locale: Locale) {
            getDatabase()!!.setLocale(locale)
        }

        @JvmStatic
        protected fun setMaxSqlCacheSize(cacheSize: Int) {
            getDatabase()!!.setMaxSqlCacheSize(cacheSize)
        }


        @JvmStatic
        fun <T : Entity<String>> createTable(entityClz: Class<T>, force: Boolean){

           val clz_ann:Table? =  entityClz.getAnnotation(Table::class.java)
            println(clz_ann)
            if(clz_ann==null){
                return
            }

            val ddl = StringBuffer()

            val tablename: String = clz_ann.value ?: return

            val column_defs = StringBuffer("\tid text primary key")

            for(f in entityClz.declaredFields){
                if(StringUtils.equals("id", f.name)){
                    continue
                }

                var field_ann: Column? = f.getAnnotation(Column::class.java) ?: continue

                val col_name = field_ann?.value
                val col_type = field_ann?.type

                if(col_name!=null&&col_type!=null){
                    column_defs.append(",\n\t$col_name ${col_type.name}")
                }

            }


            ddl.append("create table if not exists $tablename ($column_defs)")
            val ddl_sql = ddl.toString()

            if(force){
                getDatabase()!!.execSQL("drop table if exists $tablename")
            }
            getDatabase()!!.execSQL(ddl_sql)

        }


    }



    fun byCondition(condition: String, args: Array<String>, group: String, having: String, orderby: String): List<T> {
        val res = ArrayList<T>()

        val pt = javaClass.genericSuperclass as ParameterizedType
        val clazz = pt.actualTypeArguments[0] as Class<T>

        val columns = ArrayList<String>()
        val map = HashMap<String, Field>()

        for (field in clazz.declaredFields) {
            val col = field.getAnnotation(Column::class.java) ?: continue
            val col_name = col.value
            if (col_name == null || col_name!!.trim { it <= ' ' } == "") {
                continue
            }
            columns.add(col_name)
            map[col_name] = field
        }

        val field_arr = columns.toTypedArray()
        columns.toTypedArray()
        var c: Cursor? = null
        try {
            val table_name = obtainEntityTableName(clazz)
            c = query(table_name, field_arr, condition, args, group, having, orderby)
            while (c.moveToNext()) {
                try {
                    val entity = clazz.newInstance()
                    cusor2Entity(entity, columns, map, c)
                    res.add(entity)
                } catch (e: InstantiationException) {
                    Log.e("console", e.localizedMessage, e)
                    continue
                } catch (e: IllegalAccessException) {
                    Log.e("console", e.localizedMessage, e)
                    continue
                }

            }
        } catch (e: Exception) {
            Log.e("console", e.localizedMessage, e)
        } finally {
            c?.close()
        }
        return res
    }

    fun byId(id: String): T? {
        var entity: T? = null

        val pt = javaClass.genericSuperclass as ParameterizedType
        val clazz = pt.actualTypeArguments[0] as Class<T>

        val columns = ArrayList<String>()
        val map = HashMap<String, Field>()

        for (field in clazz.declaredFields) {
            val col = field.getAnnotation(Column::class.java) ?: continue
            val col_name = col.value
            if (col_name == null || col_name!!.trim { it <= ' ' } == "") {
                continue
            }

            columns.add(col_name)
            map[col_name] = field
        }

        val field_arr = columns.toTypedArray()
        columns.toTypedArray()
        var c: Cursor? = null
        try {
            val table_name = obtainEntityTableName(clazz)
            c = query(table_name, field_arr, " id=? ", arrayOf(id), "", "", "")
            if (c!!.moveToNext()) {
                try {
                    entity = clazz.newInstance()
                } catch (e: InstantiationException) {
                    Log.e("console", e.localizedMessage, e)
                    return null
                } catch (e: IllegalAccessException) {
                    Log.e("console", e.localizedMessage, e)
                    return null
                }

                cusor2Entity(entity, columns, map, c)
            }
        } catch (e: Exception) {
            Log.e("console", e.localizedMessage, e)
        } finally {
            if (c != null) {
                c!!.close()
            }
        }

        return entity
    }

    private fun cusor2Entity(entity: T?, columns: List<String>, map: Map<String, Field>, c: Cursor) {
        for (col_name in columns) {
            Log.i("console", "col:$col_name")
            try {
                val field = map[col_name]
                val type = field!!.type
                val index = c.getColumnIndex(col_name)
                field.isAccessible = true
                when {
                    type.isAssignableFrom(Float::class.java) -> field.set(entity, c.getFloat(index))
                    type.isAssignableFrom(Double::class.java) -> field.set(entity, c.getDouble(index))
                    type.isAssignableFrom(Int::class.java) -> field.set(entity, c.getInt(index))
                    type.isAssignableFrom(Long::class.java) -> field.set(entity, c.getLong(index))
                    type.isAssignableFrom(Short::class.java) -> field.set(entity, c.getShort(index))
                    type.isAssignableFrom(String::class.java) -> field.set(entity, c.getString(index))
                }
            } catch (e: IllegalAccessException) {
                Log.e(null, e.localizedMessage, e)
            }

        }
    }

    fun delete(entity: T?): Int {
        var res = 0
        if (entity == null || StringUtils.isBlank(entity!!.id)) {
            return res
        }

        val clz = entity!!.javaClass

        val ann_table = clz.getAnnotation(Table::class.java)
        if (ann_table == null) {
            Log.e("console", clz.name + " is not annotation " + Table::class.java.name)
            return res
        }

        val table_name = ann_table!!.value

        res = delete(table_name, "id = ?", arrayOf(entity!!.id))

        return res
    }


    fun save(entity: T?): T? {

        if (entity == null) {
            return entity
        }

        val clz = entity!!.javaClass

        val table_name = obtainEntityTableName(clz) ?: return null
        val values = ContentValues()

        val columns = ArrayList<String>()
        val map = HashMap<String, Field>()

        for (field in clz.declaredFields) {
            val col = field.getAnnotation(Column::class.java) ?: continue
            val col_name = col.value
            if (col_name == null || col_name!!.trim { it <= ' ' } == "") {
                continue
            }

            columns.add(col_name)
            map[col_name] = field

            try {
                field.isAccessible = true
                val col_val = field.get(entity) ?: continue
                when (col_val) {
                    is String -> values.put(col_name, col_val)
                    is Long -> values.put(col_name, col_val)
                    is Int -> values.put(col_name, col_val)
                    is Float -> values.put(col_name, col_val)
                    is Double -> values.put(col_name, col_val)
                    is Double -> values.put(col_name, col_val)
                    is Boolean -> values.put(col_name, col_val)
                    else -> values.put(col_name, col_val.toString())
                }
            } catch (e: IllegalAccessException) {
                Log.e("console", e.localizedMessage, e)
                continue
            }

        }

        var id: String? = entity!!.id
        if (id == null) {
            id = UUID.randomUUID().toString()
            values.put("id", id)
            val ins_res = insertOrThrow(table_name, null, values)
            Log.v("console", "insert rows:$ins_res")
        } else {
            val upd_res = update(table_name, values, "id=?", arrayOf(id)).toLong()
            Log.v("console", "update rows:$upd_res")
        }
        val field_arr = columns.toTypedArray()
        columns.toTypedArray()
        var c: Cursor? = null
        try {
            c = query(table_name, field_arr, " id=? ", arrayOf(id), "", "", "")

            if (c!!.moveToNext()) {
                cusor2Entity(entity, columns, map, c)
            }else {
                val ins_res = insertOrThrow(table_name, null, values)
                Log.v("console", "insert rows:$ins_res")
            }
        } catch (e: Exception) {
            Log.e("console", e.localizedMessage, e)
        } finally {
            if (c != null) {
                c!!.close()
            }
        }



        return entity
    }

    private fun obtainEntityTableName(clz: Class<*>): String? {
        val ann_table = clz.getAnnotation(Table::class.java) ?: return null

        return ann_table.value
    }

}