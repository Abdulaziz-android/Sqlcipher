package com.abdulaziz.sqlcipher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.abdulaziz.sqlcipher.db.DBHelper
import net.sqlcipher.database.SQLiteDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var database: SQLiteDatabase
    private lateinit var dbHelper: DBHelper
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        SQLiteDatabase.loadLibs(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)
        try {
            dbHelper.createDatabase()
            dbHelper.openDatabase()
            dbHelper.close()
            database = dbHelper.getReadableDatabase(DBHelper.PASSWORD)
        }catch (e:Exception){
            Log.d(TAG, "onCreate: ${e.message}")
        }
        loadData()
    }

    private fun loadData() {
        val data = DBHelper(this).getInstance(this).getData()
        Log.d(TAG, "loadData: $data")
        val textView = findViewById<TextView>(R.id.text_view)
        textView.text = data.toString()
    }
}