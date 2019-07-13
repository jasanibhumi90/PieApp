package com.pie.ui.createpie

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.kbeanie.multipicker.api.CameraImagePicker
import com.kbeanie.multipicker.api.ImagePicker
import com.kbeanie.multipicker.api.Picker
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback
import com.kbeanie.multipicker.api.entity.ChosenImage
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.PostModel
import com.pie.ui.base.BaseActivity
import com.pie.utils.AppConstant.Companion.ARG_DATA
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import com.pie.utils.PermissionUtils
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_create_pie.*
import kotlinx.android.synthetic.main.activity_forgot_pass.ivBack
import kotlinx.android.synthetic.main.listitem_image.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set


class CreatePieActivity : BaseActivity(), View.OnClickListener, ImagePickerCallback,
    PermissionUtils.OnPermissionResponse {

    private var pickerPath: String = ""
    private var imagePicker: ImagePicker? = null
    private var cameraPicker: CameraImagePicker? = null
    var arFiles: ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pie)
        ivBack.setOnClickListener(this)
        ivCamera.setOnClickListener(this)
        ivGallery.setOnClickListener(this)
        tvPie.setOnClickListener(this)
        permissionUtils = PermissionUtils(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }
            R.id.tvPie -> {
                if(arFiles.size==0 && etPie.text.isEmpty()){
                    sneakerError(this,"Please write something or add image")
                }else{
                    if (arFiles.size > 0) {
                        uploadPic()
                    } else {
                        createPie("")

                    }
                }

            }
            R.id.ivRemove -> {
                val pView = v.parent as View
                arFiles.removeAt(llImages.indexOfChild(pView))
                llImages.removeView(pView)
                AppLogger.e("tag", "arFiles++" + arFiles.size)
            }
            R.id.ivCamera -> {
                if (!permissionUtils!!.checkPermission(PermissionUtils.REQUEST_CAMERA_GALLERY_PERMISSION)) {
                    requestPermission(PermissionUtils.REQUEST_CODE_CAMERA_PERMISSION)
                } else {
                    takePicture()
                }
            }
            R.id.ivGallery -> {
                if (!permissionUtils!!.checkPermission(PermissionUtils.REQUEST_CAMERA_GALLERY_PERMISSION)) {
                    requestPermission(PermissionUtils.REQUEST_CODE_GALLERY_PERMISSION)
                } else {
                    pickImageSingle()
                }
            }
        }

    }

    private fun requestPermission(code: Int) {
        permissionUtils?.requestPermissions(
            PermissionUtils.REQUEST_CAMERA_GALLERY_PERMISSION,
            code
        )
    }

    private fun showImageChooserDialog() {
        val items = arrayOf(
            resources.getString(R.string.takephoto),
            resources.getString(R.string.choosefromgallery),
            resources.getString(R.string.cancel)
        )

        val builder = AlertDialog.Builder(this)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == resources.getString(R.string.takephoto) -> takePicture()
                items[item] == resources.getString(R.string.choosefromgallery) -> pickImageSingle()
                items[item] == resources.getString(R.string.cancel) -> dialog.dismiss()
            }
        }
        builder.show()

    }


    private fun takePicture() {
        cameraPicker = CameraImagePicker(this)
        cameraPicker!!.setImagePickerCallback(this)
        pickerPath = cameraPicker!!.pickImage()

    }


    private fun pickImageSingle() {
        CropImage.activity()
            .start(this)

    }

    override fun onPermissionGranted(requestCode: Int) {
        when (requestCode) {
            PermissionUtils.REQUEST_CODE_GALLERY_PERMISSION -> {
                pickImageSingle()
            }

            PermissionUtils.REQUEST_CODE_CAMERA_PERMISSION -> {
                takePicture()
            }
        }
    }

    override fun onPermissionDenied(requestCode: Int) {

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onImagesChosen(p0: MutableList<ChosenImage>?) {

        if (p0!!.isNotEmpty()) {
            CropImage.activity(Uri.parse(p0[0].queryUri))
                .start(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        @Suppress("DEPRECATION")
                        result.uri.path?.let {

                            pickerPath = it
                            addView(pickerPath)

                        }
                    }
                }

                Picker.PICK_IMAGE_DEVICE -> {
                    if (imagePicker == null) {
                        imagePicker = ImagePicker(this)
                        imagePicker!!.setImagePickerCallback(this)
                    }
                    imagePicker!!.submit(data)
                }

                Picker.PICK_IMAGE_CAMERA -> {
                    try {
                        val path: String = pickerPath

                        if (cameraPicker == null) {
                            cameraPicker = CameraImagePicker(this)
                            cameraPicker!!.setImagePickerCallback(this)
                            cameraPicker!!.reinitialize(path)
                        }
                        cameraPicker!!.submit(data)
                    } catch (e: Exception) {
                    }

                }
            }
        }

    }

    override fun onError(p0: String?) {
        AppLogger.e("tag", "error" + p0)
    }

    fun addView(pickerPath: String) {

        val linearLayout = LayoutInflater.from(llImages.context)
            .inflate(R.layout.listitem_image, llImages, false) as RelativeLayout
        arFiles.add(pickerPath)
        linearLayout.ivRemove.setOnClickListener(this)
        Glide.with(this).load(pickerPath).into(linearLayout.ivImage)
        llImages.addView(linearLayout, 0)
    }


    private fun uploadPic() {
        if (AppGlobal.isNetworkConnected(this)) run {
            val builder = MultipartBody.Builder()
            builder.setType(MultipartBody.FORM)
            builder.addFormDataPart("token_id", pref.getToken());

            // Map is used to multipart the file using okhttp3.RequestBody
            // Multiple Images
            for (i in 0 until arFiles.size) {
                val file = File(arFiles.get(i))
                builder.addFormDataPart(
                    "pie_image[]",
                    file.getName(),
                    RequestBody.create(MediaType.parse("multipart/form-data"), file)
                )
            }

            val requestBody: MultipartBody = builder.build()
            callApi(requestInterface.uploadPiePic(requestBody), true)
                ?.subscribe({ onFileUpload(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun createPie(imagePaths:String) {
        if (AppGlobal.isNetworkConnected(this)) run {


            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            val data = HashMap<String, Any>()
            val auth = HashMap<String, Any>()
            data[getString(R.string.param_pies_text)] = etPie.text.toString()
            data[getString(R.string.param_pies_media)] = imagePaths

            auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
            auth[getString(R.string.param_token)] = pref.getToken()

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_post_pies)
            service[getString(R.string.request)] = request
            service[getString(R.string.auth)] = auth
            callApi(requestInterface.createPie(service), true)
                ?.subscribe({ onCreatePie(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onCreatePie(
        resp: BaseResponse<PostModel>
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return

        val data = Intent()
        data.putExtra(ARG_DATA, resp.data)
        setResult(RESULT_OK, data)
        finish()
    }


    private fun onFileUpload(
        resp: BaseResponse<String>
    ) {
        Log.e("tag", "resp" + resp)
        if (super.onStatusFalse(resp, true)) return
        val imagePaths=resp.data
        if (imagePaths != null) {
            createPie(imagePaths)
        }
    }

}
