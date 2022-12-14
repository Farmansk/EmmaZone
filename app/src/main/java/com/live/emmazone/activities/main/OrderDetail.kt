package com.live.emmazone.activities.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.emmazone.R
import com.live.emmazone.adapter.AdapterOrderDetail
import com.live.emmazone.databinding.ActivityOrderDetailBinding
import com.live.emmazone.model.ModelOrderDetail

class OrderDetail : AppCompatActivity() {
    lateinit var binding: ActivityOrderDetailBinding
    lateinit var adapter: AdapterOrderDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnQRScanner.setOnClickListener {

            val alertDialog = AlertDialog.Builder(this)
            val factory = LayoutInflater.from(this)
            val view: View = factory.inflate(R.layout.dialog_scan_qr_code, null)

            val buttonCross = view.findViewById<ImageView>(R.id.crossImage)

            buttonCross.setOnClickListener {
                onBackPressed()
            }
            alertDialog.setView(view)
            alertDialog.show()
        }

        binding.btnCancel.setOnClickListener {

            val alertDialog = AlertDialog.Builder(this)
            val factory = LayoutInflater.from(this)
            val view: View = factory.inflate(R.layout.dialog_cancel_order, null)

            val buttonCancelYes = view.findViewById<Button>(R.id.btnCancelYes)
            val buttonCancelNo = view.findViewById<Button>(R.id.btnCancelNo)

            buttonCancelYes.setOnClickListener {
                onBackPressed()
            }

            buttonCancelNo.setOnClickListener {
                onBackPressed()
            }
            alertDialog.setView(view)
            alertDialog.show()
        }

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.recyclerOrderDetail.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}