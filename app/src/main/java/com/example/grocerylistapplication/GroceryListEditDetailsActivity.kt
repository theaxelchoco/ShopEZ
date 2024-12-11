package com.example.grocerylistapplication
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MenuInflater
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GroceryListEditDetailsActivity : AppCompatActivity() {

    private lateinit var listViewGroceryItems: ListView
    private var groceryItems: ArrayList<GroceryItem> = ArrayList()
    private lateinit var adapter: ArrayAdapter<GroceryItem>
    private lateinit var groceryListName: String
    private lateinit var etGroceryListName: EditText
    private lateinit var editTextItemName: EditText
    private lateinit var editTextItemPrice: EditText
    private lateinit var editTextItemStore: EditText
    private var groceryListId: Long = -1L

    private lateinit var vibrator: Vibrator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_grocery_list_details)

        val dbHelper = DatabaseHelper(this)
        val database = dbHelper.writableDatabase

        listViewGroceryItems = findViewById(R.id.listViewGroceryItems)
        val layoutListName: LinearLayout = findViewById(R.id.layoutListName)
        val textViewListName: TextView = findViewById(R.id.textViewListName)
        etGroceryListName = findViewById(R.id.etGroceryListName)
        val isNewList = intent.getBooleanExtra("isNewList", false)



        groceryListId = intent.getLongExtra("groceryListId", -1L)
        val intent = intent
        groceryListName = intent.getStringExtra("groceryListName") ?: ""



        adapter = GroceryItemAdapter(this, R.layout.list_item_grocery, groceryItems) {
            saveGroceryList()
        }
        listViewGroceryItems.adapter = adapter

        loadGroceryItems(database)

        // Set the actual list name as the text for the TextView
        textViewListName.text = groceryListName

        // Initialize the vibrator
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator



        if (isNewList) {
            textViewListName.visibility = View.GONE
            etGroceryListName.visibility = View.VISIBLE
        }

        // Handle the click event for the list name
        layoutListName.setOnClickListener {
            // Hide the TextView and show the EditText to allow editing
            textViewListName.visibility = View.GONE
            etGroceryListName.visibility = View.VISIBLE

            // Set the current list name as the initial text in the EditText
            etGroceryListName.setText(groceryListName)

            // Set the cursor position at the end of the text
            etGroceryListName.setSelection(etGroceryListName.text.length)

            // Request focus on the EditText to bring up the keyboard for editing
            etGroceryListName.requestFocus()
        }


        val fabShareList: FloatingActionButton = findViewById(R.id.fabOptions)
        fabShareList.setOnClickListener {
            showPopupMenu(it)
        }

        // Set the grocery list name in the EditText when an existing list is opened
        if (groceryListId != -1L) {
            etGroceryListName.setText(groceryListName)
        }



        listViewGroceryItems.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedItem = adapter.getItem(position)
                selectedItem?.let {
                }
            }
        listViewGroceryItems.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val selectedItem = adapter.getItem(position)
                selectedItem?.let {
                    deleteGroceryItem(database, it, groceryListName)
                    groceryItems.removeAt(position)
                    adapter.notifyDataSetChanged()
                    performHapticFeedback()
                    saveGroceryList()
                }
                true
            }

    }


    private fun performHapticFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    fun onDeleteItemClick(view: View) {
        val parentView = view.parent as View
        val position = listViewGroceryItems.getPositionForView(parentView)
        if (position != AdapterView.INVALID_POSITION) {
            groceryItems.removeAt(position)
            adapter.notifyDataSetChanged()
        }
    }


    private fun saveGroceryList() {
        val groceryListName = etGroceryListName.text.toString().trim()

        if (groceryListName.isEmpty()) {
            Toast.makeText(this, "Please enter a list name", Toast.LENGTH_SHORT).show()
            return
        }

        if (groceryItems.isEmpty()) {
            Toast.makeText(this, "Please add items to the list", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase


        // Check if this is a new list or an existing list update
        val isNewList = groceryListId == -1L

        if (isNewList) {
            // Save a new grocery list to the database
            val contentValues = ContentValues()
            contentValues.put(DatabaseHelper.COLUMN_LIST_NAME, groceryListName)

            // Insert the new grocery list into the database
            val id = db.insert(DatabaseHelper.TABLE_GROCERY_LIST, null, contentValues)
            if (id == -1L) {
                Toast.makeText(this, "Error saving the grocery list", Toast.LENGTH_SHORT).show()
                return
            }

            // Save the ID of the newly created grocery list
            groceryListId = id
        } else {
            // Update an existing grocery list in the database
            val contentValues = ContentValues()
            contentValues.put(DatabaseHelper.COLUMN_LIST_NAME, groceryListName)

            val selection = "${DatabaseHelper.COLUMN_LIST_ID} = ?"
            val selectionArgs = arrayOf(groceryListId.toString())

            // Update the grocery list name in the database
            val rowsUpdated = db.update(
                DatabaseHelper.TABLE_GROCERY_LIST,
                contentValues,
                selection,
                selectionArgs
            )

            if (rowsUpdated <= 0) {
                Toast.makeText(this, "Error updating the grocery list", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Save the grocery items for the current list in the database
        dbHelper.deleteItemsForGroceryList(groceryListId) // Clear existing items for this list

        for (item in groceryItems) {
            dbHelper.insertGroceryItem(groceryListId, item.name, item.price, item.store)
        }


        Toast.makeText(this, "Grocery list saved successfully", Toast.LENGTH_SHORT).show()
    }


    @SuppressLint("Range")
    private fun loadGroceryItems(database: SQLiteDatabase) {
        groceryItems.clear()

        // Fetch the grocery list name from the database using the groceryListId
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        var cursor = db.query(
            DatabaseHelper.TABLE_GROCERY_LIST,
            arrayOf(DatabaseHelper.COLUMN_LIST_NAME),
            "${DatabaseHelper.COLUMN_LIST_ID}=?",
            arrayOf(groceryListId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val listName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LIST_NAME))
            groceryListName = listName // Set the grocery list name in the EditText
        }

        cursor.close()

        // Load grocery items for the current list
        cursor = database.query(
            DatabaseHelper.TABLE_GROCERY_ITEM,
            arrayOf(
                DatabaseHelper.COLUMN_ITEM_NAME,
                DatabaseHelper.COLUMN_ITEM_PRICE,
                DatabaseHelper.COLUMN_ITEM_STORE
            ),
            "${DatabaseHelper.COLUMN_ITEM_LIST_ID}=?",
            arrayOf(groceryListId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val itemName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_NAME))
                val itemPrice = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_PRICE))
                val itemStore = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_STORE))
                val groceryItem = GroceryItem(itemName, itemPrice, itemStore)
                groceryItems.add(groceryItem)
            } while (cursor.moveToNext())
        }

        cursor.close()

        adapter.notifyDataSetChanged()
    }


    private fun addOrUpdateGroceryItem() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val itemNameEditText = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val itemPriceEditText = dialogView.findViewById<EditText>(R.id.editTextItemPrice)
        val itemStoreEditText = dialogView.findViewById<EditText>(R.id.editTextItemStore)
        val saveButton = dialogView.findViewById<Button>(R.id.btnSaveItem)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)

        val dialog = dialogBuilder.create()

        saveButton.setOnClickListener {
            val itemName = itemNameEditText.text.toString().trim()
            val itemPrice = itemPriceEditText.text.toString().trim()
            val itemStore = itemStoreEditText.text.toString().trim()

            if (itemName.isNotEmpty() && itemPrice.isNotEmpty() && itemStore.isNotEmpty()) {
                val newItem = GroceryItem(itemName, itemPrice, itemStore)

                groceryItems.add(newItem)

                adapter.notifyDataSetChanged()

                itemNameEditText.text.clear()
                itemPriceEditText.text.clear()
                itemStoreEditText.text.clear()

                saveGroceryList()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    fun deleteGroceryItem(database: SQLiteDatabase, groceryItem: GroceryItem, groceryListName: String) {
        val listId = getListId(database, groceryListName)
        if (listId != -1) {
            database.delete(
                DatabaseHelper.TABLE_GROCERY_ITEM,
                "${DatabaseHelper.COLUMN_ITEM_LIST_ID}=? AND ${DatabaseHelper.COLUMN_ITEM_NAME}=?",
                arrayOf(listId.toString(), groceryItem.name)
            )
        }
    }



    private fun getListId(database: SQLiteDatabase, groceryListName: String): Int {
        val cursor = database.query(
            DatabaseHelper.TABLE_GROCERY_LIST,
            arrayOf(DatabaseHelper.COLUMN_LIST_ID),
            "${DatabaseHelper.COLUMN_LIST_NAME}=?",
            arrayOf(groceryListName),
            null,
            null,
            null
        )

        var listId = -1
        try {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LIST_ID)
                if (columnIndex != -1) {
                    listId = cursor.getInt(columnIndex)
                }
            }
        } finally {
            cursor.close()
        }

        return listId
    }

    private fun shareGroceryList(anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.inflate(R.menu.menu_share)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_share_email -> {
                    shareViaEmail()
                    true
                }
                R.id.action_share_sms -> {
                    shareViaSMS()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }





    private fun shareViaEmail() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "My Grocery List")
        intent.putExtra(Intent.EXTRA_TEXT, getGroceryListDetails())
        startActivity(Intent.createChooser(intent, "Share via Email"))
    }



    private fun shareViaSMS() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("smsto:")
        intent.putExtra("sms_body", getGroceryListDetails())
        startActivity(intent)
    }



    private fun getGroceryListDetails(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Grocery List: ${etGroceryListName.text}\n\n")
        for (item in groceryItems) {
            stringBuilder.append("${item.name} - ${item.price}\n")
        }
        return stringBuilder.toString()
    }




    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter_store, null)
        val editTextStoreName = dialogView.findViewById<EditText>(R.id.editTextStoreName)

        AlertDialog.Builder(this)
            .setTitle("Filter by Store Name")
            .setView(dialogView)
            .setPositiveButton("Filter") { _, _ ->
                val storeName = editTextStoreName.text.toString().trim()
                filterByStore(storeName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }





    private fun filterByStore(storeName: String) {
        if (storeName.isNotEmpty()) {
            val filteredList = groceryItems.filter { it.store.equals(storeName, ignoreCase = true) }
            adapter.clear()
            adapter.addAll(filteredList)
            adapter.notifyDataSetChanged()
        }
    }




    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.list_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_item -> {
                    addOrUpdateGroceryItem()
                    true
                }
                R.id.action_save_list -> {
                    saveGroceryList()
                    true
                }
                R.id.action_delete_list -> {
                    showDeleteConfirmationDialog()
                    true
                }
                R.id.action_filter_list ->{
                    showFilterDialog()
                    true
                }

                R.id.action_share_list -> {
                    shareGroceryList(view)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }






    private fun deleteGroceryList() {
        val dbHelper = DatabaseHelper(this)
        val deleted = dbHelper.deleteGroceryList(groceryListId)

        if (deleted) {
            Toast.makeText(this, "Grocery list deleted successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error deleting the grocery list", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete List")
            .setMessage("Are you sure you want to delete this grocery list?")
            .setPositiveButton("Delete") { _, _ ->
                deleteGroceryList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



}
