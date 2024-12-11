package com.example.grocerylistapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Create grocery lists table
        val createListTableQuery = "CREATE TABLE $TABLE_GROCERY_LIST (" +
                "$COLUMN_LIST_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_LIST_NAME TEXT)"
        db.execSQL(createListTableQuery)

        // Create grocery items table
        val createItemTableQuery = "CREATE TABLE $TABLE_GROCERY_ITEM (" +
                "$COLUMN_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_ITEM_LIST_ID INTEGER, " +
                "$COLUMN_ITEM_NAME TEXT, " +
                "$COLUMN_ITEM_PRICE REAL, " +
                "$COLUMN_ITEM_STORE TEXT)"
        db.execSQL(createItemTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "grocery.db"

        // Constants for grocery list table
        internal const val TABLE_GROCERY_LIST = "grocery_list"
        internal const val COLUMN_LIST_ID = "_id"
        internal const val COLUMN_LIST_NAME = "list_name"

        // Constants for grocery item table
        internal const val TABLE_GROCERY_ITEM = "grocery_item"
        private const val COLUMN_ITEM_ID = "_id"
        internal const val COLUMN_ITEM_NAME = "item_name"
        internal const val COLUMN_ITEM_PRICE = "item_price"
        internal const val COLUMN_ITEM_STORE = "item_store"
        internal const val COLUMN_ITEM_LIST_ID = "list_id"
    }

    fun insertGroceryItem(listId: Long, name: String, price: String, store: String) {
        val db = writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ITEM_LIST_ID, listId)
        contentValues.put(COLUMN_ITEM_NAME, name)
        contentValues.put(COLUMN_ITEM_PRICE, price)
        contentValues.put(COLUMN_ITEM_STORE, store)
        db.insert(TABLE_GROCERY_ITEM, null, contentValues)
    }

    fun deleteItemsForGroceryList(listId: Long) {
        val db = writableDatabase
        val selection = "$COLUMN_ITEM_LIST_ID = ?"
        val selectionArgs = arrayOf(listId.toString())
        db.delete(TABLE_GROCERY_ITEM, selection, selectionArgs)
    }
    @SuppressLint("Range")
    internal fun getListId(database: SQLiteDatabase, groceryList: GroceryList): Long {
        val cursor = database.query(
            DatabaseHelper.TABLE_GROCERY_LIST,
            arrayOf(DatabaseHelper.COLUMN_LIST_ID),
            "${DatabaseHelper.COLUMN_LIST_NAME}=?",
            arrayOf(groceryList.name),
            null,
            null,
            null
        )

        var listId: Long = -1
        if (cursor.moveToFirst()) {
            listId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_LIST_ID))
        }

        cursor.close()
        return listId
    }

        fun deleteGroceryList(listId: Long): Boolean {
            val db = writableDatabase

            // Delete the grocery list from the grocery_lists table
            val groceryListDeleted = db.delete(
                TABLE_GROCERY_LIST,
                "$COLUMN_LIST_ID = ?",
                arrayOf(listId.toString())
            ) > 0

            if (!groceryListDeleted) {
                return false
            }

            // Delete all grocery items associated with the list from the grocery_items table
            val groceryItemsDeleted = db.delete(
                TABLE_GROCERY_ITEM,
                "$COLUMN_ITEM_LIST_ID = ?",
                arrayOf(listId.toString())
            ) > 0

            db.close()

            return groceryItemsDeleted
        }

}

