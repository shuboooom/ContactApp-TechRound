package com.shubham.contacts

import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

class ContactAdapter(val contacts: MutableList<Contact>, private val checkChange: (Contact,Boolean) -> Unit)  : RecyclerView.Adapter<ContactAdapter.MyViewHolder>() {

    inner class MyViewHolder(view:View):RecyclerView.ViewHolder(view)
    {
        val check = view.findViewById<CheckBox>(R.id.check)
        val image = view.findViewById<ImageView>(R.id.image)
        val name = view.findViewById<TextView>(R.id.name)
        val phone = view.findViewById<TextView>(R.id.phone)
        val container = view.findViewById<LinearLayout>(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemview = MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_contact,parent,false))
        itemview.check.setOnCheckedChangeListener { buttonView, isChecked ->
            checkChange.invoke(contacts[itemview.adapterPosition],isChecked)
        }
        itemview.container.setOnClickListener {
            val con = contacts[itemview.adapterPosition]
            itemview.check.isChecked = !con.selected
        }
        return itemview
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val con = contacts[holder.adapterPosition]
        holder.name.text = con.name
        holder.phone.text = con.phone
        holder.check.isChecked = con.selected

        when(con.photo)
        {
            null -> holder.image.setImageResource(R.drawable.ic_round_person_24)
            else -> {
                Glide.with(holder.itemView.context)
                    .load(con.photo)
                    .circleCrop()
                    .placeholder(R.drawable.ic_round_person_24)
                    .into(holder.image)
            }
        }

    }

    fun notifyChanges(newList : List<Contact>)
    {

        Log.d("Old List Size",""+contacts.size)
        Log.d("new List Size",""+newList.size)

        val diff = DiffUtil.calculateDiff(object  : DiffUtil.Callback(){
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return contacts[oldItemPosition].contactId == newList[newItemPosition].contactId
            }

            override fun getOldListSize(): Int {
                return contacts.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return contacts[oldItemPosition] == newList[newItemPosition]
            }

        })

        contacts.clear()
        contacts.addAll(newList)
        diff.dispatchUpdatesTo(this)
    }

    fun getSelectedCount(): Int
    {
        var count = 0;
        contacts.forEach {
            if(it.selected)
            {
                count++
            }
        }
        return count
    }
}