package com.example.grocerylistapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class GroceryListsActivity : AppCompatActivity() {

    private lateinit var listViewGroceryLists: ListView
    private lateinit var adapter: ArrayAdapter<GroceryList>
    private var groceryLists: ArrayList<GroceryList> = ArrayList()
    private lateinit var btnNewGroceryList: Button

    private var isExistingList: Boolean = false
    private var groceryListId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery_lists)

        val dbHelper = DatabaseHelper(this)
        val database = dbHelper.writableDatabase

        listViewGroceryLists = findViewById(R.id.listViewGroceryLists)
        btnNewGroceryList = findViewById(R.id.btnNewGroceryList)


        adapter = GroceryListAdapter(this, R.layout.list_item_grocery_list, groceryLists)
        listViewGroceryLists.adapter = adapter


        loadGroceryListsFromDatabase()
        listViewGroceryLists.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedList = adapter.getItem(position)
                selectedList?.let {
                    val groceryListId = it.id // Get the correct groceryListId from the selected GroceryList object
                    val intent = Intent(this@GroceryListsActivity, GroceryListEditDetailsActivity::class.java)
                    intent.putExtra("groceryListId", groceryListId)
                    startActivity(intent)
                }
            }

        btnNewGroceryList.setOnClickListener {
            val newListName = "New List"

            val intent = Intent(this@GroceryListsActivity, GroceryListEditDetailsActivity::class.java)
            intent.putExtra("newListName", newListName)
            intent.putExtra("isNewList", true);
            startActivity(intent)
        }




    }


    override fun onResume() {
        super.onResume()
        loadGroceryListsFromDatabase()
    }


    @SuppressLint("Range")
    private fun loadGroceryListsFromDatabase() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_GROCERY_LIST,
            arrayOf(DatabaseHelper.COLUMN_LIST_ID, DatabaseHelper.COLUMN_LIST_NAME),
            null,
            null,
            null,
            null,
            null
        )

        val groceryLists = ArrayList<GroceryList>()

        if (cursor.moveToFirst()) {
            do {
                val listId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_LIST_ID))
                val listName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LIST_NAME))
                groceryLists.add(GroceryList(listId, listName))
            } while (cursor.moveToNext())
        }

        cursor.close()


        adapter.clear()
        adapter.addAll(groceryLists)
    }





}
