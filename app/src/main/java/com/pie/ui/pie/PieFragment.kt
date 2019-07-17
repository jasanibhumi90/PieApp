package com.pie.ui.pie

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.tcking.giraffecompressor.GiraffeCompressor
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.PostModel
import com.pie.model.ReportModel
import com.pie.ui.base.BaseFragment
import com.pie.ui.createpie.CreatePieActivity
import com.pie.ui.editpie.EditPieActivity
import com.pie.utils.AppConstant.Companion.ARG_DATA
import com.pie.utils.AppConstant.Companion.ARG_PIE_DATA
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import kotlinx.android.synthetic.main.dialog_reportpost.*
import kotlinx.android.synthetic.main.flow_report.view.*
import kotlinx.android.synthetic.main.fragment_pie.*
import org.apmem.tools.layouts.FlowLayout
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set
import org.jetbrains.anko.startActivity



class PieFragment : BaseFragment(), View.OnClickListener {
    val REQUEST_CODE_CREATE_PIE = 100
    val REQUEST_CODE_EDIT_PIE = 101
    private lateinit var reportPostdialog: Dialog
    val arReasons: ArrayList<ReportModel> = ArrayList()
    private lateinit var pieAdapter: PieAdapter
    private var reasonId = 0
    private var reasonName = ""
    val arPosts: ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pie, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GiraffeCompressor.init(context)

        ivCreatePie.setOnClickListener(this)

        pieAdapter = PieAdapter(R.layout.listitem_home, this)
        rvPosts.run {
            layoutManager = LinearLayoutManager(activity!!)
            adapter = pieAdapter
        }


