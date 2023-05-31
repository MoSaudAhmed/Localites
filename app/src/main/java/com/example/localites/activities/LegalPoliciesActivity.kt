package com.example.localites.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.localites.R

class LegalPoliciesActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var tv_toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal_policies)

        initViews()

        setSupportActionBar(toolbar)
        tv_toolbarTitle.setText("Legal policies")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tv_toolbarTitle = findViewById(R.id.tv_toolbar_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}
