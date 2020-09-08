package com.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.MainApplication
import com.adapters.TodoAdapter
import com.adityabugalia.basictodo.R
import com.enums.Filter
import com.events.TodoListEvent
import com.models.TodoModel
import com.network.ApiCalls
import com.utility.RxBus


import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar_main.*


class HomeActivity : AppCompatActivity(), View.OnClickListener {

    val TAG = HomeActivity::class.java.simpleName

    val compositeDisposable = CompositeDisposable()

    var todoList = TodoModel()

    private lateinit var rxBus: RxBus

    var todoAdapter: TodoAdapter? = null

    // Save current filter
    var currentFilter = Filter.ALL

    // Save last index of last page. It increase by 5 up to size of list
    var pageCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        init()
        initRxBusListener()
    }

    override fun onDestroy() {
        compositeDisposable.clear()

        super.onDestroy()
    }

    private fun init() {
        rxBus = MainApplication.getRxBus()

        recyclerList.layoutManager = LinearLayoutManager(this)

        // Init adapter
        todoAdapter = TodoAdapter(this)

        // Set adapter without data
        recyclerList.adapter = todoAdapter
        recyclerList.adapter?.notifyDataSetChanged()

        // Set click listener
        imgFilter.setOnClickListener(this)
        loadMore.setOnClickListener(this)

        // Searchview listener to filter search data
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                todoAdapter?.setFilter(currentFilter, searchView.query)
                return false
            }

        })

        // Call api to get to do data
        ApiCalls.todoList(this)
    }

    private fun initRxBusListener() {
        compositeDisposable.add(
            rxBus
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { receiverObject ->
                    if (receiverObject != null) {
                        if (receiverObject is TodoListEvent) {
                            // Receive data once it retrive from API
                            todoList = receiverObject.todoList

                            // Add item. It will add 5 items
                           addItem()
                        }
                    }
                })
        )
    }

    private fun addItem() {
        // Logic to add 5 items all the time when user click on load more item
        val totalTodoSize = todoList.size
        val nextCouter = pageCounter + 5

        // If pageCounter(last index of shown data) is less than total size of list, add 5 more items
        if (pageCounter < totalTodoSize) {

            // To prevent from ArrayIndexOutOfBound, check next 5 items are less than total size.
            // If yes add 5 items or add rest of the items
            if (nextCouter > totalTodoSize - 1) {
                val subList = TodoModel()

                // get 5 items from pageCounter to size of list (it can be less than 5)
                subList.addAll(todoList.subList(pageCounter, totalTodoSize - 1))
                todoAdapter?.setItems(subList)

                // No more to do items available, so disable button
                loadMore.visibility = View.GONE
            } else {
                val subList = TodoModel()

                // get 5 items from pageCounter to nextCouter
                subList.addAll(todoList.subList(pageCounter, nextCouter))
                todoAdapter?.setItems(subList)

                // Increase counter for next load more
                pageCounter += 5
            }
        }

        // Update list according to set filters
        todoAdapter?.setFilter(currentFilter, searchView.query)

        Log.i(TAG, "Total Size: " + todoList.size)
    }

    override fun onClick(v: View?) {
        if (v == imgFilter) {
            v?.let { showPopup(it) }
        } else if(v == loadMore) {
            addItem()
        }
    }

    private fun showPopup(v: View) {
        // Add Navigation menu for filter options
        PopupMenu(this, v).apply {
            setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    return when (item?.itemId) {
                        R.id.filterAll -> {
                            currentFilter = Filter.ALL
                            todoAdapter?.setFilter(currentFilter, searchView.query)
                            true
                        }
                        R.id.filterComplete -> {
                            currentFilter = Filter.COMPLETE
                            todoAdapter?.setFilter(currentFilter, searchView.query)
                            true
                        }
                        R.id.filterIncomplete -> {
                            currentFilter = Filter.INCOMPLETE
                            todoAdapter?.setFilter(currentFilter, searchView.query)
                            true
                        }
                        else -> false
                    }
                }

            })
            inflate(R.menu.menu_filters)
            show()
        }
    }
}