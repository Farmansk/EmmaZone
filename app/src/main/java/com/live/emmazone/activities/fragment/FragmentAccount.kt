package com.live.emmazone.activities.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.switchmaterial.SwitchMaterial
import com.live.emmazone.R
import com.live.emmazone.activities.FAQ
import com.live.emmazone.activities.PrivacyPolicy
import com.live.emmazone.activities.TermsCondition
import com.live.emmazone.activities.auth.ChangePassword
import com.live.emmazone.activities.auth.ProfileActivity
import com.live.emmazone.activities.auth.UserLoginChoice
import com.live.emmazone.activities.main.Cart
import com.live.emmazone.activities.main.Notifications
import com.live.emmazone.extensionfuncton.Validator
import com.live.emmazone.extensionfuncton.getPreference
import com.live.emmazone.net.RestObservable
import com.live.emmazone.net.Status
import com.live.emmazone.response_model.LoginResponse
import com.live.emmazone.response_model.NotificationStatusResponse
import com.live.emmazone.response_model.ProfileResponse
import com.live.emmazone.utils.AppConstants
import com.live.emmazone.utils.AppUtils
import com.live.emmazone.view_models.AppViewModel
import com.makeramen.roundedimageview.RoundedImageView
import com.schunts.extensionfuncton.loadImage
import com.schunts.extensionfuncton.toast

class FragmentAccount : Fragment(), Observer<RestObservable> {

    private val appViewModel: AppViewModel by viewModels()

    var isNotification = true

    var type = ""
    var profileImage:RoundedImageView?=null
    var tvName:TextView?=null
    var tvEmail:TextView?=null
    var tvPhone:TextView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = LayoutInflater.from(context).inflate(R.layout.fragment_account, container, false)

        val logout = view.findViewById<Button>(R.id.btn_logout)
        val changePwdLayout = view.findViewById<ConstraintLayout>(R.id.changePwdLayout)
        val faqLayout = view.findViewById<ConstraintLayout>(R.id.faqLayout)
        val tcLayout = view.findViewById<ConstraintLayout>(R.id.termsConditionLayout)
        val privacyPolicyLayout = view.findViewById<ConstraintLayout>(R.id.privacyPolicyLayout)
        val cart = view.findViewById<ImageView>(R.id.cart)
        val notifications = view.findViewById<ImageView>(R.id.image_notifications)
         profileImage = view.findViewById<RoundedImageView>(R.id.pickImage)
        val toggle = view.findViewById<SwitchMaterial>(R.id.switch_notification)
         tvName = view.findViewById<TextView>(R.id.tvName)
         tvEmail = view.findViewById<TextView>(R.id.tvEmail)
         tvPhone = view.findViewById<TextView>(R.id.tvPhone)

//        toggle.setOnClickListener {
//
//            isNotification = !isNotification
//            toggle.setImageResource(
//                if (isNotification)
//                    R.drawable.on
//                else
//                    R.drawable.off
//            )
//        }

        profileImage?.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }

        cart.setOnClickListener {
            val intent = Intent(activity, Cart::class.java)
            startActivity(intent)
        }

        notifications.setOnClickListener {
            val intent = Intent(activity, Notifications::class.java)
            startActivity(intent)
        }

        logout.setOnClickListener {
            activity?.finishAffinity()
            val intent = Intent(activity, UserLoginChoice::class.java)
            startActivity(intent)
        }
        changePwdLayout.setOnClickListener {
            val intent = Intent(activity, ChangePassword::class.java)
            startActivity(intent)
        }
        faqLayout.setOnClickListener {
            val intent = Intent(activity, FAQ::class.java)
            startActivity(intent)
        }
        tcLayout.setOnClickListener {
            val intent = Intent(activity, TermsCondition::class.java)
            startActivity(intent)
        }
        privacyPolicyLayout.setOnClickListener {
            val intent = Intent(activity, PrivacyPolicy::class.java)
            startActivity(intent)
        }

        if (getPreference(AppConstants.NOTIFICATION_TYPE, "").equals("1"))
            toggle.isChecked = true
        else  toggle.isChecked = false

        toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                type = "1"

            } else{
                type = "0"

            }

            notificationStatus()



        }
        appViewModel.profileApi(requireActivity(), true)
        appViewModel.getResponse().observe(this, this)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



    }

    override fun onResume() {
        super.onResume()
        appViewModel.profileApi(requireActivity(), true)
    }

    private fun notificationStatus() {

            val hashMap = HashMap<String, String>()
            hashMap["notification_status"] = type

            appViewModel.notificationStatusApi(requireActivity(), true, hashMap)
            appViewModel.getResponse().observe(this, this)

    }

    override fun onChanged(t: RestObservable?) {

        when (t!!.status) {
            Status.SUCCESS -> {
                if (t.data is NotificationStatusResponse) {

                    toast("Notification status changed successfully")

                }
                else if (t.data is ProfileResponse) {
                    val response: ProfileResponse = t.data

                    profileImage?.loadImage(AppConstants.IMAGE_USER_URL+response.body.image)

                    tvName?.text = response.body.username
                    tvEmail?.text = response.body.email
                    tvPhone?.text = response.body.countryCode+response.body.phone

                }
            }


        }


    }
}