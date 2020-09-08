package com.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.adityabugalia.basictodo.R

class MainActivity : AppCompatActivity() {

    private var mDelayHandler: Handler = Handler()

    internal val mRunnable: Runnable = Runnable {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show dashboard after 3 seconds
        mDelayHandler.postDelayed(mRunnable, 3000)
    }
}