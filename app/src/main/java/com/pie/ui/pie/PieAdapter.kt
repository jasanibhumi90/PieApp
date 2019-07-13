package com.pie.ui.pie

import android.view.View
import com.bumptech.glide.Glide
import com.pie.model.PostModel
import com.pie.ui.base.BaseAdapter
import kotlinx.android.synthetic.main.listitem_home.view.*
import com.pie.ui.main.MainActivity



class PieAdapter(
    layout: Int,
    private val clickListener: View.OnClickListener
) : BaseAdapter<PostModel>(layout), BaseAdapter.OnBind<PostModel> {



    init {
        setOnBinding(this)
    }


    override fun onBind(view: View, position: Int, data: PostModel) {
        view.run {
            if(data.profile_pic.isNotEmpty()) {
                Glide.with(mContext).load(data.profile_pic).load(ivProfile)
            }
            tvUserName.text=(data.first_name+" "+data.last_name)
            tvTime.text=data.post_at
            tvPostDesc.text=data.pies_text
            if(!data.pies_media_url.isNullOrEmpty() && data.pies_media_url.size>0) {
                rlImage.visibility=View.VISIBLE
                viewPagerImage.setAdapter(SlidingImage_Adapter(mContext, data.pies_media_url))
            }else{
                rlImage.visibility=View.GONE
            }
            indicator.setViewPager(viewPagerImage)
          /*
                ivPostImage.visibility=View.VISIBLE
                Glide.with(mContext).load(data.pies_media_url[0]).load(ivPostImage)
            }else{
                ivPostImage.visibility=View.GONE
            }*/
            tvLikes.text=data.likes
            tvComments.text=data.comments
            tvShare.text=data.shared
        }
    }
}