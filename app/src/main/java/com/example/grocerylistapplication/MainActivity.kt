package com.example.grocerylistapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGroceryLists: Button = findViewById(R.id.btnGroceryLists)

        btnGroceryLists.setOnClickListener {
            val intent = Intent(this, GroceryListsActivity::class.java)
            startActivity(intent)
        }

        val btnAboutUs: Button = findViewById(R.id.btnAboutUs)
        btnAboutUs.setOnClickListener {
            val intent = Intent(this, AboutUsActivity::class.java)
            startActivity(intent)
        }

        val btnExit: Button = findViewById(R.id.btnExit)

        btnExit.setOnClickListener {
            exitApp()
        }
    }

    fun onCreateNewListButtonClick(view: View) {
        val intent = Intent(this, GroceryListEditDetailsActivity::class.java)
        intent.putExtra("isNewList", true);
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_about_us, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about_us -> {
                startActivity(Intent(this, AboutUsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun exitApp() {
        finish()

        android.os.Process.killProcess(android.os.Process.myPid())
    }


}