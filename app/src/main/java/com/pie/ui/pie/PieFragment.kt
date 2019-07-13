package com.pie.ui.pie

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.PostModel
import com.pie.ui.base.BaseFragment
import com.pie.ui.createpie.CreatePieActivity
import com.pie.utils.AppConstant.Companion.ARG_DATA
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import kotlinx.android.synthetic.main.fragment_pie.*
import org.jetbrains.anko.startActivityForResult
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

class PieFragment : BaseFragment(), View.OnClickListener {
    val REQUEST_CODE_CREATE_PIE = 100


    private lateinit var pieAdapter: PieAdapter
    val arPosts: ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pie, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivCreatePie.setOnClickListener(this)

        pieAdapter = PieAdapter(R.layout.listitem_home, this)
        rvPosts.run {
            layoutManager = LinearLayoutManager(activity!!)
            adapter = pieAdapter
        }


        getPies()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivCreatePie -> {
                val intent = Intent(activity!!,CreatePieActivity::class.java)
                startActivityForResult(intent,REQUEST_CODE_CREATE_PIE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CREATE_PIE -> {
                    AppLogger.e("tag","createpie++")
                    data?.let {
                        val postModel = it.getSerializableExtra(ARG_DATA) as PostModel?
                        postModel?.let {
                            pieAdapter.addAtFirst(it)
                        }
                    }
                }
            }

        }
    }

    private fun getPies() {
        if (AppGlobal.isNetworkConnected(activity!!)) run {


            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_offset)] = "0"

            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_get_pies)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.getPies(service), true)
                ?.subscribe({ onGetPie(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(activity!!, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onGetPie(
        resp: BaseResponse<ArrayList<PostModel>>
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return

        if (resp.data?.size != 0) {
            resp.data?.let { pieAdapter.addAll(it) }
        }
    }


}
