package com.example.grocerylistapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import org.w3c.dom.Text

// AboutUsActivity.kt
class AboutUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        var abtUs: TextView = findViewById<TextView>(R.id.aboutUsText)
        abtUs.text = "Welcome to ShopEasy, the ultimate solution for managing your grocery lists with ease and convenience. ShopEasy is a user-friendly Android application designed to simplify your shopping experience and make grocery management a breeze.\n" +
                "\n" +
                "Our Mission:At ShopEasy, our mission is to transform the way you shop for groceries. We understand the challenges of juggling busy schedules, forgetting items, and feeling overwhelmed in crowded grocery stores. Our app is here to streamline your shopping journey, saving you time, money, and effort.\n" +
                "\n" +
                "Join the ShopEasy Community:Follow us on social media to stay updated with the latest features, tips, and offers. Join the ever-growing ShopEasy community and embark on a more enjoyable shopping experience.\n" +
                "\n" +
                "Thank you for choosing ShopEasy as your trusted grocery list management app. Happy shopping!"
    }


}
