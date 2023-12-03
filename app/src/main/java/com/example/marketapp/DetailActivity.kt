package com.example.marketapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailActivity : AppCompatActivity() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val postCollectionRef = db.collection("post")
    private val userCollectionRef = db.collection("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "판매 글 보기"

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed(){
                startActivity(Intent(this@DetailActivity, PostActivity::class.java))
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val id = intent.getStringExtra("id").toString()
        val deleteBtn = findViewById<ImageButton>(R.id.deletePostButton)
        val editBtn = findViewById<ImageButton>(R.id.editPostButton)
        val sendMsgBtn = findViewById<ImageButton>(R.id.sendMsgButton)

        val curUserEmail = Firebase.auth.currentUser?.email.toString()
        var ownerEmail = ""
        var ownerID = ""

        postCollectionRef.document(id).get().addOnSuccessListener {
            //id = post 작성자의 uid
            //it = post 식별 id
            ownerEmail = it["user-email"].toString()
            ownerID = it["UID"].toString()
            if (ownerEmail == curUserEmail) {
                //작성자 id(email)와 접속한 id(email)가 동일하면 수정, 삭제 버튼 활성화, 메세지 전송 버튼 비활성화
                deleteBtn.visibility = View.VISIBLE
                editBtn.visibility = View.VISIBLE
                sendMsgBtn.visibility = View.INVISIBLE
            }
        }.addOnFailureListener { println("ownerID 얻어오기 실패") }

        posting(id) //포스트 내용 채우기

        deleteBtn.setOnClickListener {
            //게시글 삭제하기 전 확인 알림
            val builder = AlertDialog.Builder(this).apply {
                setMessage("게시글을 삭제할까요?")
                setPositiveButton("네") { dialog, n ->
                    deletePost(id)
                    dialog.dismiss()
                }
                setNegativeButton("아니요") { dialog, n -> dialog.dismiss() }
            }.create(); builder.show()
        }
        //포스트 수정
        editBtn.setOnClickListener { editPost(id) }
        sendMsgBtn.setOnClickListener {//메세지 전송
            sendMsg(curUserEmail, ownerEmail, ownerID, id)
        }
    }

    private fun posting(id: String) {
        val title = findViewById<TextView>(R.id.textTitle)
        val username = findViewById<TextView>(R.id.userTextView)
        val price = findViewById<TextView>(R.id.textPrice)
        val state = findViewById<TextView>(R.id.stateTextView)
        val content = findViewById<TextView>(R.id.textContent)

        //id = 포스트 식별 ID
        postCollectionRef.document(id).get().addOnSuccessListener {
            //it = 클릭한 포스트의 document
            title.text = it["title"].toString()
            username.text = "${it["username"].toString()}\n(${it["user-email"].toString()})"
            price.text = it["price"].toString()
            state.text = it["state"].toString()
            if(state.text == SELLING){state.setTextColor(Color.parseColor(MAIN_COLOR))}
            else if (state.text == NO_SELLING){state.setTextColor(Color.DKGRAY)}
            content.text = it["content"].toString()
        }
    }

    private fun deletePost(id: String) {
        postCollectionRef.document(id).delete().addOnSuccessListener { finish() }
    }

    private fun editPost(id: String) {
        val intent = Intent(this, NewPostActivity::class.java)
        intent.putExtra("mode", EDITMODE)
        intent.putExtra("editID", id)
        startActivity(intent)
    }

    //메세지 전송을 위한 다이얼로그
    private fun sendMsg(senderEmail: String, receiverEmail: String, receiverID: String , postID: String) {
        val msgCollectionRef = db.collection("user").document(receiverID).collection("msg")
        var senderName=""
        userCollectionRef.document(Firebase.auth.currentUser?.uid.toString()).get().addOnSuccessListener {
            senderName = it["name"].toString()
        }
        val curTime = System.currentTimeMillis()

        class MsgSendDialog : BottomSheetDialogFragment() {
            override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
            ): View? {
                super.onCreateView(inflater, container, savedInstanceState)
                isCancelable = false
                return inflater.inflate(R.layout.sendmsg_dialog, container, false)
            }

            override fun onActivityCreated(savedInstanceState: Bundle?) {
                super.onActivityCreated(savedInstanceState)
                val sendBtn = view?.findViewById<Button>(R.id.sendButton)
                val cancelBtn = view?.findViewById<Button>(R.id.cancelButton)
                val msgContent = view?.findViewById<EditText>(R.id.editMsgContent)?.text
                val senderText = view?.findViewById<TextView>(R.id.senderTextView)
                senderText?.text = receiverEmail

                sendBtn?.setOnClickListener {
                    if (msgContent.toString() == "") {
                        Toast.makeText(this@DetailActivity, "메세지 내용이 누락되었습니다.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val msg = hashMapOf(
                            "content" to msgContent.toString(),
                            "sender" to senderName,
                            "sender-email" to senderEmail,
                            "receiver" to receiverEmail,
                            "post" to postID, //어느 상품 글에서 보낸 메세지인지 확인용
                            "time" to curTime
                        )
                        msgCollectionRef.add(msg).addOnSuccessListener {}
                        dialog?.dismiss()
                    }
                }
                cancelBtn?.setOnClickListener{ dialog?.dismiss() }
            }
        }
        MsgSendDialog().show(supportFragmentManager, "MsgDialog")
    }
}

