package com.example.marketapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore

data class MsgItem(val id: String, val sender: String, val senderEmail: String, val content:String, val post:String, val time:Long) {
    constructor(doc: QueryDocumentSnapshot) :
            this(doc.id, doc["sender"].toString(), doc["sender-email"].toString(),
                doc["content"].toString(), doc["post"].toString(), doc["time"].toString().toLong())
}

class MsgViewHolder(val view: View): RecyclerView.ViewHolder(view)

class MsgAdapter (private val context: Context, private var msgItems: List<MsgItem>)
    : RecyclerView.Adapter<MsgViewHolder>() {
    private val db: FirebaseFirestore = com.google.firebase.ktx.Firebase.firestore
    private val postCollectionRef = db.collection("post")

    fun interface OnItemLongClickListener {
        fun onItemLongClick(msg_id: String)
    }

    private var itemLongClickListener : OnItemLongClickListener? = null

    fun setItemLongClickListener(listener: OnItemLongClickListener){  itemLongClickListener = listener   }

    fun updateList(newList: List<MsgItem>) {
        msgItems = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.msg_item, parent, false)
        return MsgViewHolder(view)
    }

    override fun onBindViewHolder(holder: MsgViewHolder, position: Int) {
        val item = msgItems[position]

        holder.view.findViewById<TextView>(R.id.senderText).text = item.sender
        holder.view.findViewById<TextView>(R.id.senderEmailText).text = item.senderEmail
        holder.view.findViewById<TextView>(R.id.previewText).text = item.content

        postCollectionRef.document(item.post).get().addOnSuccessListener {
            holder.view.findViewById<TextView>(R.id.postText).text = it["title"].toString()
        }

        holder.view.setOnLongClickListener{
            val id = item.id
            itemLongClickListener?.onItemLongClick(id)
            true
        }
    }
    override fun getItemCount(): Int = msgItems.size
}