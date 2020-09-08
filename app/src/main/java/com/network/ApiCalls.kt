package com.network

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.MainApplication
import com.adityabugalia.basictodo.R

import com.events.TodoListEvent
import com.models.TodoModel
import com.utility.CommonUtil
import com.utility.RxBus
import com.google.gson.Gson
import java.io.File


object ApiCalls {

    private val TAG = "ApiCalls"

    private val gson: Gson

    private lateinit var rxBus: RxBus

    init {
        gson = Gson()
        rxBus = MainApplication.getRxBus()
    }

    lateinit var progressDialog: ProgressDialog

    fun showDialog() {
        if (progressDialog != null) {
            progressDialog.setCancelable(false)
            progressDialog.setMessage("Please wait..")
            progressDialog.show()
        }
    }

    fun hideDialog() {
        try {
            if (progressDialog != null) {
                progressDialog.hide()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun todoList(context: Context) {
        progressDialog = ProgressDialog(context)

        // Check internet connection
        if (!CommonUtil.isInternetAvailable(context)) {
            return
        }

        showDialog()

        var getURL = ApiData.TODO_URL
        val requestParam = null

        val jsonRequest =
            object : JsonArrayRequest(Method.GET, getURL, requestParam,
                Response.Listener { response ->
                    Log.i(TAG, "Todo Success: ${response}")

                    val responseModel = gson.fromJson<TodoModel>(
                        response.toString(),
                        TodoModel::class.java
                    )
                    rxBus.send(TodoListEvent(responseModel))

                    hideDialog()
                },
                Response.ErrorListener { error ->
                    Log.i(TAG, "reservationList Error: ${error.message}")

                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_error_message),
                        Toast.LENGTH_SHORT
                    ).show()

                    hideDialog()
                }
            ){
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    return HashMap<String, String>()
                }
            }

        MainApplication.getRequestQueue().add(jsonRequest)
    }
}