package com.example.samplecollapseapp

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.samplecollapseapp.databinding.ViewHeaderBinding
import com.example.samplecollapseapp.databinding.ViewItemBinding
import kotlinx.android.synthetic.main.fragment_recycler_view.view.*


class RecyclerViewFragment : Fragment() {

    private lateinit var adapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_recycler_view, container, false)
v.recyclerView.setHasFixedSize(true)
        adapter = RecyclerViewAdapter()
        v.recyclerView.adapter = adapter
        v.recyclerView.layoutManager = GridLayoutManager(context, 1)
        v.recyclerView.addItemDecoration(StickyHeaderItemDecoration(adapter))

        adapter.items = createItems()
        adapter.notifyDataSetChanged()

        return v
    }

    private fun createItems(): List<Item> {
        val items: MutableList<Item> = ArrayList()
        for (i in 0..99) {
            val isHeader = Math.random() <= 0.25
            val item = if (isHeader) {
                Item(Item.Type.HEADER, "header$i")
            } else {
                Item(Item.Type.ITEM,"item$i")
            }
            items.add(item)
        }
        return items
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            RecyclerViewFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaderItemDecoration.StickyHeaderInterface {

    var items: List<Item> = mutableListOf()
    var headerTitle: String = ""

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = items[position].type.ordinal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == Item.Type.HEADER.ordinal) HeaderViewHolder.create(parent) else ItemViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is HeaderViewHolder -> holder.update(items[position])
            is ItemViewHolder -> holder.update(items[position])
        }
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var pos = itemPosition
        var headerPosition = -1
        do {
            if (isHeader(pos)) {
                headerPosition = pos
                break
            }
            pos -= 1
        } while (pos >= 0)
        return headerPosition
    }

    override fun getHeaderLayout(headerPosition: Int) = R.layout.view_sticky_header

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        if (items[headerPosition].type === Item.Type.HEADER) {
            val headerTextView =
                header?.findViewById<View>(R.id.header) as? TextView
            headerTextView?.text = items[headerPosition].text
            if (TextUtils.isEmpty(headerTitle) || !TextUtils.equals(
                    headerTitle,
                    items[headerPosition].text
                )
            ) {
                headerTitle = items[headerPosition].text
            }
        }
    }

    override fun isHeader(itemPosition: Int) = items[itemPosition].type === Item.Type.HEADER

    private interface ViewHolderInterface {
        fun update(item: Item?)
    }

    private class HeaderViewHolder(binding: ViewHeaderBinding) : RecyclerView.ViewHolder(binding.root), ViewHolderInterface {
        private val binding: ViewHeaderBinding = binding
        override fun update(item: Item?) {
            binding.setItem(item)
            binding.executePendingBindings()
        }

        companion object {
            fun create(parent: ViewGroup): HeaderViewHolder {
                val binding: ViewHeaderBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.view_header,
                    parent,
                    false
                )
                return HeaderViewHolder(binding)
            }
        }
    }

    private class ItemViewHolder(binding: ViewItemBinding) : RecyclerView.ViewHolder(binding.root), ViewHolderInterface {
        private val binding: ViewItemBinding = binding
        override fun update(item: Item?) {
            binding.setItem(item)
            binding.executePendingBindings()
        }

        companion object {
            fun create(parent: ViewGroup): ItemViewHolder {
                val binding: ViewItemBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.view_item,
                    parent,
                    false
                )
                return ItemViewHolder(binding)
            }
        }
    }

}

data class Item(var type: Type, var text: String) {
    enum class Type {
        HEADER, ITEM
    }
}