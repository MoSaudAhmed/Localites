package com.example.localites.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.localites.R
import com.example.localites.adapters.GroupDetailPostsAdapter
import com.example.localites.helpers.*
import com.example.localites.interfaces.GroupDetailsCallback
import com.example.localites.models.CreateGroupModel
import com.example.localites.models.CreateGroupPostModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

class GroupDetailsActivity : AppCompatActivity(), View.OnClickListener, GroupDetailsCallback {

    lateinit var createGroupModel: CreateGroupModel
    lateinit var chipGroup_GroupDetails: ChipGroup
    lateinit var tv_GroupDetails_title: TextView
    lateinit var img_groupDetails_GroupImage: ImageView
    lateinit var img_GroupDetails_more: ImageView
    lateinit var tv_DetailGroupLocation: TextView
    lateinit var card_GroupDetails_CreatePost: CardView
    lateinit var rv_groupDetails_recycler: RecyclerView
    lateinit var toolbar: Toolbar
    lateinit var tv_toolbar_title: TextView
    lateinit var tv_groupDetails_noCover: TextView
    lateinit var tv_DetailGroupfollowersCount: TextView
    lateinit var swipetoRefresh_GroupDetails: SwipeRefreshLayout
    lateinit var lay_GroupDetails: LinearLayout
    lateinit var lay_resultNotFound: LinearLayout

    lateinit var firestore: FirebaseFirestore
    lateinit var firebaseUser: FirebaseUser

    var hasThisGroup: Boolean = false
    private var loadingMore = false

    lateinit var postsAdapter: GroupDetailPostsAdapter
    var postsList: MutableList<CreateGroupPostModel> = ArrayList()

    lateinit var circularProgressBar: CircularProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        firestore = FirebaseFirestore.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        Preferences.loadPreferences(this)

        circularProgressBar = CircularProgressBar(this)

        createGroupModel = intent.getParcelableExtra(Constants.groups)

        initView()

        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        tv_toolbar_title.text = "Group Details"

