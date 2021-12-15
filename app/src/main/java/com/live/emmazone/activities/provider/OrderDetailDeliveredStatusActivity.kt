package com.live.emmazone.activities.provider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.emmazone.R
import com.live.emmazone.activities.Interface.OnItemClick
import com.live.emmazone.adapter.AdapterProvODNewSalesStatus
import com.live.emmazone.databinding.ActivityOrderDetailDeliveredStatusBinding
import com.live.emmazone.model.ModelNewSaleOrderDetail
import com.live.emmazone.model.ModelOnGoingOrders

class OrderDetailDeliveredStatusActivity : AppCompatActivity(){
    lateinit var binding : ActivityOrderDetailDeliveredStatusBinding
    val list = ArrayList<ModelNewSaleOrderDetail>()
    val listChildRecycler = ArrayList<ModelOnGoingOrders>()
    lateinit var adapter : AdapterProvODNewSalesStatus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailDeliveredStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.rvOrderDetailDelivered.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        listChildRecycler.add(ModelOnGoingOrders(R.drawable.shoes_square, "Brend Shoe",
            "03", "90.00€"))
        listChildRecycler.add(ModelOnGoingOrders(R.drawable.shoes_square, "Brend Shoe",
            "03", "90.00€"))

        list.add(ModelNewSaleOrderDetail("PLU9540572", R.drawable.avtarr_girl,
            "Allen Chandler",
            R.drawable.chat, listChildRecycler))

        binding.rvOrderDetailDelivered.adapter = AdapterProvODNewSalesStatus(list)

    }

}