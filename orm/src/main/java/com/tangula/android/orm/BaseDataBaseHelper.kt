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

/**
 * The interface which defined how to create a database.
 * @author Dose &middot; King &lt;doss.king@outlook.com&gt;
 */
interface DbCreator {
    /**
     * Obtain the database's name.
     * @return database name.
     */
    fun getDataBaseName(): String

    /**
     * Obtain the database's version.
     */
    fun getDataBaseVersion(): Int

    /**
     * Callback function when database not exists.
     */
    fun onCreate(db: SQLiteDatabase) {}

    /**
     * Callback function when database exists and it's version is lower than current version.
     */
    fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}

/**
 * Database Helper with basic features.
 * @author Dose &middot; King &lt;doss.king@outlook.com&gt;
 */
abstract class BaseDataBaseHelper<T : Entity<String>> {


    companion object {

        /**
         * The helper singleton instance.
         */
        @JvmStatic
        private var HELPER: SQLiteOpenHelper? = null

        /**
         * The database singleton instance.
         */
        @JvmStatic
        private var DB: SQLiteDatabase? = null

        /**
         * The context supplier function.
         *
         * When the helper create the database, it would use this function's reutrn value as Context.
         */
        @JvmStatic
        var CONTEXT_FAC: ContextSupplier? = null

        /**
         * The DbCreator instance.
         */
        @JvmStatic
        private var DB_CREATOR: DbCreator? = null

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
                            Log.v("tgldb", "create database")
                            DB_CREATOR?.onCreate(db)
                        }

                        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                            Log.v("tgldb", "upgrade database")
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

        /**
         * Obtian the singleton database instance.
         *
         * If the database not exists, this function would create the database. So when you first invoke
         * it, the process would spend more time than normal.
         *
         * This function would initial HELP and DB property of this class's static fields.
         *
         */
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

        /**
         * This is a proxy function for {SQLiteDatabase::beginTransaction}.
         */
        @JvmStatic
        protected fun beginTransaction() {
            getDatabase()!!.beginTransaction()
        }

        /**
         * This is a proxy function for {SQLiteDatabase::beginTransactionNonExclusive}.
         */
        @JvmStatic
        protected fun beginTransactionNonExclusive() {
            getDatabase()!!.beginTransactionNonExclusive()
        }

