package com.example.localites.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.localites.R
import com.example.localites.activities.CreateMarketAdd
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class MarketFramgnet : Fragment(), View.OnClickListener {

    lateinit var market_card_search: CardView
    lateinit var img_market_card_search_close: ImageView
    lateinit var img_market_card_search_search: ImageView
    lateinit var et_market_searchEditText: EditText
    lateinit var cg_market_ChipGroup: ChipGroup
    lateinit var fab_create_market: FloatingActionButton

    lateinit var firebaseUser: FirebaseUser
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore

    lateinit var swiprRefresh_market: SwipeRefreshLayout
    lateinit var rv_MarketHome: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_market_framgnet, container, false)

        initViews(view)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firebaseFirestore = FirebaseFirestore.getInstance()

        setHasOptionsMenu(true)

        return view
    }

    private fun initViews(view: View) {
        market_card_search = view.findViewById(R.id.market_card_search)
        img_market_card_search_close = view.findViewById(R.id.img_market_card_search_close)
        img_market_card_search_search = view.findViewById(R.id.img_market_card_search_search)
        et_market_searchEditText = view.findViewById(R.id.et_market_searchEditText)
        cg_market_ChipGroup = view.findViewById(R.id.cg_market_ChipGroup)
        fab_create_market = view.findViewById(R.id.fab_create_market)

        swiprRefresh_market = view.findViewById(R.id.swiprRefresh_market)
        rv_MarketHome = view.findViewById(R.id.rv_MarketHome)

        img_market_card_search_close.setOnClickListener(this)
        img_market_card_search_search.setOnClickListener(this)
        fab_create_market.setOnClickListener(this)


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_material_search, menu)
        val searchItem = menu.findItem(R.id.menu_Search)
        searchItem.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                if (market_card_search.visibility == View.VISIBLE) {
                    et_market_searchEditText.text.clear()
                    market_card_search.visibility = View.GONE
                } else {
                    market_card_search.visibility = View.VISIBLE
                }
                return true
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_market_card_search_close -> {
                et_market_searchEditText.text.clear()
                market_card_search.visibility = View.GONE
                return
            }
            R.id.img_market_card_search_search -> {
                if (et_market_searchEditText.text.trim().length > 0) {
                    addItemToGroup(et_market_searchEditText.text.toString())
                    market_card_search.visibility = View.GONE
                    et_market_searchEditText.text.clear()
                } else {
                    Toast.makeText(activity, "Please enter something to search", Toast.LENGTH_LONG)
                        .show()
                }
                return
            }
            R.id.fab_create_market -> {
                activity!!.startActivity(Intent(activity, CreateMarketAdd::class.java))
                return
            }
        }
    }

    private fun addItemToGroup(text: String) {
        cg_market_ChipGroup.removeAllViews()
        var chip = Chip(activity)
        chip.setChipBackgroundColorResource(R.color.backgroundYellow)
        chip.setCloseIconVisible(true)
        chip.setCloseIconResource(R.drawable.ic_close)
        chip.setTextColor(Color.BLACK)
//        chip.setCloseIconTintResource(android.R.color.black)
        chip.text = text
        cg_market_ChipGroup.addView(chip)
        chip.setOnCloseIconClickListener {
            cg_market_ChipGroup.removeAllViews()
            et_market_searchEditText.text.clear()

        }
    }

    public fun backPressed() {

    }

}
