package com.example.marketapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MsgActivity : AppCompatActivity() {
    private var adapter: MsgAdapter? = null
    private val db: FirebaseFirestore = Firebase.firestore

    private var curUser = ""
    private var msgCollectionRef: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.msg_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "받은 메세지"

        curUser = intent.getStringExtra("curUser").toString() //현재 사용자의 uid
        msgCollectionRef = db.collection("user").document(curUser).collection("msg")

        val recyclerView = findViewById<RecyclerView>(R.id.msgRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MsgAdapter(this, emptyList())
        recyclerView.adapter = adapter

        updateMsgList()

        adapter?.setItemLongClickListener {
            val builder = AlertDialog.Builder(this).apply {
                setMessage("받은 메세지를 삭제할까요?")
                setPositiveButton("네") { dialog, n ->
                    msgCollectionRef!!.document(it).delete().addOnSuccessListener { updateMsgList() }
                    dialog.dismiss()
                }
                setNegativeButton("아니요") { dialog, n -> dialog.dismiss() }
            }.create(); builder.show()
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@MsgActivity, PostActivity::class.java))
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun updateMsgList() {
        msgCollectionRef!!.get().addOnSuccessListener {snapshot->
            val msgItems = mutableListOf<MsgItem>()
            for (doc in snapshot) {
                msgItems.add(MsgItem(doc))
                msgItems.sortByDescending { it.time }
            }
            adapter?.updateList(msgItems)
            if(adapter?.itemCount==0){ setContentView(R.layout.msg_null_layout) }
        }.addOnFailureListener { }
    }
}

