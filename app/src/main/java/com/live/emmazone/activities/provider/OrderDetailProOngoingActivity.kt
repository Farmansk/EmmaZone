package com.live.emmazone.activities.provider

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.emmazone.R
import com.live.emmazone.adapter.AdapterProvODNewSalesStatus
import com.live.emmazone.databinding.ActivityOrderDetailProOngoingBinding
import com.live.emmazone.model.ModelNewSaleOrderDetail
import com.live.emmazone.model.ModelOnGoingOrders

class OrderDetailProOngoingActivity : AppCompatActivity() {
    lateinit var binding: ActivityOrderDetailProOngoingBinding
    val list = ArrayList<ModelNewSaleOrderDetail>()
    val listChildRecycler = ArrayList<ModelOnGoingOrders>()
    lateinit var adapter: AdapterProvODNewSalesStatus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailProOngoingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnScan.setOnClickListener {
            QrScannerActivity.start(this@OrderDetailProOngoingActivity)
        }

        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.rvOrderDetailOnGoing.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        listChildRecycler.add(
            ModelOnGoingOrders(
                R.drawable.shoes_square, "Brend Shoe",
                "03", "90.00€",status = ""
            )
        )
        listChildRecycler.add(
            ModelOnGoingOrders(
                R.drawable.shoes_square, "Brend Shoe",
                "03", "90.00€",status = ""
            )
        )

        list.add(
            ModelNewSaleOrderDetail(
                "PLU9540572", R.drawable.avtarr_girl,
                "Allen Chandler",
                R.drawable.chat, listChildRecycler
            )
        )

        binding.rvOrderDetailOnGoing.adapter = AdapterProvODNewSalesStatus(this, list)

    }

}