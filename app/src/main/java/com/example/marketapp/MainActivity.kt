package com.example.marketapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

const val NEWMODE: Int = 0
const val EDITMODE: Int = 1

const val SELLING: String = "판매중"
const val NO_SELLING: String = "판매 중지"

const val MAIN_COLOR:String = "#83A13A"
const val SUB_COLOR:String = "#8083A13A"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Firebase.auth.currentUser == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
        }else{
            startActivity(
                Intent(this, PostActivity::class.java)
            )
        }
    }
}