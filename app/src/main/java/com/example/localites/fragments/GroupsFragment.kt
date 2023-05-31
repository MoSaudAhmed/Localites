package com.example.localites.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.localites.R
import com.example.localites.activities.CreateGroupActivity
import com.example.localites.activities.GroupDetailsActivity
import com.example.localites.adapters.GroupsAdapter
import com.example.localites.helpers.CircularProgressBar
import com.example.localites.helpers.Constants
import com.example.localites.helpers.showSnackbar
import com.example.localites.interfaces.GroupCallback
import com.example.localites.models.CreateGroupModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class GroupsFragment : Fragment(), View.OnClickListener, GroupCallback {

    lateinit var fab_create_group: FloatingActionButton
    lateinit var lay_fragmentGroups: RelativeLayout
    lateinit var rv_GroupsFragment: RecyclerView
    lateinit var groups_SwipeToRefresh: SwipeRefreshLayout
    lateinit var lay_resultNotFound: LinearLayout

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    lateinit var firestore: FirebaseFirestore

    lateinit var alert: AlertDialog.Builder
    lateinit var alertDialog: AlertDialog

    lateinit var groupsAdapter: GroupsAdapter
    var groupsList: MutableList<CreateGroupModel> = ArrayList()

    private var loadingMore = false
    private var isCustomSearch = false
    private var isCountrySearch = false


    var lastVisibleDocument: DocumentSnapshot? = null
    var isReachedEnd = false

    var countryName: String = ""
    var groupName: String = ""
    var mQuery: Query? = null

    lateinit var circularProgressBar: CircularProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_groups, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firestore = FirebaseFirestore.getInstance()

        initViews(view)
        setHasOptionsMenu(true)
        circularProgressBar = CircularProgressBar(activity)
        loadAllGroupsService()

        groupsAdapter = GroupsAdapter(groupsList, this, activity!!)

        var layoutManager = LinearLayoutManager(activity!!)
        rv_GroupsFragment.layoutManager = layoutManager
        rv_GroupsFragment.setHasFixedSize(true)
        rv_GroupsFragment.adapter = groupsAdapter

        groups_SwipeToRefresh.setOnRefreshListener {
            lastVisibleDocument = null
            groupsList.clear()

            if (isCountrySearch && !!countryName.isNullOrEmpty()) {
                if (this != null) {
                    lastVisibleDocument = null
                    loadCountryGroupsService(countryName)
                }
            } else if (isCustomSearch && !groupName.isNullOrEmpty()) {
                if (this != null) {
                    lastVisibleDocument = null
                    loadGroupNameGroupsService(groupName)
                }
            } else {
                if (this != null) {
                    lastVisibleDocument = null
                    loadAllGroupsService()
                }
            }
        }

        rv_GroupsFragment.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItem = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (dy <= 0) {
                    fab_create_group.animate().scaleX(1F).scaleY(1F).setDuration(300).start()
                } else {
                    fab_create_group.animate().scaleX(0F).scaleY(0F).setDuration(300).start()

                    if (!loadingMore && lastVisibleItem == totalItem - 1) {
                        loadingMore = true

                        if (isCountrySearch && !countryName.isNullOrEmpty()) {
                            if (this != null)
                                loadCountryGroupsService(countryName)
                        } else if (isCustomSearch && !groupName.isNullOrEmpty()) {
                            if (this != null)
                                loadGroupNameGroupsService(groupName)
                        } else {
                            if (this != null)
                                loadAllGroupsService()
                        }
                    }
                }
            }
        })
        return view
    }


    private fun initViews(view: View) {
        fab_create_group = view.findViewById(R.id.fab_create_group)
        lay_fragmentGroups = view.findViewById(R.id.lay_fragmentGroups)
        rv_GroupsFragment = view.findViewById(R.id.rv_GroupsFragment)
        groups_SwipeToRefresh = view.findViewById(R.id.groups_SwipeToRefresh)
        lay_resultNotFound = view.findViewById(R.id.lay_resultNotFound)

        fab_create_group.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_create_group -> {
                var intent = Intent(activity, CreateGroupActivity::class.java)
                activity?.startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.groups_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_groupLocation -> {
                showChooseLocationDialog("location")
                return true
            }
            R.id.menu_groupSearch -> {
                showChooseLocationDialog("searchGroup")
                return true
            }
            R.id.menu_groupReset -> {
                groupsList.clear()
                loadAllGroupsService()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showChooseLocationDialog(from: String) {

        alert = AlertDialog.Builder(activity!!)
        val layoutInflater = layoutInflater
        val dialogueview = layoutInflater.inflate(R.layout.dialog_groups_location_search, null)
        alertDialog = alert.create()
        alertDialog.setView(dialogueview)

        val btn_coverSelectionDialog_cancel =
            dialogueview.findViewById(R.id.btn_GroupLocationSearch_Cancel) as Button
        val btn_coverSelectionDialog_set =
            dialogueview.findViewById(R.id.btn_GroupLocationSearch_Set) as Button
        val et_GroupLocationSearch_Search =
            dialogueview.findViewById(R.id.et_GroupLocationSearch_Search) as EditText
        val tv_groupSearch_title =
            dialogueview.findViewById(R.id.tv_groupSearch_title) as TextView

        if (from.equals("searchGroup")) {
            tv_groupSearch_title.setText("Please enter group name you want to search")
            btn_coverSelectionDialog_set.setText("Search")
            et_GroupLocationSearch_Search.setHint("Group Name")
        }

        btn_coverSelectionDialog_set.setOnClickListener {

            lastVisibleDocument = null
            if (from.equals("searchGroup")) {
                if (et_GroupLocationSearch_Search.text.isNullOrEmpty()) {
                    Toast.makeText(activity, "Please enter group name to search", Toast.LENGTH_LONG)
                        .show()
                } else {
                    isCountrySearch = false
                    isCustomSearch = true
                    groupsList.clear()
                    lastVisibleDocument = null
                    groupName = et_GroupLocationSearch_Search.text.toString()
                    loadGroupNameGroupsService(et_GroupLocationSearch_Search.text.toString())
                    alertDialog.dismiss()
                }
            } else {
                if (et_GroupLocationSearch_Search.text.isNullOrEmpty()) {
                    Toast.makeText(
                        activity,
                        "Please enter Country name to search", Toast.LENGTH_LONG
                    ).show()
                } else {
                    groupsList.clear()
                    isCountrySearch = true
                    isCustomSearch = false
                    lastVisibleDocument = null
                    countryName = et_GroupLocationSearch_Search.text.toString()
                    loadCountryGroupsService(et_GroupLocationSearch_Search.text.toString())
                    alertDialog.dismiss()
                }
            }

        }
        btn_coverSelectionDialog_cancel.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }


    //Loading All Groups
    private fun loadAllGroupsService() {

        var circularProgressBar=CircularProgressBar(activity)
        circularProgressBar.showProgressDialog()

        isCountrySearch = false
        isCustomSearch = false
        var searchLimit = 15
        if (lastVisibleDocument == null) {
            mQuery = FirebaseFirestore.getInstance().collection(Constants.groups)
                .limit(searchLimit.toLong())
        } else {
            mQuery = FirebaseFirestore.getInstance().collection(Constants.groups)
                .startAfter(lastVisibleDocument!!).limit(searchLimit.toLong())
        }

        groups_SwipeToRefresh.isRefreshing = true

        mQuery!!.get().addOnSuccessListener { documents ->
            if (documents != null && documents.size() > 0 && !isReachedEnd) {
                loadingMore = false
                if (documents.size() > 0) {
                    lastVisibleDocument = documents.documents[documents.size() - 1]

                    for (document in documents) {
                        val groupModel: CreateGroupModel =
                            document.toObject(CreateGroupModel::class.java)
                        if (groupModel != null) {
                            groupsList.add(groupModel)
                        }
                    }
                } else {
                    isReachedEnd = true
                }

            } else {
                Toast.makeText(activity, "You Have Reached the end", Toast.LENGTH_LONG).show()
            }
            //add to adapter and notifyData
            if (groupsList.size <= 0) {
                lay_resultNotFound.visibility = View.VISIBLE
            } else {
                lay_resultNotFound.visibility = View.GONE
            }
            groups_SwipeToRefresh.isRefreshing = false
            groupsAdapter.notifyDataSetChanged()
            circularProgressBar.dismissProgressDialog()
        }.addOnFailureListener {
            showSnackbar(activity!!, lay_fragmentGroups, "There was an error in getting data")
            groups_SwipeToRefresh.isRefreshing = false
            if (groupsList.size <= 0) {
                lay_resultNotFound.visibility = View.VISIBLE
            } else {
                lay_resultNotFound.visibility = View.GONE
            }
            circularProgressBar.dismissProgressDialog()
        }
    }

    private fun loadGroupNameGroupsService(groupName: String) {

        var circularProgressBar=CircularProgressBar(activity)
        circularProgressBar.showProgressDialog()

        isCountrySearch = false
        isCustomSearch = true

        mQuery = FirebaseFirestore.getInstance().collection(Constants.groups)
            .whereEqualTo(Constants.groupName, groupName.toLowerCase())
            .limit(1)

        groups_SwipeToRefresh.isRefreshing = true
        mQuery!!.get().addOnSuccessListener { documents ->
            if (documents != null && documents.size() > 0 && !isReachedEnd) {
                loadingMore = false
                if (documents.size() > 0) {
                    for (document in documents) {
                        val groupModel: CreateGroupModel =
                            document.toObject(CreateGroupModel::class.java)
                        if (groupModel != null) {
                            groupsList.add(groupModel)
                        }
                    }
                }
            }
            //add to adapter and notifyData
            if (groupsList.size <= 0) {
                lay_resultNotFound.visibility = View.VISIBLE
            } else {
                lay_resultNotFound.visibility = View.GONE
            }
            groups_SwipeToRefresh.isRefreshing = false
            groupsAdapter.notifyDataSetChanged()
            circularProgressBar.dismissProgressDialog()

        }.addOnFailureListener {
            showSnackbar(activity!!, lay_fragmentGroups, "There was an error in getting data")
            groups_SwipeToRefresh.isRefreshing = false
            if (groupsList.size <= 0) {
                lay_resultNotFound.visibility = View.VISIBLE
            } else {
                lay_resultNotFound.visibility = View.GONE
            }
            circularProgressBar.dismissProgressDialog()
        }
    }


    //Loading Groups country Wise
    private fun loadCountryGroupsService(countryName: String) {
        var circularProgressBar=CircularProgressBar(activity)
        circularProgressBar.showProgressDialog()
        isCountrySearch = true
        isCustomSearch = false
        var searchLimit = 15
        if (lastVisibleDocument == null) {
            mQuery = FirebaseFirestore.getInstance().collection(Constants.groups)
                .whereEqualTo(
                    Constants.groupLocationCountry,
                    countryName.toLowerCase()
                )
                .limit(searchLimit.toLong())
        } else {
            mQuery = FirebaseFirestore.getInstance().collection(Constants.groups)
                .whereEqualTo(
                    Constants.groupLocationCountry, countryName.toLowerCase()
                )
                .startAfter(lastVisibleDocument!!).limit(searchLimit.toLong())
        }

        groups_SwipeToRefresh.isRefreshing = true
        mQuery!!.get().addOnSuccessListener { documents ->
            if (documents != null && documents.size() > 0 && !isReachedEnd) {
                loadingMore = false
                if (documents.size() > 0) {
                    lastVisibleDocument = documents.documents[documents.size() - 1]

                    for (document in documents) {
                        val groupModel: CreateGroupModel =
                            document.toObject(CreateGroupModel::class.java)
                        if (groupModel != null) {
                            groupsList.add(groupModel)
                        }
                    }
                } else {
                    isReachedEnd = true
                }

            } else {
                Toast.makeText(activity, "You Have Reached the end", Toast.LENGTH_LONG).show()
            }
            //add to adapter and notifyData
            if (groupsList.size <= 0) {
                lay_resultNotFound.visibility = View.VISIBLE
            } else {
                lay_resultNotFound.visibility = View.GONE
            }
            groups_SwipeToRefresh.isRefreshing = false
            groupsAdapter.notifyDataSetChanged()
            circularProgressBar.dismissProgressDialog()
        }.addOnFailureListener {
            showSnackbar(activity!!, lay_fragmentGroups, "There was an error in getting data")
            groups_SwipeToRefresh.isRefreshing = false
            if (groupsList.size <= 0) {
                lay_resultNotFound.visibility = View.VISIBLE
            } else {
                lay_resultNotFound.visibility = View.GONE
            }
            circularProgressBar.dismissProgressDialog()
        }

    }


    override fun onitemClicked(position: Int) {
        var intent = Intent(activity, GroupDetailsActivity::class.java)
        intent.putExtra(Constants.groups, groupsList.get(position))
        startActivity(intent)
    }
}
