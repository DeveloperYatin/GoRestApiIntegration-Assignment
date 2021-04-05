package com.developeryatin.peopledatafetchingdemo

import android.app.SearchManager
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.developeryatin.peopledatafetchingdemo.model.PeopleDataModel
import com.developeryatin.peopledatafetchingdemo.model.PeoplesData
import com.developeryatin.peopledatafetchingdemo.adapter.Adapter
import com.developeryatin.peopledatafetchingdemo.interfaces.ApiClient.apiClient
import com.developeryatin.peopledatafetchingdemo.interfaces.ApiInterface
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), OnRefreshListener {


    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var arrayList = ArrayList<PeoplesData>()
    private var adapter: Adapter? = null
    private var topText: TextView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var errorLayout: RelativeLayout? = null
    private var errorImage: ImageView? = null
    private var errorTitle: TextView? = null
    private var errorMessage: TextView? = null
    private var btnRetry: Button? = null
    //private var pageNumber = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout!!.setOnRefreshListener(this)
        swipeRefreshLayout!!.setColorSchemeResources(R.color.colorAccent)
        topText = findViewById(R.id.topText)
        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.isNestedScrollingEnabled = false

        onLoadingSwipeRefresh("")
        errorLayout = findViewById(R.id.errorLayout)
        errorImage = findViewById(R.id.errorImage)
        errorTitle = findViewById(R.id.errorTitle)
        errorMessage = findViewById(R.id.errorMessage)
        btnRetry = findViewById(R.id.btnRetry)

    }

    private fun loadJson(keyword: String) {
        errorLayout!!.visibility = View.GONE
        swipeRefreshLayout!!.isRefreshing = true
        val apiInterface = apiClient!!.create(ApiInterface::class.java)

        val call: Call<PeopleDataModel?>? = if (keyword.isNotEmpty()) {
            apiInterface.getNewPage(keyword.toInt())
        } else {
            apiInterface.getData()
        }

        call!!.enqueue(object : Callback<PeopleDataModel?> {
            override fun onResponse(
                call: Call<PeopleDataModel?>,
                response: Response<PeopleDataModel?>
            ) =
                if (response.isSuccessful && response.body()!!.data != null) {
                    if (arrayList.isNotEmpty()) {
                        arrayList.clear()
                    }
                    val jsonArray = JSONArray(response.body()!!.data.toString())
                    if (!jsonArray.isNull(0)) {
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = JSONObject(jsonArray[i].toString())
                            val id = jsonObject.getString("id")
                            val name = jsonObject.getString("name")
                            val email = jsonObject.getString("email")
                            val status = jsonObject.getString("status")
                            val createdAt = jsonObject.getString("created_at")
                            arrayList.add(PeoplesData(id, name, email, status, createdAt))
                        }
                    }
                    //articles = response.body().data
                    adapter = Adapter(arrayList, this@MainActivity)
                    recyclerView!!.adapter = adapter
                    adapter!!.notifyDataSetChanged()
                    topText!!.visibility = View.VISIBLE
                    swipeRefreshLayout!!.isRefreshing = false

                    /*recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            val lastCompletelyVisibleItemPosition: Int =
                                (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()
                            try {
                                if(arrayList.isNotEmpty()){
                                    if (lastCompletelyVisibleItemPosition == arrayList.size - 1) {
                                        pageNumber += 1
                                        loadJson(pageNumber.toString())
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    })*/

                } else {
                    topText!!.visibility = View.INVISIBLE
                    swipeRefreshLayout!!.isRefreshing = false
                    val errorCode: String = when (response.code()) {
                        404 -> "404 not found"
                        500 -> "500 server broken"
                        else -> "unknown error"
                    }
                    showErrorMessage(
                        R.drawable.no_result,
                        "No Result",
                        """
                                    Please Try Again!
                                    $errorCode
                                    """.trimIndent()
                    )
                }

            override fun onFailure(call: Call<PeopleDataModel?>, t: Throwable) {
                topText!!.visibility = View.INVISIBLE
                swipeRefreshLayout!!.isRefreshing = false
                showErrorMessage(
                    R.drawable.oops,
                    "Oops..",
                    """
                            Network failure, Please Try Again
                            $t
                            """.trimIndent()
                )
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = "Search User using user id..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.length > 2) {
                    onLoadingSwipeRefresh(query)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Type more than two letters!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchMenuItem.icon.setVisible(false, false)
        return true
    }

    override fun onRefresh() {
        loadJson("")
    }

    private fun onLoadingSwipeRefresh(keyword: String) {
        swipeRefreshLayout!!.post { loadJson(keyword) }
    }

    private fun showErrorMessage(imageView: Int, title: String, message: String) {
        if (errorLayout!!.visibility == View.GONE) {
            errorLayout!!.visibility = View.VISIBLE
        }
        errorImage!!.setImageResource(imageView)
        errorTitle!!.text = title
        errorMessage!!.text = message
        btnRetry!!.setOnClickListener { onLoadingSwipeRefresh("") }
    }
}