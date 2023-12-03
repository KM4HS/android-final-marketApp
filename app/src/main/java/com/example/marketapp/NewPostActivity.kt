package com.example.marketapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import java.text.SimpleDateFormat

class NewPostActivity : AppCompatActivity() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val postCollectionRef = db.collection("post")
    private val userCollectionRef = db.collection("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.newpost_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mode = intent.getIntExtra("mode", NEWMODE)

        if(mode== NEWMODE) {supportActionBar?.title="판매 글 작성"}
        else if(mode== EDITMODE){supportActionBar?.title="판매 글 수정"}

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { backPressedAlert() }
        }; onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val userID = Firebase.auth.currentUser
        var userEmail = userID?.email.toString()
        var userName = ""

        val stateSwitch = findViewById<SwitchCompat>(R.id.stateSwitch)
        val uploadButton = findViewById<Button>(R.id.uploadButton)

        var selling = SELLING

        stateSwitch.setOnCheckedChangeListener { p0, ischecked ->
            if (ischecked) { stateSwitch.text = SELLING; selling = SELLING }
            else { stateSwitch.text = NO_SELLING; selling = NO_SELLING } }

        if (mode == NEWMODE) { //새 글 작성 모드
            //판매 여부 결정 버튼은 수정 모드에서만 활성화
            stateSwitch.visibility = View.INVISIBLE
            uploadButton.text = "판매글 올리기"

            userCollectionRef.whereEqualTo("id", userEmail).get()
                .addOnSuccessListener { for (doc in it) { userName = doc["name"].toString() } }
            uploadButton.setOnClickListener { uploadPost(userName, userEmail, SELLING, NEWMODE) }

        } else if (mode == EDITMODE) { //수정 모드
            val editTitle = findViewById<EditText>(R.id.editTitle)
            val editPrice = findViewById<EditText>(R.id.editPrice)
            val editContent = findViewById<EditText>(R.id.editContent)

            uploadButton.text = "수정 적용하기"
            editTitle.setTextColor(Color.LTGRAY)
            editTitle.isFocusableInTouchMode=false
            editContent.setTextColor(Color.LTGRAY)
            editContent.isFocusableInTouchMode=false

            val editID = intent.getStringExtra("editID").toString()

            postCollectionRef.document(editID).get().addOnSuccessListener {
                if(it["state"].toString() == SELLING){selling = SELLING; stateSwitch.isChecked=true}
                else{selling = NO_SELLING; stateSwitch.isChecked=false}

                editTitle.setText(it["title"].toString())
                editPrice.setText(it["price"].toString())
                editContent.setText(it["content"].toString())

                userName = it["username"].toString()
                userEmail = it["user-email"].toString()
            }
            uploadButton.setOnClickListener { uploadPost(userName, userEmail, selling, EDITMODE) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                backPressedAlert()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun backPressedAlert() {
        val builder = AlertDialog.Builder(this@NewPostActivity).apply {
            setMessage("진행 상황은 저장되지 않습니다.\n게시글 작성을 취소할까요?")
            setPositiveButton("네") { dialog, n -> finish(); dialog.dismiss() }
            setNegativeButton("아니요") { dialog, n -> dialog.dismiss() }
        }.create(); builder.show()
    }

    private fun uploadPost(userName: String, userEmail: String, state: String, mode: Int) {
        val editTitle = findViewById<EditText>(R.id.editTitle).text.toString()
        val editPrice = findViewById<EditText>(R.id.editPrice).text.toString()
        val editContent = findViewById<EditText>(R.id.editContent).text.toString()
        val uid = Firebase.auth.currentUser?.uid.toString()

        if (editTitle == "" || editContent == "" || editPrice == "") {
            Toast.makeText(this@NewPostActivity, "입력되지 않은 항목이 있습니다.", Toast.LENGTH_SHORT).show()
        } else {
            val currentTime : Long = System.currentTimeMillis()

            if (mode == NEWMODE) { //추가: add
                val newPost = hashMapOf(
                    "title" to editTitle,
                    "price" to editPrice,
                    "content" to editContent,
                    "username" to userName,
                    "state" to state,
                    "user-email" to userEmail,
                    "UID" to uid,
                    "lastTime" to currentTime
                )
                postCollectionRef.add(newPost).addOnSuccessListener {}
            } else if (mode == EDITMODE) { //수정: update
                val editID = intent.getStringExtra("editID").toString()
                val editPost = hashMapOf(
                    "price" to editPrice,
                    "state" to state
                )
                postCollectionRef.document(editID).update(editPost as Map<String, Any>)
                    .addOnSuccessListener {}
            }
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
