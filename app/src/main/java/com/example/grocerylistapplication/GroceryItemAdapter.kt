package com.example.grocerylistapplication

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class GroceryItemAdapter(
    context: Context,
    resource: Int,
    private val groceryItems: ArrayList<GroceryItem>,
    private val onItemEdited: (ArrayList<GroceryItem>) -> Unit // Callback function
) : ArrayAdapter<GroceryItem>(context, resource, groceryItems) {

    // ViewHolder pattern to optimize ListView performance
    private class ViewHolder {
        lateinit var itemName: TextView
        lateinit var itemPrice: TextView
        lateinit var itemStore: TextView
        lateinit var checkBoxItem: CheckBox
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val viewHolder: ViewHolder

        if (itemView == null) {
            itemView =
                LayoutInflater.from(context).inflate(R.layout.list_item_grocery, parent, false)
            viewHolder = ViewHolder()
            viewHolder.itemName = itemView.findViewById(R.id.textViewItemName)
            viewHolder.itemPrice = itemView.findViewById(R.id.textViewItemPrice)
            viewHolder.itemStore = itemView.findViewById(R.id.textViewItemStore)
            viewHolder.checkBoxItem = itemView.findViewById(R.id.checkBoxItem)
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }

        val currentItem = groceryItems[position]

        viewHolder.itemName.text = currentItem.name
        viewHolder.itemPrice.text = currentItem.price
        viewHolder.itemStore.text = currentItem.store

        // Handle checkbox state and item crossing out
        viewHolder.checkBoxItem.isChecked = currentItem.isCrossedOut
        viewHolder.checkBoxItem.setOnClickListener {
            currentItem.isCrossedOut = viewHolder.checkBoxItem.isChecked
            onItemEdited(groceryItems) // Notify the activity about the changes
            updateItemView(viewHolder, currentItem) // Update the view for the current item
        }

        // Update the view for the current item
        updateItemView(viewHolder, currentItem)

        val btnDeleteItem = itemView?.findViewById<Button>(R.id.btnDeleteItem)
        val btnEditItem = itemView?.findViewById<Button>(R.id.btnEditItem)
        // Handle delete button click for each item
        if (btnDeleteItem != null) {
            btnDeleteItem.setOnClickListener {
                groceryItems.removeAt(position)
                notifyDataSetChanged()
                onItemEdited(groceryItems) // Notify the activity about the changes
            }
        }

        // Handle edit button click for each item
        if (btnEditItem != null) {
            btnEditItem.setOnClickListener {
                // Open a dialog or activity to edit the item details
                openEditItemDialog(currentItem, position)
            }
        }

        return itemView!!
    }

    private fun openEditItemDialog(item: GroceryItem, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_item, null)
        val editTextItemName = dialogView.findViewById<EditText>(R.id.editTextEditItemName)
        val editTextItemPrice = dialogView.findViewById<EditText>(R.id.editTextEditItemPrice)
        val editTextItemStore = dialogView.findViewById<EditText>(R.id.editTextEditItemStore)

        editTextItemName.setText(item.name)
        editTextItemPrice.setText(item.price)
        editTextItemStore.setText(item.store)

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setTitle("Edit Item")
            .setPositiveButton("Save") { _, _ ->
                val newName = editTextItemName.text.toString().trim()
                val newPrice = editTextItemPrice.text.toString().trim()
                val newStore = editTextItemStore.text.toString().trim()

                if (newName.isNotEmpty() && newPrice.isNotEmpty() && newStore.isNotEmpty()) {
                    val editedItem = GroceryItem(newName, newPrice, newStore)
                    groceryItems[position] = editedItem
                    notifyDataSetChanged()
                    onItemEdited(groceryItems)
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun updateItemView(viewHolder: ViewHolder, item: GroceryItem) {
        if (item.isCrossedOut) {
            viewHolder.itemName.paintFlags =
                viewHolder.itemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            viewHolder.itemPrice.paintFlags =
                viewHolder.itemPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            viewHolder.itemStore.paintFlags =
                viewHolder.itemStore.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            viewHolder.itemName.paintFlags =
                viewHolder.itemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            viewHolder.itemPrice.paintFlags =
                viewHolder.itemPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            viewHolder.itemStore.paintFlags =
                viewHolder.itemStore.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}

