package com.example.grocerylistapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class GroceryListAdapter(context: Context, resource: Int, objects: ArrayList<GroceryList>) :
    ArrayAdapter<GroceryList>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        val groceryList = getItem(position)
        view.findViewById<TextView>(android.R.id.text1)?.text = groceryList?.name

        return view
    }
}
