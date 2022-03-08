package com.live.emmazone.activities.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.emmazone.R
import com.live.emmazone.activities.listeners.OnActionListenerNew
import com.live.emmazone.activities.main.OrderDetail
import com.live.emmazone.adapter.AdapterOnGoingUserOrders
import com.live.emmazone.databinding.OnGoingOrdersFragmentBinding
import com.live.emmazone.net.RestObservable
import com.live.emmazone.net.Status
import com.live.emmazone.response_model.SalesResponse
import com.live.emmazone.response_model.UserOrderListing
import com.live.emmazone.view_models.AppViewModel

class OnGoingOrdersFragment : Fragment(), View.OnClickListener, Observer<RestObservable> {

    private val appViewModel: AppViewModel by viewModels()
    val list = ArrayList<UserOrderListing.OrderListBody>()

    lateinit var adapter: AdapterOnGoingUserOrders

    private lateinit var binding: OnGoingOrdersFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = OnGoingOrdersFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClicks()

        val onActionListenerNew = object : OnActionListenerNew {
            override fun notifyOnClick() {
                //binding.imgCodeScanner.performClick()
            }
        }
        setAdapter(onActionListenerNew)
        getMyOrdersApi()

    }

    private fun getMyOrdersApi() {  // 1 => past orders, 2 => ongoing orders
        val hashMap= HashMap<String,String>()
        hashMap["status"]="1"
        appViewModel.orderListingApi(requireActivity(),hashMap,true)
        appViewModel.mResponse.observe(this,this)
    }

    private fun setOnClicks() {

    }

    private fun setAdapter(onActionListenerNew: OnActionListenerNew) {
        adapter= AdapterOnGoingUserOrders(requireContext(), list,onActionListenerNew)
        binding.rvOnGoingOrders.adapter = adapter
    }

    override fun onClick(v: View?) {

        when(v?.id){
            R.id.imgCodeScanner ->{
                val dialog = Dialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(true)

                dialog.setContentView(R.layout.dialog_scan_qr_code)

                dialog.window?.apply {

                    setLayout(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            android.R.color.transparent
                        )
                    )
                }

                val backIcon = dialog.findViewById<ImageView>(R.id.crossImage)

                backIcon.setOnClickListener { dialog.dismiss() }

                dialog.show()
            }

            R.id.btnStatusOnTheWay ->{
                val intent = Intent(activity, OrderDetail::class.java)
                startActivity(intent)
            }
        }

    }

    override fun onChanged(t: RestObservable?) {
        when (t!!.status) {
            Status.SUCCESS -> {
                if (t.data is UserOrderListing) {
                    list.clear()
                    list.addAll(t.data.body)
                    if(list.size>0){
                        binding.tvNoData.visibility= View.GONE
                        binding.rvOnGoingOrders.visibility= View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }else{
                        binding.tvNoData.visibility= View.VISIBLE
                        binding.rvOnGoingOrders.visibility= View.GONE
                    }

                }
            }
        }
    }

}