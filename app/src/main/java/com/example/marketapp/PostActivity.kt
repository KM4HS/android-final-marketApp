package com.example.marketapp

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import org.w3c.dom.Text

class PostActivity : AppCompatActivity() {
    var adapter: PostAdapter? = null
    private val db: FirebaseFirestore = com.google.firebase.ktx.Firebase.firestore
    private val postCollectionRef = db.collection("post")
    private var snapshotListener: ListenerRegistration? = null
    private var mBackWait: Long = 0
    private var filterState = false //default


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "판매 글 목록"

        val intent = Intent(this, DetailActivity::class.java)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostAdapter(this, emptyList())
        recyclerView.adapter = adapter

        val filterSwitch = findViewById<SwitchCompat>(R.id.filterSwitch)
        filterSwitch.setOnCheckedChangeListener{sv, ischecked->
            filterState = ischecked
            updateList(filterState)
        }

        updateList(filterState)

        adapter?.setItemClickListener { intent.putExtra("id", it); startActivity(intent) }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("뒤로가기 클릭")
                if (System.currentTimeMillis() - mBackWait >= 2000) {
                    mBackWait = System.currentTimeMillis()
                    Toast.makeText(this@PostActivity, "뒤로가기 버튼을 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
                } else { finishAffinity() } }
        }; onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onStart() {
        super.onStart()
        snapshotListener = postCollectionRef.addSnapshotListener { snapshot, error ->
            updateList(filterState)
        }
    }

    override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val newPostIntent = Intent(this, NewPostActivity::class.java)
        val msgIntent = Intent(this, MsgActivity::class.java)
        when (item.itemId) {
            R.id.newPostMenu -> {
                newPostIntent.putExtra("mode", NEWMODE)
                startActivity(newPostIntent)
            }
            R.id.receivedMsgMenu -> {
                msgIntent.putExtra("curUser", Firebase.auth.currentUser?.uid.toString())
                startActivity(msgIntent)
            }
            R.id.signOutMenu -> {
                com.google.firebase.ktx.Firebase.auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun updateList(filter:Boolean) {
        if(filter) {
            postCollectionRef.whereEqualTo("state", SELLING).get().addOnSuccessListener { snapshot ->
                val posts = mutableListOf<Item>()
                for (doc in snapshot) {
                    posts.add(Item(doc))
                    posts.sortByDescending { it.time }
                }
                adapter?.updateList(posts)
            }.addOnFailureListener { println("필터링 된 포스트 가져오지 못함") }
        }else{
            postCollectionRef.get().addOnSuccessListener {snapshot->
                val posts = mutableListOf<Item>()
                for (doc in snapshot) {
                    posts.add(Item(doc))
                    posts.sortByDescending { it.time }
                }
                adapter?.updateList(posts)
                if(adapter?.itemCount==0) {
                    setContentView(R.layout.msg_null_layout)
                    findViewById<ImageView>(R.id.nullImageView).setImageResource(R.drawable.non_selling_icon)
                    findViewById<TextView>(R.id.nullTextView).text = "등록된 게시글이 없습니다."
                }
            }.addOnFailureListener { println("포스트 가져오지 못함") }
        }
    }
}