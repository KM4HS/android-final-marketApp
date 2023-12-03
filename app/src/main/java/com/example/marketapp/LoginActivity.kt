package com.example.marketapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    private var mBackWait:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signin_activity)
        supportActionBar?.title="로그인"

        findViewById<Button>(R.id.signInButton)?.setOnClickListener {
            val userEmail = findViewById<EditText>(R.id.editId)?.text.toString()
            val password = findViewById<EditText>(R.id.editPassword)?.text.toString()
            if(userEmail==""||password==""){
                Toast.makeText(this, "정보 입력이 누락되었습니다.",Toast.LENGTH_SHORT).show()
            }else{
                doLogin(userEmail, password)
            }
        }

        findViewById<Button>(R.id.signUpButton)?.setOnClickListener{
            startActivity(
                Intent(this, SignUpActivity::class.java)
            )
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("뒤로가기 클릭")
                if (System.currentTimeMillis() - mBackWait >= 2000) {
                    mBackWait = System.currentTimeMillis()
                    Toast.makeText(this@LoginActivity, "뒤로가기 버튼을 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
                } else {
                    finishAffinity()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun doLogin(userEmail: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                    )
                    finish()
                } else {
                    Toast.makeText(this, "아이디와 비밀번호를 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}