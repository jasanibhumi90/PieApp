package com.pie.model

import java.io.Serializable

data class BaseResponse<T>(
    var message: String,
    var token: String,
    var success: Int,
    var otpcode: Int,
    val data: T? = null
) : Serializable


data class LoginModel(
    var user_id: String,
    var first_name: String,
    var last_name: String,
    var user_name: String,
    var profile_pic: String,
    var gender: String,
    var things_ids: String,
    var profile_status: String,
    var country_code: String,
    var country_name: String,
    var phone_no: String,
    var wallet: String,
    var email_id: String,
    var password: String,
    var mobile_code: String,

    var is_verify: String,
    var device_type: String,
    var device_id: String,
    var is_blocked: String


) : Serializable



data class PostModel(
    var id: String="",
    var user_id: String="",
    var pie_user_id: String="",
    var parent_id: String,
    var post_type: String,
    var pies_text: String,
    var pies_media: String,
    var profile_pic: String,
    var first_name: String,
    var last_name: String,
    var creation_datetime: String,
    var likes: String,
    var comments: String,
    var shared: String,
    var pies_media_url: ArrayList<String>,
    var post_at: String



) : Serializable