        /**
         * This is a proxy function for {SQLiteDatabase::beginTransactionWithListener}.
         */
        @JvmStatic
        protected fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) {
            getDatabase()!!.beginTransactionWithListener(transactionListener)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::beginTransactionWithListenerNonExclusive}.
         */
        @JvmStatic
        protected fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) {
            getDatabase()!!.beginTransactionWithListenerNonExclusive(transactionListener)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::endTransaction}.
         */
        @JvmStatic
        protected fun endTransaction() {
            getDatabase()!!.endTransaction()
        }

        /**
         * This is a proxy function for {SQLiteDatabase::setTransactionSuccessful}.
         */
        @JvmStatic
        protected fun setTransactionSuccessful() {
            getDatabase()!!.setTransactionSuccessful()
        }

        /**
         * This is a proxy function for {SQLiteDatabase::inTransaction}.
         */
        @JvmStatic
        protected fun inTransaction(): Boolean {
            return getDatabase()!!.inTransaction()
        }

        /**
         * This is a proxy function for {SQLiteDatabase::getVersion}.
         */
        @JvmStatic
        protected fun getVersion(): Int {
            return getDatabase()!!.version
        }

        /**
         * This is a proxy function for {SQLiteDatabase::setVersion}.
         */
        @JvmStatic
        protected fun setVersion(version: Int) {
            getDatabase()!!.version = version
        }

        /**
         * This is a proxy function for {SQLiteDatabase::SQLException}.
         */
        @JvmStatic
        @Throws(SQLException::class)
        protected fun compileStatement(sql: String): SQLiteStatement {
            return getDatabase()!!.compileStatement(sql)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::query}.
         */
        @JvmStatic
        protected fun query(distinct: Boolean, table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String): Cursor {
            return getDatabase()!!.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::query}.
         */
        @JvmStatic
        protected fun query(distinct: Boolean, table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String, cancellationSignal: CancellationSignal): Cursor {
            return getDatabase()!!.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::queryWithFactory}.
         */
        @JvmStatic
        protected fun queryWithFactory(cursorFactory: SQLiteDatabase.CursorFactory, distinct: Boolean, table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String): Cursor {
            return getDatabase()!!.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::queryWithFactory}.
         */
        @JvmStatic
        protected fun queryWithFactory(cursorFactory: SQLiteDatabase.CursorFactory, distinct: Boolean, table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String, cancellationSignal: CancellationSignal): Cursor {
            return getDatabase()!!.queryWithFactory(cursorFactory, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::query}.
         */
        @JvmStatic
        protected fun query(table: String?, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String): Cursor {
            return getDatabase()!!.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::query}.
         */
        @JvmStatic
        protected fun query(table: String, columns: Array<String>, selection: String, selectionArgs: Array<String>, groupBy: String, having: String, orderBy: String, limit: String): Cursor {
            return getDatabase()!!.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::rawQuery}.
         */
        @JvmStatic
        protected fun rawQuery(sqlResId: Int, selectionArgs: Array<String>): Cursor {
            return rawQuery(CONTEXT_FAC!!.get().getString(sqlResId), selectionArgs)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::rawQuery}.
         */
        @JvmStatic
        protected fun rawQuery(sql: String, selectionArgs: Array<String>): Cursor {
            return getDatabase()!!.rawQuery(sql, selectionArgs)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::rawQuery}.
         */
        @JvmStatic
        protected fun rawQuery(sqlResId: Int, selectionArgs: Array<String>, cancellationSignal: CancellationSignal): Cursor {
            return rawQuery(CONTEXT_FAC!!.get().getString(sqlResId), selectionArgs, cancellationSignal)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::rawQuery}.
         */
        @JvmStatic
        protected fun rawQuery(sql: String, selectionArgs: Array<String>, cancellationSignal: CancellationSignal): Cursor {
            return getDatabase()!!.rawQuery(sql, selectionArgs, cancellationSignal)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::rawQueryWithFactory}.
         */
        @JvmStatic
        protected fun rawQueryWithFactory(cursorFactory: SQLiteDatabase.CursorFactory, sql: String, selectionArgs: Array<String>, editTable: String): Cursor {
            return getDatabase()!!.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::rawQueryWithFactory}.
         */
        @JvmStatic
        protected fun rawQueryWithFactory(cursorFactory: SQLiteDatabase.CursorFactory, sql: String, selectionArgs: Array<String>, editTable: String, cancellationSignal: CancellationSignal): Cursor {
            return getDatabase()!!.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable, cancellationSignal)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::insert}.
         */
        @JvmStatic
        protected fun insert(table: String, nullColumnHack: String, values: ContentValues): Long {
            return getDatabase()!!.insert(table, nullColumnHack, values)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::insertOrThrow}.
         */
        @JvmStatic
        @Throws(SQLException::class)
        protected fun insertOrThrow(table: String, nullColumnHack: String?, values: ContentValues): Long {
            return getDatabase()!!.insertOrThrow(table, nullColumnHack, values)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::replace}.
         */
        @JvmStatic
        protected fun replace(table: String, nullColumnHack: String, initialValues: ContentValues): Long {
            return getDatabase()!!.replace(table, nullColumnHack, initialValues)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::replaceOrThrow}.
         */
        @JvmStatic
        @Throws(SQLException::class)
        protected fun replaceOrThrow(table: String, nullColumnHack: String, initialValues: ContentValues): Long {
            return getDatabase()!!.replaceOrThrow(table, nullColumnHack, initialValues)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::insertWithOnConflict}.
         */
        @JvmStatic
        protected fun insertWithOnConflict(table: String, nullColumnHack: String, initialValues: ContentValues, conflictAlgorithm: Int): Long {
            return getDatabase()!!.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::delete}.
         */
        @JvmStatic
        protected fun delete(table: String, whereClause: String, whereArgs: Array<String>): Int {
            return getDatabase()!!.delete(table, whereClause, whereArgs)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::update}.
         */
        @JvmStatic
        protected fun update(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>): Int {
            return getDatabase()!!.update(table, values, whereClause, whereArgs)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::updateWithOnConflict}.
         */
        @JvmStatic
        protected fun updateWithOnConflict(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>, conflictAlgorithm: Int): Int {
            return getDatabase()!!.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::execSQL}.
         */
        @JvmStatic
        protected fun execSQL(resId: Int) {
            execSQL(CONTEXT_FAC!!.get().getString(resId))
        }

        /**
         * This is a proxy function for {SQLiteDatabase::execSQL}.
         */
        @JvmStatic
        @Throws(SQLException::class)
        protected fun execSQL(sql: String) {
            getDatabase()!!.execSQL(sql)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::execSQL}.
         */
        @JvmStatic
        protected fun execSQL(resId: Int, bindArgs: Array<Any>) {
            execSQL(CONTEXT_FAC!!.get().getString(resId, *bindArgs))
        }

        /**
         * This is a proxy function for {SQLiteDatabase::execSQL}.
         */
        @JvmStatic
        @Throws(SQLException::class)
        protected fun execSQL(sql: String, bindArgs: Array<Any>) {
            getDatabase()!!.execSQL(sql, bindArgs)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::validateSql}.
         */
        @JvmStatic
        protected fun validateSql(sql: String, cancellationSignal: CancellationSignal?) =//getDatabase().validateSql(sql, cancellationSignal);
                Unit

        /**
         * This is a proxy function for {SQLiteDatabase::isReadOnly}.
         */
        @JvmStatic
        protected fun isReadOnly(): Boolean {
            return getDatabase()!!.isReadOnly
        }

        /**
         * This is a proxy function for {SQLiteDatabase::isOpen}.
         */
        @JvmStatic
        protected fun isOpen(): Boolean {
            return getDatabase()!!.isOpen
        }

        /**
         * This is a proxy function for {SQLiteDatabase::needUpgrade}.
         */
        @JvmStatic
        protected fun needUpgrade(newVersion: Int): Boolean {
            return getDatabase()!!.needUpgrade(newVersion)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::setLocale}.
         */
        @JvmStatic
        protected fun setLocale(locale: Locale) {
            getDatabase()!!.setLocale(locale)
        }

        /**
         * This is a proxy function for {SQLiteDatabase::setMaxSqlCacheSize}.
         */
        @JvmStatic
        protected fun setMaxSqlCacheSize(cacheSize: Int) {
            getDatabase()!!.setMaxSqlCacheSize(cacheSize)
        }

        /**
         * create the table which be defined in the entity class.
         * @param[entityClz] the entity class, which defined the table.
         * @param[force] is force create table.
         * when force is true, it would drop the table if it exists. when force is false, and if
         * the table exists, would execute nothing.
         */
        @JvmStatic
        fun <T : Entity<String>> createTable(database: SQLiteDatabase, entityClz: Class<T>, force: Boolean){

            val clz_ann:Table? =  entityClz.getAnnotation(Table::class.java)
            if(clz_ann==null){
                return
            }


            val tablename: String = clz_ann.value ?: return

            val column_defs = StringBuffer("\tid text primary key") //default id column.

            for(f in entityClz.declaredFields){
                if(StringUtils.equals("id", f.name)){ //skip the id property, and id type is uuid..
                    continue
                }

                val field_ann: Column? = f.getAnnotation(Column::class.java) ?: continue

                val col_name = field_ann?.value
                val col_type = field_ann?.type

                if(col_name!=null&&col_type!=null){
                    column_defs.append(",\n\t$col_name ${col_type.name}")
                }

            }

            if(force){ // if force is true, drop the exists table first.
                database.execSQL("drop table if exists $tablename")
            }

            //execute the create sql of the table.
            database.execSQL("create table if not exists $tablename ($column_defs)")

        }

    }

    /**
     * Obtain the query reuslt,.
     */
    fun byCondition(condition: String, args: Array<String>, group: String, having: String, orderby: String): List<T> {
        val res = ArrayList<T>()

        val pt = javaClass.genericSuperclass as ParameterizedType
        val clazz = pt.actualTypeArguments[0] as Class<T>

        val columns = ArrayList<String>()
        val map = HashMap<String, Field>()

        for (field in clazz.declaredFields) {
            val col = field.getAnnotation(Column::class.java) ?: continue
            val colName = col.value
            if (colName.trim { it <= ' ' } == "") {
                continue
            }
            columns.add(colName)
            map[colName] = field
        }

        val fieldArr = columns.toTypedArray()
        columns.toTypedArray()
        var c: Cursor? = null
        try {
            val tableName = obtainEntityTableName(clazz)
            c = query(tableName, fieldArr, condition, args, group, having, orderby)
            while (c.moveToNext()) {
                try {
                    val entity = clazz.newInstance()
                    cusor2Entity(entity, columns, map, c)
                    res.add(entity)
                } catch (e: InstantiationException) {
                    Log.e("tgldb", e.localizedMessage, e)
                    continue
                } catch (e: IllegalAccessException) {
                    Log.e("tgldb", e.localizedMessage, e)
                    continue
                }

            }
        } catch (e: Exception) {
            Log.e("tgldb", e.localizedMessage, e)
        } finally {
            c?.close()
        }
        return res
    }

    /**
     * .
     */
    fun byId(id: String): T? {
        var entity: T? = null

        val pt = javaClass.genericSuperclass as ParameterizedType
        val clazz = pt.actualTypeArguments[0] as Class<T>

        val columns = ArrayList<String>()
        val map = HashMap<String, Field>()

        for (field in clazz.declaredFields) {
            val col = field.getAnnotation(Column::class.java) ?: continue
            val colName = col.value
            if (colName.trim { it <= ' ' } == "") {
                continue
            }

            columns.add(colName)
            map[colName] = field
        }

        val fieldArr = columns.toTypedArray()
        columns.toTypedArray()
        var c: Cursor? = null
        try {
            val tableName = obtainEntityTableName(clazz)
            c = query(tableName, fieldArr, " id=? ", arrayOf(id), "", "", "")
            if (c.moveToNext()) {
                try {
                    entity = clazz.newInstance()
                } catch (e: InstantiationException) {
                    Log.e("tgldb", e.localizedMessage, e)
                    return null
                } catch (e: IllegalAccessException) {
                    Log.e("tgldb", e.localizedMessage, e)
                    return null
                }

                cusor2Entity(entity, columns, map, c)
            }
        } catch (e: Exception) {
            Log.e("tgldb", e.localizedMessage, e)
        } finally {
            c?.close()
        }

        return entity
    }

    /**
     * .
     */
    private fun cusor2Entity(entity: T?, columns: List<String>, map: Map<String, Field>, c: Cursor) {
        for (col_name in columns) {
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

    /**
     * .
     */
    fun delete(entity: T?): Int {
        var res = 0
        if (entity == null || StringUtils.isBlank(entity.id)) {
            Log.v("tgldb","remove null or entity's id is null")
            return res
        }

        val clz = entity.javaClass

        val annTable = clz.getAnnotation(Table::class.java)
        if (annTable == null) {
            Log.e("tgldb", clz.name + " is not annotation " + Table::class.java.name)
            return res
        }

        val tableName = annTable.value

        res = delete(tableName, "id = ?", arrayOf(entity.id))

        Log.v("tgldb","remove $tableName which's id is $entity.id")

        return res
    }


    /**
     * .
     */
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
            val colName = col.value
            if (colName == null || colName!!.trim { it <= ' ' } == "") {
                continue
            }

            columns.add(colName)
            map[colName] = field

            try {
                field.isAccessible = true
                val colVal = field.get(entity) ?: continue
                when (colVal) {
                    is String -> values.put(colName, colVal)
                    is Long -> values.put(colName, colVal)
                    is Int -> values.put(colName, colVal)
                    is Float -> values.put(colName, colVal)
                    is Double -> values.put(colName, colVal)
                    is Double -> values.put(colName, colVal)
                    is Boolean -> values.put(colName, colVal)
                    else -> values.put(colName, colVal.toString())
                }
            } catch (e: IllegalAccessException) {
                Log.e("tgldb", e.localizedMessage, e)
                continue
            }

        }

        var id: String? = entity!!.id
        if (id == null) {
            id = UUID.randomUUID().toString()
            values.put("id", id)
            val insRes = insertOrThrow(table_name, null, values)
            Log.v("tgldb", "insert rows:$insRes")
        } else {
            val upd_res = update(table_name, values, "id=?", arrayOf(id)).toLong()
            Log.v("tgldb", "update rows:$upd_res")
        }
        val fieldArr = columns.toTypedArray()
        columns.toTypedArray()
        var cursor: Cursor? = null
        try {
            cursor = query(table_name, fieldArr, " id=? ", arrayOf(id), "", "", "")

            if (cursor.moveToNext()) {
                cusor2Entity(entity, columns, map, cursor)
            }else {
                val insRes = insertOrThrow(table_name, null, values)
                Log.v("tgldb", "insert rows:$insRes")
            }
        } catch (e: Exception) {
            Log.e("tgldb", e.localizedMessage, e)
        } finally {
            cursor?.close()
        }



        return entity
    }

    /**
     * .
     */
    private fun obtainEntityTableName(clz: Class<*>): String {
        return clz.getAnnotation(Table::class.java).value
    }

}