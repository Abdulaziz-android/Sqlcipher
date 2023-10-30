package com.abdulaziz.sqlcipher.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.widget.Toast
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import net.sqlcipher.database.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

private val DB_NAME = "kodlangan.db" // Database name
private val DB_VERSION = 1 // Database version
private val TABLE_NAME = "person"
private val COLUMN_NAME = "name"


class DBHelper(private val context: Context) : SQLiteOpenHelper(
    context,
    DB_NAME,
    null,
    DB_VERSION
) {

    companion object {
        val PASSWORD = "kod"
    }
    private val TAG = "DataBaseHelper" // Tag just for the LogCat window
    private var instance: DBHelper? = null
    private var DB_PATH = "" // Database path

    private var database: SQLiteDatabase? = null

/*
    CREATE TABLE "person" (
    "id"	INTEGER NOT NULL,
    "name"	TEXT NOT NULL,
    PRIMARY KEY("id" AUTOINCREMENT)
    );
*/

    private val SQL_CREATE_TABLE_QUERY =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (id INTEGER NOT NULL, $COLUMN_NAME TEXT NOT NULL, PRIMARY KEY(id AUTOINCREMENT))"

    private val SQL_DELETE_TABLE_QUERY=
        "DROP TABLE IF EXISTS $TABLE_NAME"

    init {
        DB_PATH = context.applicationInfo.dataDir + "/databases/"
    }

    @Synchronized
    fun getInstance(context: Context): DBHelper {
        Log.d(TAG, "getInstance: ")
        if (instance == null) instance = DBHelper(context)
        return instance!!
    }

    @Throws(IOException::class)
    fun createDatabase() {
        val isExistingDB = checkExistingDB()
        if (!isExistingDB) {
            this.getReadableDatabase(PASSWORD)
            this.close()
            try {
                copyDatabase()
            } catch (e: Exception) {
                Log.d(TAG, "createDatabase: ${e.message}")
            }
        }
    }

    private fun copyDatabase() {
        var input: InputStream?
        try {
            input = context.assets.open(DB_NAME)
            val outputFileName = DB_PATH + DB_NAME
            val outputStream = FileOutputStream(outputFileName)
            val mBuffer = ByteArray(1024)
            var length: Int
            while (input.read(mBuffer).also { length = it } > 0) {
                outputStream.write(mBuffer, 0, length)
            }
            outputStream.flush()
            outputStream.close()
            input.close()
        } catch (e: Exception) {
            Log.d(TAG, "copyDatabase: ${e.message}")
        }
    }

    private fun checkExistingDB(): Boolean {
        val dbFile = File(DB_PATH + DB_NAME)
        return dbFile.exists()
    }

    @Throws(SQLiteException::class)
    fun openDatabase(): Boolean {
        val path = DB_PATH + DB_NAME
        database =
            SQLiteDatabase.openDatabase(path, PASSWORD, null, SQLiteDatabase.CREATE_IF_NECESSARY)
        return database != null
    }

    override fun close() {
        database?.close()
        super.close()
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL(SQL_CREATE_TABLE_QUERY);
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL(SQL_DELETE_TABLE_QUERY);
        onCreate(p0)
    }

    fun insertNewPerson(name:String){
        val db = instance?.getWritableDatabase(PASSWORD)
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        db?.insert(TABLE_NAME, null,values)
        db?.close()
    }

    @SuppressLint("Range")
    fun getData() : List<String>{
        //getInstance(context)

        if (instance == null) instance = DBHelper(context)
        val db = instance?.getWritableDatabase(PASSWORD)

        Log.d(TAG, "getData: instance = $instance")
        val cursor = db?.rawQuery("SELECT * FROM $TABLE_NAME", null)
        val names = arrayListOf<String>()
        if (cursor?.moveToFirst() == true){
            while (!cursor.isAfterLast){
                Log.d(TAG, "getData: ${cursor.count}")
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                Log.d(TAG, "getData: $name")
                names.add(name)
                cursor.moveToNext()
            }
        }
        cursor?.close()
        db?.close()
        return names
    }
}