        if (createGroupModel != null) {
            if (createGroupModel.groupCoverPic!!.isNotEmpty()) {
                Glide.with(this).load(createGroupModel.groupCoverPic)
                    .into(img_groupDetails_GroupImage)
                tv_groupDetails_noCover.visibility = View.GONE
            } else {
                img_groupDetails_GroupImage.visibility = View.GONE
                //tv_groupDetails_noCover.visibility = View.VISIBLE
            }
            if (createGroupModel.groupName!!.isNotEmpty()) {
                tv_GroupDetails_title.text =
                    TextChanges().capitalize(createGroupModel.groupName.toString())
            } else {
                tv_GroupDetails_title.text = "N / A"
            }
            tv_DetailGroupLocation.text =
                "${TextChanges().capitalize(createGroupModel.groupLocationCity.toString())}, ${TextChanges().capitalize(
                    createGroupModel.groupLocationCountry.toString()
                )}"

            if (createGroupModel.groupTypeList != null && createGroupModel.groupTypeList!!.isNotEmpty()) {
                for (text in createGroupModel.groupTypeList!!) {
                    addItemToGroup(text)
                }
            }
            tv_DetailGroupfollowersCount.text =
                "${createGroupModel.followers.toString() + " followers"}"
        }

        checkIfThisGroupIsInMyGroups()
        loadAllGroupPosts()

        var layoutManager = LinearLayoutManager(this@GroupDetailsActivity)
        postsAdapter = GroupDetailPostsAdapter(postsList, this, this@GroupDetailsActivity)
        rv_groupDetails_recycler.layoutManager = layoutManager
        rv_groupDetails_recycler.setHasFixedSize(true)
        rv_groupDetails_recycler.adapter = postsAdapter


        rv_groupDetails_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItem = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (dy > 0) {

                    if (!loadingMore && lastVisibleItem == totalItem - 1) {
                        loadingMore = true
                        loadAllGroupPosts()
                    }
                }
            }
        })

        swipetoRefresh_GroupDetails.setOnRefreshListener {
            lastVisibleDocument = null
            postsList.clear()

            lastVisibleDocument = null
            loadAllGroupPosts()
        }


    }


    private fun checkIfThisGroupIsInMyGroups() {
        var circularProgressBar = CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        if (!createGroupModel.groupId.isNullOrEmpty()) {
            var docRef = firestore.collection(Constants.users).document(firebaseUser.uid)
                .collection(Constants.groups).document(createGroupModel.groupId.toString())
            docRef.get()
                .addOnSuccessListener {
                    circularProgressBar.dismissProgressDialog()
                    if (it.exists()) {
                        hasThisGroup = true
                    } else {
                        hasThisGroup = false
                    }
                }.addOnFailureListener {
                    hasThisGroup = false
                    circularProgressBar.dismissProgressDialog()
                }
        } else {
            circularProgressBar.dismissProgressDialog()
        }
    }

    private fun initView() {
        img_groupDetails_GroupImage = findViewById(R.id.img_groupDetails_GroupImage)
        tv_GroupDetails_title = findViewById(R.id.tv_GroupDetails_title)
        chipGroup_GroupDetails = findViewById(R.id.chipGroup_GroupDetails)
        tv_DetailGroupLocation = findViewById(R.id.tv_DetailGroupLocation)
        img_GroupDetails_more = findViewById(R.id.img_GroupDetails_more)
        card_GroupDetails_CreatePost = findViewById(R.id.card_GroupDetails_CreatePost)
        rv_groupDetails_recycler = findViewById(R.id.rv_groupDetails_recycler)
        toolbar = findViewById(R.id.toolbar)
        tv_toolbar_title = findViewById(R.id.tv_toolbar_title)
        tv_groupDetails_noCover = findViewById(R.id.tv_groupDetails_noCover)
        tv_DetailGroupfollowersCount = findViewById(R.id.tv_DetailGroupfollowersCount)
        swipetoRefresh_GroupDetails = findViewById(R.id.swipetoRefresh_GroupDetails)
        lay_GroupDetails = findViewById(R.id.lay_GroupDetails)
        lay_resultNotFound = findViewById(R.id.lay_resultNotFound)

        img_GroupDetails_more.setOnClickListener(this)
        card_GroupDetails_CreatePost.setOnClickListener(this)

    }

    private fun addItemToGroup(text: String) {
        var chip = Chip(this)
        chip.setChipBackgroundColorResource(R.color.backgroundYellow)
        chip.setCloseIconVisible(false)
        chip.setTextColor(Color.BLACK)
        chip.text = TextChanges().capitalize(text)
        chipGroup_GroupDetails.addView(chip)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_GroupDetails_more -> {
                showMorePopUp()
                return
            }
            R.id.card_GroupDetails_CreatePost -> {
                createPostClicked()
                return
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun createPostClicked() {

        if (createGroupModel.onlyOwnerCanPost) {
            Toast.makeText(
                this@GroupDetailsActivity,
                "Only Admin can post",
                Toast.LENGTH_LONG
            ).show()
        } else {
            var intent = Intent(this, CreateGroupPost::class.java)
            intent.putExtra(Constants.groupId, createGroupModel.groupId)
            startActivityForResult(intent, Constants.groupPostResult)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.groupPostResult -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    var createGroupPostModel: CreateGroupPostModel =
                        data!!.getParcelableExtra("post")
                    postsList.add(createGroupPostModel)
                    postsAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    var menu_groupDetails_more_contact_edit: MenuItem? = null
    var menu_groupDetails_delete_unfollow: MenuItem? = null

    private fun showMorePopUp() {
        var popupMenu = PopupMenu(this, img_GroupDetails_more)
        var menu = popupMenu.menu
        popupMenu.menuInflater.inflate(R.menu.menu_group_details_more, menu)
        menu_groupDetails_more_contact_edit =
            menu.findItem(R.id.menu_groupDetails_more_contact_edit)
        menu_groupDetails_delete_unfollow =
            menu.findItem(R.id.menu_groupDetails_delete_unfollow)
        if (createGroupModel.createdBy.toString().equals(firebaseUser.uid)) {
            menu_groupDetails_more_contact_edit!!.setTitle(Constants.editGroup)
        }

        if (createGroupModel.createdBy.toString().equals(firebaseUser.uid)) {
            menu_groupDetails_delete_unfollow!!.setTitle(Constants.deleteGroup)
        } else {
            Log.e("hasThisGroupSetting", hasThisGroup.toString())
            if (hasThisGroup) {
                menu_groupDetails_delete_unfollow!!.setTitle(Constants.unfollow)
            } else {
                menu_groupDetails_delete_unfollow!!.setTitle(Constants.follow)
            }
        }

        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.menu_groupDetails_more_contact_edit -> {
                        if (createGroupModel.createdBy.toString()
                                .equals(firebaseUser.uid)
                        ) {
                            var intent =
                                Intent(this@GroupDetailsActivity, CreateGroupActivity::class.java)
                            intent.putExtra("from", "updateGroup")
                            intent.putExtra("data", createGroupModel)
                            startActivityForResult(intent, Constants.updateGroupResult)
                        } else {
                            gotoOwnerProfile()
                        }
                        return true
                    }
                    R.id.menu_groupDetails_delete_unfollow -> {
                        if (createGroupModel.createdBy.toString()
                                .equals(firebaseUser.uid)
                        ) {
                            var alertDialog: AlertDialog.Builder =
                                AlertDialog.Builder(this@GroupDetailsActivity)
                            alertDialog.setTitle("Delete Group")
                            alertDialog.setCancelable(false)
                            alertDialog.setMessage("Are you sure, You want to delete ${createGroupModel.groupName} Group?")
                            alertDialog.setNeutralButton("Delete",
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        deleteGroupCalled()
                                        return

                                    }
                                })
                            alertDialog.setPositiveButton("Cancel",
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        dialog!!.cancel()
                                        return
                                    }

                                })
                            alertDialog.show()

                        } else {
                            if (menu_groupDetails_delete_unfollow!!.title.toString()
                                    .equals(Constants.follow)
                            ) {
                                followCalled()
                                Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "follow Clicked",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {

                                unfollowCalled()
                                Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "unfollow Clicked",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                return true
            }

        })
        popupMenu.show()
    }

    private fun gotoOwnerProfile() {
        var intent = Intent(this, ProfileDetails::class.java)
        intent.putExtra(Constants.uid, createGroupModel.createdBy)
        Log.e("GroupDetailsActivity", createGroupModel.createdBy + "_uid")
        startActivity(intent)
    }

    private fun deleteGroupCalled() {
        var circularProgressBar = CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        //Delete Group reference from my
        var myGroupRef = firestore.collection(Constants.users).document(firebaseUser.uid)
            .collection(Constants.groups).document(createGroupModel.groupId.toString())
        myGroupRef.get().addOnSuccessListener {
            if (it.exists()) {
                myGroupRef.delete()
            }
            circularProgressBar.dismissProgressDialog()
        }.addOnFailureListener {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                .show()
            circularProgressBar.dismissProgressDialog()
        }
        //Deleting Group Data
        var groupDeleteRef =
            firestore.collection(Constants.groups).document(createGroupModel.groupId.toString())

        groupDeleteRef.get().addOnSuccessListener {
            if (it.exists()) {
                var docRef = it.toObject(CreateGroupModel::class.java)
                if (docRef!!.createdBy.toString().equals(firebaseUser.uid)) {
                    groupDeleteRef.delete()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun followCalled() {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        var map = HashMap<String, String>()
        map.put(createGroupModel.groupId.toString(), "")
        firestore.collection(Constants.users).document(firebaseUser.uid)
            .collection(Constants.groups).document(createGroupModel.groupId.toString()).set(
                map,
                SetOptions.merge()
            ).addOnSuccessListener {
                hasThisGroup = true
                circularProgressBar.dismissProgressDialog()
            }.addOnFailureListener {
                hasThisGroup = false
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                circularProgressBar.dismissProgressDialog()
            }
        createGroupModel.followers = createGroupModel.followers!!.inc()
        tv_DetailGroupfollowersCount.setText(
            "${createGroupModel.followers.toString() + " followers"}"
        )

        var groupFollowIncrementRef =
            firestore.collection(Constants.groups).document(createGroupModel.groupId.toString())
        groupFollowIncrementRef.update(Constants.followers, FieldValue.increment(1))
    }

    private fun unfollowCalled() {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        var myGroupRef = firestore.collection(Constants.users).document(firebaseUser.uid)
            .collection(Constants.groups).document(createGroupModel.groupId.toString())
        myGroupRef.get().addOnSuccessListener {
            if (it.exists()) {
                myGroupRef.delete()
                var groupFollowIncrementRef =
                    firestore.collection(Constants.groups)
                        .document(createGroupModel.groupId.toString())
                groupFollowIncrementRef.update(Constants.followers, FieldValue.increment(-1))
            }
            createGroupModel!!.followers = createGroupModel.followers!!.dec()
            tv_DetailGroupfollowersCount.text = "${(createGroupModel!!.followers)} followers"

            hasThisGroup = false

            circularProgressBar.dismissProgressDialog()
        }.addOnFailureListener {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                .show()
            circularProgressBar.dismissProgressDialog()
        }

    }

    var lastVisibleDocument: DocumentSnapshot? = null
    var isReachedEnd = false

    var mQuery: Query? = null


    fun loadAllGroupPosts() {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        var searchLimit = 15

        if (lastVisibleDocument == null) {
            mQuery = FirebaseFirestore.getInstance().collection(Constants.allPosts)
                .whereEqualTo(Constants.groupId, createGroupModel.groupId)
                .limit(searchLimit.toLong())
        } else {
            mQuery = FirebaseFirestore.getInstance().collection(Constants.allPosts)
                .whereEqualTo(Constants.groupId, createGroupModel.groupId)
                .startAfter(lastVisibleDocument!!).limit(searchLimit.toLong())
        }

        swipetoRefresh_GroupDetails.isRefreshing = true

        mQuery!!.get().addOnSuccessListener { documents ->
            if (documents != null && documents.size() > 0 && !isReachedEnd) {
                loadingMore = false
                if (documents.size() > 0) {
                    lastVisibleDocument = documents.documents[documents.size() - 1]

                    for (document in documents) {
                        val postsModel: CreateGroupPostModel =
                            document.toObject(CreateGroupPostModel::class.java)
                        if (postsModel != null) {
                            postsList.add(postsModel)
                        }
                    }
                } else {
                    isReachedEnd = true
                }

            } else {
                Toast.makeText(
                    this@GroupDetailsActivity,
                    "You Have Reached the end",
                    Toast.LENGTH_LONG
                ).show()
            }
            //add to adapter and notifyData
            if (postsList.size <= 0) {
                lay_resultNotFound.visibility = View.VISIBLE
            } else {
                lay_resultNotFound.visibility = View.GONE
            }
            circularProgressBar.dismissProgressDialog()
            swipetoRefresh_GroupDetails.isRefreshing = false
            postsAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            showSnackbar(
                this@GroupDetailsActivity,
                lay_GroupDetails,
                "There was an error in getting data"
            )
            circularProgressBar.dismissProgressDialog()
            swipetoRefresh_GroupDetails.isRefreshing = false
            if (postsList.size <= 0) {
                lay_resultNotFound.visibility = View.VISIBLE
            } else {
                lay_resultNotFound.visibility = View.GONE
            }
        }

    }

    override fun onReportClicked(position: Int) {
        var circularProgressBar=CircularProgressBar(this)
        circularProgressBar.showProgressDialog()

        var map = HashMap<String, Int>()
        map.put(Constants.report, 1)
        var reportIncrementRef =
            firestore.collection(Constants.report).document(postsList.get(position).postId!!)

        reportIncrementRef.update(Constants.report, FieldValue.increment(1))
            .addOnSuccessListener {
                Toast.makeText(
                    this@GroupDetailsActivity,
                    "Thanks for reporting to us, Team will take appropriate action after verifying the post",
                    Toast.LENGTH_LONG
                ).show()
                circularProgressBar.dismissProgressDialog()
            }.addOnFailureListener {
                reportIncrementRef.set(map)
                circularProgressBar.dismissProgressDialog()
            }
    }

    override fun onPostClicked(position: Int) {
        var intent = Intent(this, PostDetailsActivity::class.java)
        intent.putExtra("Post", postsList.get(position))
        startActivity(intent)
    }
}
