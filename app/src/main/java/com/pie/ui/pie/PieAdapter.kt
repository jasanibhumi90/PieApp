package com.pie.ui.pie

import android.view.View
import com.bumptech.glide.Glide
import com.pie.model.PostModel
import com.pie.ui.base.BaseAdapter
import kotlinx.android.synthetic.main.listitem_home.view.*


class PieAdapter(
    layout: Int,
    private val clickListener: View.OnClickListener
) : BaseAdapter<PostModel>(layout), BaseAdapter.OnBind<PostModel> {



    init {
        setOnBinding(this)
    }


    override fun onBind(view: View, position: Int, data: PostModel) {
        view.run {
            if (data.profile_pic.isNotEmpty()) {
                Glide.with(mContext).load(data.profile_pic).load(ivProfile)
            }
            tvUserName.text = (data.first_name + " " + data.last_name)
            tvTime.text = data.post_at
            tvPostDesc.text = data.pies_text
            llOne.visibility = View.GONE
            ivImage1.visibility = View.GONE
            ivImage2.visibility = View.GONE

            llTwo.visibility = View.GONE
            ivImage3.visibility = View.GONE
            ivImage4.visibility = View.GONE
            if (!data.pies_media_url.isNullOrEmpty() && data.pies_media_url.size > 0) {
                if (data.pies_media_url.size==1){
                    llOne.visibility = View.VISIBLE

                        ivImage1.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[0]).into(ivImage1)

                }else if (data.pies_media_url.size==2){
                    llOne.visibility = View.VISIBLE

                        ivImage1.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[0]).into(ivImage1)

                        ivImage2.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[1]).into(ivImage2)

                }
                else if (data.pies_media_url.size==3){
                    llOne.visibility = View.VISIBLE
                    llTwo.visibility = View.VISIBLE

                        ivImage1.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[0]).into(ivImage1)

                        ivImage2.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[1]).into(ivImage2)


                        ivImage3.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[2]).into(ivImage3)

                }
                else if (data.pies_media_url.size==4){
                    llOne.visibility = View.VISIBLE
                    llTwo.visibility = View.VISIBLE

                        ivImage1.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[0]).into(ivImage1)

                        ivImage2.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[1]).into(ivImage2)


                        ivImage3.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[2]).into(ivImage3)


                        ivImage4.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[3]).into(ivImage4)

                }

              /*  if (data.pies_media_url.size==1 || data.pies_media_url.size == 2) {
                    llOne.visibility = View.VISIBLE
                    if (data.pies_media_url.size == 1) {
                        ivImage1.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[0]).into(ivImage1)
                    }
                    if (data.pies_media_url.size == 2) {
                        ivImage2.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[1]).into(ivImage2)
                    }
                }
                if (data.pies_media_url.size==3 || data.pies_media_url.size == 4) {
                    llTwo.visibility = View.VISIBLE
                    if (data.pies_media_url.size == 3) {
                        ivImage3.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[2]).into(ivImage3)
                    }
                    if (data.pies_media_url.size == 4) {
                        ivImage4.visibility = View.VISIBLE
                        Glide.with(mContext).load(data.pies_media_url[3]).into(ivImage4)
                    }
                }*/

            } else {

            }

            /*
                  ivPostImage.visibility=View.VISIBLE
                  Glide.with(mContext).load(data.pies_media_url[0]).load(ivPostImage)
              }else{
                  ivPostImage.visibility=View.GONE
              }*/
            tvLikes.text = data.likes
            tvComments.text = data.comments
            tvShare.text = data.shared


            tvLikes.setOnClickListener(clickListener)
            tvLikes.tag=position

            ivMenu.setOnClickListener(clickListener)
            ivMenu.tag=position

            tvComments.setOnClickListener(clickListener)
            tvComments.tag=position
        }
    }

}