package com.example.localites.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.localites.R
import com.example.localites.adapters.PostDiscussionAdapter
import com.example.localites.helpers.*
import com.example.localites.models.CreateGroupPostModel
import com.example.localites.models.PersonDetailsModel
import com.example.localites.models.PostDiscussionModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostDetailsActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var rv_discussionPostDetails: RecyclerView
    lateinit var img_postDetails_profilePic: ImageView
    lateinit var tv_postDetails_name: TextView
    lateinit var tv_postDetails_date: TextView
    lateinit var tv_postDetails_message: TextView
    lateinit var img_postDetails_picture: ImageView
    lateinit var et_postDetailsSend: EditText
    lateinit var imgButton_postDetailsSend: ImageButton
    lateinit var lay_activityPostDetails: LinearLayout
    lateinit var swipetoRefresh_PostDescussion: SwipeRefreshLayout
    lateinit var lay_resultNotFound: LinearLayout
    lateinit var toolbar: Toolbar
    lateinit var tv_toolbar_title: TextView
    lateinit var tv_noDataText: TextView

    lateinit var firebaseUser: FirebaseUser
    lateinit var firebaseFirestore: FirebaseFirestore

    lateinit var createGroupPostModel: CreateGroupPostModel
    lateinit var personDetails: PersonDetailsModel

    var lastVisibleDocument: DocumentSnapshot? = null
    var isReachedEnd = false
    var mQuery: Query? = null
    private var loadingMore = false

    lateinit var circularProgressBar: CircularProgressBar

    var discussionList: MutableList<PostDiscussionModel> = ArrayList()
    lateinit var discussionAdapter: PostDiscussionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        initView()
        circularProgressBar = CircularProgressBar(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        tv_toolbar_title.setText("Post Detail")

        tv_noDataText.text = "no replies yet, reply below to Start discussion"

        createGroupPostModel = intent.getParcelableExtra("Post")

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firebaseFirestore = FirebaseFirestore.getInstance()

        //setting RecyclerView adapter
        discussionAdapter = PostDiscussionAdapter(discussionList, this@PostDetailsActivity)
        rv_discussionPostDetails.layoutManager = LinearLayoutManager(this)
        rv_discussionPostDetails.setHasFixedSize(true)
        rv_discussionPostDetails.adapter = discussionAdapter

        //calling user details of post owner
        getPostOwnerProfile(createGroupPostModel.createdBy)
        //calling replies discussion
        getPostDiscussion(createGroupPostModel.postId)

        swipetoRefresh_PostDescussion.setOnRefreshListener {
            lastVisibleDocument = null
            discussionList.clear()
            getPostDiscussion(createGroupPostModel.createdBy)
        }

        tv_postDetails_date.text =
            TextChanges().convertDateToText(createGroupPostModel.createdDate.toString())

        tv_postDetails_message.text = createGroupPostModel.message
        if (!TextUtils.isEmpty(createGroupPostModel.groupsCover.toString())) {
            Glide.with(this).load(createGroupPostModel.groupsCover).into(img_postDetails_picture)
            img_postDetails_picture.visibility = View.VISIBLE
        } else {
            img_postDetails_picture.visibility = View.GONE
        }

    }


    private fun getPostDiscussion(postId: String?) {

        var circularProgressBar = CircularProgressBar(this)
        circularProgressBar.showProgressDialog()

        firebaseFirestore.collection(Constants.allPosts)
            .document(postId.toString()).collection(Constants.discussion).get()
            .addOnSuccessListener {
                if (lastVisibleDocument == null) {
                    mQuery = FirebaseFirestore.getInstance().collection(Constants.allPosts)
                        .document(createGroupPostModel.postId.toString())
                        .collection(Constants.discussion)
                        .limit(15)
                } else {
                    mQuery = FirebaseFirestore.getInstance().collection(Constants.allPosts)
                        .document(postId.toString())
                        .collection(Constants.discussion)
                        .startAfter(lastVisibleDocument!!).limit(15)
                }

                mQuery!!.get().addOnSuccessListener { documents ->
                    if (documents != null && documents.size() > 0 && !isReachedEnd) {
                        loadingMore = false
                        if (documents.size() > 0) {
                            lastVisibleDocument = documents.documents[documents.size() - 1]

                            for (document in documents) {
                                val discussionModel: PostDiscussionModel =
                                    document.toObject(PostDiscussionModel::class.java)
                                if (discussionModel != null) {
                                    discussionList.add(discussionModel)
                                }
                            }
                        } else {
                            isReachedEnd = true
                        }

                    } else {
                        Toast.makeText(
                            this@PostDetailsActivity,
                            "You Have Reached the end",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                    //add to adapter and notifyData
                    if (discussionList.size <= 0) {
                        lay_resultNotFound.visibility = View.VISIBLE
                    } else {
                        lay_resultNotFound.visibility = View.GONE
                    }
                    swipetoRefresh_PostDescussion.isRefreshing = false
                    discussionAdapter.notifyDataSetChanged()
                }.addOnFailureListener {
                    showSnackbar(
                        this@PostDetailsActivity,
                        lay_activityPostDetails,
                        "There was an error in getting data"
                    )
                    swipetoRefresh_PostDescussion.isRefreshing = false
                    if (discussionList.size <= 0) {
                        lay_resultNotFound.visibility = View.VISIBLE
                    } else {
                        lay_resultNotFound.visibility = View.GONE
                    }
                }
                circularProgressBar.dismissProgressDialog()
            }.addOnFailureListener {
                lay_resultNotFound.visibility = View.VISIBLE
                circularProgressBar.dismissProgressDialog()
            }
    }

    private fun getPostOwnerProfile(createdBy: String?) {
        var circularProgressBar = CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        firebaseFirestore.collection(Constants.users).document(createdBy!!)
            .get().addOnSuccessListener { document ->
                if (document != null) {
                    personDetails =
                        document.toObject(PersonDetailsModel::class.java)!!
                    Glide.with(this@PostDetailsActivity).load(personDetails.profilePic)
                        .error(R.drawable.ic_person_outerline_yellow)
                        .into(img_postDetails_profilePic)
                    if (!TextUtils.isEmpty(personDetails.displayName)) {
                        tv_postDetails_name.text = personDetails.displayName
                    } else {
                        tv_postDetails_name.text = "Anonymous"
                    }
                }
                circularProgressBar.dismissProgressDialog()
            }.addOnFailureListener {
                circularProgressBar.dismissProgressDialog()
            }
    }

    private fun initView() {
        rv_discussionPostDetails = findViewById(R.id.rv_discussionPostDetails)
        img_postDetails_profilePic = findViewById(R.id.img_postDetails_profilePic)
        tv_postDetails_name = findViewById(R.id.tv_postDetails_name)
        tv_postDetails_date = findViewById(R.id.tv_postDetails_date)
        tv_postDetails_message = findViewById(R.id.tv_postDetails_message)
        img_postDetails_picture = findViewById(R.id.img_postDetails_picture)
        et_postDetailsSend = findViewById(R.id.et_postDetailsSend)
        imgButton_postDetailsSend = findViewById(R.id.imgButton_postDetailsSend)
        lay_activityPostDetails = findViewById(R.id.lay_activityPostDetails)
        swipetoRefresh_PostDescussion = findViewById(R.id.swipetoRefresh_PostDescussion)
        lay_resultNotFound = findViewById(R.id.lay_resultNotFound)
        toolbar = findViewById(R.id.toolbar)
        tv_toolbar_title = findViewById(R.id.tv_toolbar_title)
        tv_noDataText = findViewById(R.id.tv_noDataText)

        imgButton_postDetailsSend.setOnClickListener(this)
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

    override fun onClick(v: View?) {
        var circularProgressBar = CircularProgressBar(this)
        circularProgressBar.showProgressDialog()
        if (!TextUtils.isEmpty(et_postDetailsSend.text)) {
            var refId = firebaseFirestore.collection(Constants.allPosts)
                .document(createGroupPostModel.postId.toString()).collection(Constants.discussion)
                .document().id
            var postDiscussionModel = PostDiscussionModel()
            postDiscussionModel.uid = Preferences.uid
            postDiscussionModel.displayName = Preferences.displayName
            postDiscussionModel.profilePic = Preferences.profilePic
            postDiscussionModel.reply = et_postDetailsSend.text.toString()

            val date = TextChanges().convertTimeStampToDate()
            postDiscussionModel.date = date

            firebaseFirestore.collection(Constants.allPosts)
                .document(createGroupPostModel.postId.toString())
                .collection(Constants.discussion)
                .document(refId).set(postDiscussionModel).addOnSuccessListener {
                    Toast.makeText(
                        this@PostDetailsActivity,
                        "Replied to the post",
                        Toast.LENGTH_LONG
                    ).show()
                    lay_resultNotFound.visibility = View.GONE
                    et_postDetailsSend.text.clear()
                    discussionList.add(postDiscussionModel)
                    discussionAdapter.notifyDataSetChanged()
                    circularProgressBar.dismissProgressDialog()
                }.addOnFailureListener {
                    showSnackbar(
                        this@PostDetailsActivity,
                        lay_activityPostDetails,
                        "Something went wrong. Please try again later"
                    )
                    circularProgressBar.dismissProgressDialog()
                }
        } else {
            showSnackbar(
                this@PostDetailsActivity,
                lay_activityPostDetails,
                "can't post empty reply, Please enter something"
            )
            circularProgressBar.dismissProgressDialog()
        }
    }
}
