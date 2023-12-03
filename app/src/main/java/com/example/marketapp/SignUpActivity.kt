package com.example.marketapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore

class SignUpActivity: AppCompatActivity() {
    private val db: FirebaseFirestore = com.google.firebase.ktx.Firebase.firestore
    private val userCollectionRef = db.collection("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="회원가입"

        findViewById<Button>(R.id.signupDone)?.setOnClickListener{
            val userEmail = findViewById<EditText>(R.id.editNewId).text.toString()
            val password = findViewById<EditText>(R.id.editNewPassword).text.toString()
            val name = findViewById<EditText>(R.id.editNewName).text.toString()

            if(userEmail==""||name==""||password==""){
                Toast.makeText(this, "정보 입력이 누락되었습니다.", Toast.LENGTH_SHORT).show()
            }else if(password.length<6){
                Toast.makeText(this, "비밀번호를 6자 이상으로 설정해주세요.", Toast.LENGTH_SHORT).show()
            }else{
                doSignUp(userEmail, password, name)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed(){
        val builder = AlertDialog.Builder(this).apply {
            setMessage("회원가입을 취소할까요?")
            setPositiveButton("네") { dialog, n ->
                dialog.dismiss()
                finish()
            }
            setNegativeButton("아니요") { dialog, n -> dialog.dismiss() }
        }.create()
        builder.show()
    }

    private fun doSignUp(userEmail: String, password: String, name: String){
        Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this){
                if(it.isSuccessful) {
                    val birth = findViewById<DatePicker>(R.id.datePicker)
                    val uid = it.result.user?.uid.toString()
                    val birthMap = hashMapOf(
                        "year" to birth.year,
                        "month" to birth.month+1,
                        "day" to birth.dayOfMonth
                    )

                    val userMap = hashMapOf(
                        "id" to userEmail,
                        "name" to name,
                        "password" to password,
                        "birth" to birthMap
                    )

                    userCollectionRef.document(uid).set(userMap).addOnSuccessListener {
                        println("계정 데이터 추가 성공")
                        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
                            .addOnCompleteListener(this){
                                startActivity(
                                    Intent(this, MainActivity::class.java)
                                )
                                finish()
                            }
                    }
                }else{
                    Log.w("SignUpActivity", "signUpWithEmail", it.exception)
                    Toast.makeText(this, "회원가입에 실패했습니다.\n입력한 정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

