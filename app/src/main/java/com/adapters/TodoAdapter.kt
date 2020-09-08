package com.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.adityabugalia.basictodo.R
import com.models.TodoModel


import java.util.*


class TodoAdapter(
    private val context: Context
) :
    RecyclerView.Adapter<TodoAdapter.ViewHolder>(), Filterable {

    private var items = TodoModel()
    private var filterItems = TodoModel()
    private var currentFilter = com.enums.Filter.ALL

    fun setItems(items: TodoModel) {
        this.items.addAll(items)
    }

    override fun getItemCount(): Int {
        return filterItems.size
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(context).inflate(R.layout.view_todo_row, viewGroup, false)
        )
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int
    ) {
        val vh = viewHolder as ItemViewHolder

        vh.bind(filterItems.get(position))
    }

    open inner class ViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!)

    inner class ItemViewHolder(itemView: View) :
        ViewHolder(itemView) {

        private val txtTitle: TextView
        private val imgTaskComplete: ImageView
        private val imgArrow: ImageView

        fun bind(todoItem: TodoModel.TodoModelItem) {

            // Get value of title
            val title = "" + todoItem.title

            // Check if size is more than 20, add ellipsize effect
            if (title.length > 20) {
                txtTitle.text = "" + title.substring(0, 17) + "..."

                imgArrow.visibility = View.VISIBLE
            } else {
                txtTitle.text = "" + todoItem.title

                imgArrow.visibility = View.INVISIBLE
            }

            // When user click on text, show full text and rotate arrow image.
            // When user click on it once again, return to ellipsize effect
            txtTitle.setOnClickListener { v: View? ->
                if (txtTitle.text.length == 20 && title.length > 20) {
                    txtTitle.text = title

                    imgArrow.animate().rotation(180f).start()

                } else if (txtTitle.text.length > 20 && title.length > 20) {
                    txtTitle.text = "" + title.substring(0, 17) + "..."

                    imgArrow.animate().rotation(0f).start()
                }
            }

            // Show if task is completed or no by showing right arrow
            if (todoItem.completed != null && todoItem.completed) {
                imgTaskComplete.visibility = View.VISIBLE
            } else {
                imgTaskComplete.visibility = View.INVISIBLE
            }
        }

        init {
            txtTitle = itemView.findViewById(R.id.txtTitle)
            imgTaskComplete = itemView.findViewById(R.id.imgTaskComplete)
            imgArrow = itemView.findViewById(R.id.imgArrow)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    // If there is no text into SearchView, it will show all data
                    filterItems = items
                } else {
                    val resultList = TodoModel()

                    // Filter according to search text.
                    for (row in items) {
                        if (row.title?.toLowerCase(Locale.ROOT)?.contains(charSearch.toLowerCase(Locale.ROOT))!!) {
                            resultList.add(row)
                        }
                    }

                    filterItems = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterItems
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                var updatedTodoModel = results?.values as TodoModel

                val filterResults = TodoModel()

                // Find data for completed task, incompleted task or all tasks
                for (todo in updatedTodoModel) {

                    when(currentFilter) {
                        com.enums.Filter.ALL -> {
                            filterResults.add(todo)
                        }

                        com.enums.Filter.COMPLETE -> {
                            if (todo.completed == true) {
                                filterResults.add(todo)
                            }
                        }

                        com.enums.Filter.INCOMPLETE -> {
                            if (todo.completed == false) {
                                filterResults.add(todo)
                            }
                        }
                    }
                }

                // Finally apply filter to adapter
                filterItems = filterResults

                notifyDataSetChanged()
            }
        }
    }

    fun setFilter(currentFilter: com.enums.Filter, query: CharSequence?) {
        // Get current filter
        this.currentFilter = currentFilter
        filter?.filter(query)
    }
}