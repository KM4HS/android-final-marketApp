package com.example.marketapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class Item(val id: String, val title: String, val state:String, val price: Int, val userName: String, val time:Long) {
    constructor(doc: QueryDocumentSnapshot) :
            this(doc.id, doc["title"].toString(), doc["state"].toString(), doc["price"].toString().toIntOrNull() ?: 0,
                doc["username"].toString(), doc["lastTime"].toString().toLong())
}


class PostViewHolder(val view: View):RecyclerView.ViewHolder(view)

class PostAdapter(private val context: Context, private var items: List<Item>) :
    RecyclerView.Adapter<PostViewHolder>() {

    fun interface onItemClickListener {
        fun onItemClick(email: String)
    }

    private var itemClickListener : onItemClickListener? = null

    fun setItemClickListener(listener: onItemClickListener){  itemClickListener = listener   }

    fun updateList(newList: List<Item>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = items[position]

        val stateText = holder.view.findViewById<TextView>(R.id.textState)
        val stateIcon = holder.view.findViewById<ImageView>(R.id.imageView)
        stateText.text = item.state
        if(item.state== SELLING){
            stateText.setTextColor(Color.parseColor(MAIN_COLOR))
            stateIcon.setImageResource(R.drawable.selling_icon)
            stateIcon.setColorFilter(Color.parseColor(MAIN_COLOR))
        }
        else{
            stateText.setTextColor(Color.GRAY)
            stateIcon.setImageResource(R.drawable.non_selling_icon)
            stateIcon.setColorFilter(Color.DKGRAY)
        }

        val timeDataFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        timeDataFormat.timeZone = TimeZone.getTimeZone("GMT+09:00")

        holder.view.findViewById<TextView>(R.id.textTitle).text = item.title
        holder.view.findViewById<TextView>(R.id.textTime).text = timeDataFormat.format(item.time)
        holder.view.findViewById<TextView>(R.id.textPrice).text = "${item.price.toString()}원"

        holder.view.setOnClickListener{
            val id = item.id
            itemClickListener?.onItemClick(id)
        }
    }
}