        getPies()
    }

    private fun isValid(): Boolean {
        if (reasonName.isEmpty()) {
            AppGlobal.alertDialog(mActivity, resources.getString(R.string.error_reason))
            return false
        } else if (reasonName.toLowerCase() == "other".toLowerCase() && reportPostdialog.edDescription.text.toString().isEmpty()) {
            AppGlobal.alertDialog(mActivity, resources.getString(R.string.error_desc))
            return false
        }
        return true
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivCreatePie -> {
                val intent = Intent(activity!!, CreatePieActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_CREATE_PIE)
            }

            R.id.ivMenu -> {
                val pos = p0.tag as Int
                openMenu(p0,pieAdapter.getItem(pos).user_id)

            }

            R.id.tvComments -> {

            }
            R.id.ivReportDilogClose -> {
                reportPostdialog.dismiss()
            }


            R.id.tvReportPostSend -> {
                if (isValid()) {

                    val pos: Int = p0.tag as Int
                    reportPost(
                        pos,
                        pieAdapter.getItem(pos).id,
                        reasonId,
                        reportPostdialog.edDescription.text.toString()
                    )
                }
            }
        }
    }
    private fun openMenu(v: View, userId: String) {
        val popup = PopupMenu(this.activity!!, v)

        popup.inflate(R.menu.option_menu)
        if (pref.getLoginData()?.user_id == userId) {
            popup.menu.findItem(R.id.menu1).isVisible=true
            popup.menu.findItem(R.id.menu1).title = resources.getString(R.string.menu_editpost)
            popup.menu.findItem(R.id.menu2).title = resources.getString(R.string.menu_deletepost)
        } else {
            popup.menu.findItem(R.id.menu1).isVisible=false
            popup.menu.findItem(R.id.menu2).title = resources.getString(R.string.menu_reportpost)
        }
        popup.setOnMenuItemClickListener { item ->
           val position = v.tag as Int

            when (item.itemId) {
                R.id.menu1 -> {
                    val intent = Intent(activity!!, EditPieActivity::class.java)
                    intent.putExtra(ARG_PIE_DATA , pieAdapter.getItem(position))
                    startActivityForResult(intent, REQUEST_CODE_EDIT_PIE)

                }  R.id.menu2 -> {
                    if (pref.getLoginData()?.user_id == userId) {
                         deletePie(position)
                    } else {
                        getReports(position)
                    }
                }
            }
            false
        }
        popup.show()
    }

    private fun reportPost(position: Int, postId: String, reason: Int, comment: String) {
        val request = HashMap<String, Any>()
        val service = HashMap<String, Any>()
        val data = HashMap<String, Any>()
        val auth = HashMap<String, Any>()
        data[getString(R.string.param_pie_id)] = postId
        data[getString(R.string.param_why_reporting)] = reason
        data[getString(R.string.param_reporting_text)] = comment

        auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
        auth[getString(R.string.param_token)] = pref.getToken()

        request[getString(R.string.data)] = data
        service[getString(R.string.service)] = getString(R.string.service_post_pies_report)
        service[getString(R.string.request)] = request
        service[getString(R.string.auth)] = auth
        callApi(requestInterface.postReport(service), true)
            ?.subscribe({ onGetPostReport(it, position) }) { onResponseFailure(it, true) }
            ?.let { mCompositeDisposable.add(it) }

    }

    private fun onGetPostReport(
        resp: BaseResponse<Any>,
        position: Int
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return
        reportPostdialog.dismiss()
        sneakerSuccess(activity!!, resp.message)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CREATE_PIE -> {
                    AppLogger.e("tag", "createpie++")
                    data?.let {
                        val postModel = it.getSerializableExtra(ARG_DATA) as PostModel?
                        postModel?.let {
                            pieAdapter.addAtFirst(it)
                        }
                    }
                }
                REQUEST_CODE_EDIT_PIE -> {
                    AppLogger.e("tag", "createpie++")
                    data?.let {
                        val postModel = it.getSerializableExtra(ARG_DATA) as PostModel?
                        val pos=pieAdapter.getAll().indexOfFirst { it.id==postModel?.id }
                        if(pos!=-1){
                            postModel?.let { it1 -> pieAdapter.updateItem(pos, it1) }
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


    private fun deletePie(position: Int) {
        if (AppGlobal.isNetworkConnected(activity!!)) run {


            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_pie_id)] = pieAdapter.getItem(position).id

            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_post_pies_delete)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.getPies(service), true)
                ?.subscribe({ onDeletePie(it, position) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(activity!!, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onDeletePie(
        resp: BaseResponse<ArrayList<PostModel>>,
        position: Int
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return
sneakerSuccess(activity!!,resp.message)
        pieAdapter.removeItem(position)
    }


    private fun getReports(position: Int) {
        if (AppGlobal.isNetworkConnected(activity!!)) run {


            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()


            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_get_report)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.getReportss(service), true)
                ?.subscribe({ onGetReports(it, position) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(activity!!, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onGetReports(
        resp: BaseResponse<ArrayList<ReportModel>>,
        position: Int
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return

        if (resp.data?.size != 0) {
            resp.data?.let { arReasons.addAll(it) }
            reportPostDialog(position)
        }
    }

    private fun reportPostDialog(position: Int) {
        try {
            reportPostdialog = Dialog(activity)
            reportPostdialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
            reportPostdialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            reportPostdialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            reportPostdialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            reportPostdialog.setContentView(R.layout.dialog_reportpost)
            reportPostdialog.ivReportDilogClose.setOnClickListener(this)
            reportPostdialog.setCanceledOnTouchOutside(false)
            reportPostdialog.window?.attributes?.windowAnimations = R.style.DialogStyle

            reportPostdialog.tvReportPostSend.setOnClickListener(this)

            reportPostdialog.tvReportPostSend.tag = position

            addView(reportPostdialog.flowLayout)

            val lp = WindowManager.LayoutParams()
            val window = reportPostdialog.window
            lp.copyFrom(window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = lp
            // commentsDialog.getWindow().setCallback(windowCallback);
            reportPostdialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addView(flowLayout: FlowLayout) {
        if (flowLayout.childCount != 0) {
            flowLayout.removeAllViews()
        }
        var prevPos = 0
        var pos = 0

        for (i in 0 until arReasons.size) {
            val data = arReasons[i]
            val v = LayoutInflater.from(flowLayout.context)
                .inflate(R.layout.flow_report, flowLayout, false) as LinearLayout

            val newDrawable: Drawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.bg_btn_radius_twenty))
            DrawableCompat.setTint(newDrawable, resources.getColor(R.color.colorInvitesBg))
            v.llFlowLayout?.background = newDrawable

            v.tvReasonName.text = data.report_name
            v.llFlowLayout.setOnClickListener {
                pos = i
                reasonId = data.id.toInt()
                reasonName = data.report_name
                flowLayout.getChildAt(prevPos).background = newDrawable
                flowLayout.getChildAt(pos).background = resources.getDrawable(R.drawable.bg_btn_blue_radius_twenty)
                prevPos = pos
            }
            flowLayout.addView(v, flowLayout.childCount)
        }
    }


}
