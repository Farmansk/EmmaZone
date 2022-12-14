package com.live.emmazone.activities.fragment

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import com.live.emmazone.MainActivity
import com.live.emmazone.R
import com.live.emmazone.activities.auth.LoginActivity
import com.live.emmazone.activities.auth.UserLoginChoice
import com.live.emmazone.activities.main.*
import com.live.emmazone.adapter.AdapterNearbyShops
import com.live.emmazone.adapter.ColorAdapter
import com.live.emmazone.adapter.SearchProductAdapter
import com.live.emmazone.adapter.SizeAdapter
import com.live.emmazone.databinding.BottomSheetFilterProductDialogBinding
import com.live.emmazone.databinding.FragmentHomeBinding
import com.live.emmazone.extensionfuncton.getPreference
import com.live.emmazone.extensionfuncton.savePreference
import com.live.emmazone.net.RestObservable
import com.live.emmazone.net.Status
import com.live.emmazone.response_model.*
import com.live.emmazone.utils.AppConstants
import com.live.emmazone.utils.AppUtils
import com.live.emmazone.utils.LocationUpdateUtilityFragment
import com.live.emmazone.view_models.AppViewModel
import kotlinx.android.synthetic.main.activity_search_product.*
import kotlinx.android.synthetic.main.bottom_sheet_filter_product_dialog.*
import kotlinx.android.synthetic.main.fragment_provider_home.view.*
import java.io.IOException
import java.util.*

class HomeFragment : LocationUpdateUtilityFragment(), Observer<RestObservable> {

    private val appViewModel: AppViewModel by viewModels()

    private var mLatitude = ""
    private var mLongitude = ""
    private var mDistance = ""

    private lateinit var binding: FragmentHomeBinding

    private val list = ArrayList<ShopListingResponse.Body.Shop>()
    lateinit var nearShopAdapter: AdapterNearbyShops
    private var selectedPos: Int? = null
    private val filterLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                mDistance = result.data?.getStringExtra(AppConstants.DISTANCE)!!
                mLatitude = result.data?.getStringExtra(AppConstants.LATITUDE)!!
                mLongitude = result.data?.getStringExtra(AppConstants.LONGITUDE)!!
                binding.tvLocation.text = result.data?.getStringExtra(AppConstants.LOCATION)!!

                if (mLatitude.isNotEmpty() && mLongitude.isNotEmpty()) {
                    shopListingApi()
                } else {
                    getLiveLocation(requireActivity())
                }

            }
        }
    //Switch Search Type
    private var isShopSearch = true
    private var searchAdapter: SearchProductAdapter? = null
    private val arrayList = ArrayList<SearchProductResponse.Body>()
    private var bottomDialog: BottomSheetDialog? = null
    var categoryID = ""
    var colorID = ""
    var sizeID = ""
    var catid = ""
    var colId = ""
    var sizId = ""
    var categoryName = ""
    var priceRangeValue: Boolean? = null
    var priceSort = ""
    var dropDownType = 0
    private val colorList = ArrayList<CategoryColorSizeResponse.Body.CategoryColor>()
    private val sizeList = ArrayList<CategoryColorSizeResponse.Body.CategorySize>()
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var sizeAdapter: SizeAdapter
    private val categoryList: java.util.ArrayList<CategoryListResponse.Body> = java.util.ArrayList()
    override fun updatedLatLng(lat: Double?, lng: Double?) {
        if (lat != null && lng != null) {
            if (activity != null) {
                mLatitude = lat.toString()
                mLongitude = lng.toString()

                shopListingApi()
                stopLocationUpdates()
                binding.tvLocation.text = completedAddress(lat, lng)

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        clicksHandle()
        getLiveLocation(requireActivity())

    }

    private fun clicksHandle() {
        if (getPreference(AppConstants.PROFILE_TYPE, "") == "guest") {
            binding.tvUserName.text = "Guest"
        } else {
            binding.tvUserName.text = getPreference(AppConstants.NAME, "").toString()
        }

        binding.btnClickHereHome.setOnClickListener {
            if (getPreference(AppConstants.PROFILE_TYPE, "") == "guest") {
                startActivity(Intent(activity, UserLoginChoice::class.java))
            } else {
                startActivity(
                    Intent(
                        activity,
                        LoginActivity::class.java
                    ).putExtra(AppConstants.USER_CHOICE, "3")
                )
            }
        }

        binding.cart.setOnClickListener {
            if (getPreference(AppConstants.PROFILE_TYPE, "") == AppConstants.GUEST) {
                (activity as MainActivity).showLoginDialog()
                return@setOnClickListener
            }
            val intent = Intent(activity, Cart::class.java)
            startActivity(intent)
        }

        binding.imageFilterHome.setOnClickListener {
            if(isShopSearch){
                val intent = Intent(activity, FilterActivity::class.java)
                intent.putExtra(AppConstants.DISTANCE, mDistance)
                intent.putExtra(AppConstants.LATITUDE, mLatitude)
                intent.putExtra(AppConstants.LONGITUDE, mLongitude)
                intent.putExtra(AppConstants.LOCATION, binding.tvLocation.text)
                filterLauncher.launch(intent)
            }
            else {
                showBottomDialog()
            }
        }

        binding.imageNotifications.setOnClickListener {
            if (getPreference(AppConstants.PROFILE_TYPE, "") == AppConstants.GUEST) {
                (activity as MainActivity).showLoginDialog()
                return@setOnClickListener
            }
            val intent = Intent(activity, Notifications::class.java)
            startActivity(intent)
        }

        binding.edtSearchWishList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                searchShopsFilter(s.toString())
            }

        })

        binding.ivMyOrder.setOnClickListener {
            val intent = Intent(requireContext(), SearchProductActivity::class.java)
            startActivity(intent)
        }
        //Radio Group
        binding.searchRadioGroup.setOnCheckedChangeListener { radioGroup, id->
            isShopSearch = ( id == binding.radioShop.id )
            switchSearchType()
        }
    }

    private fun locationEnableDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_enable_location_services)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val crossIcon = dialog.findViewById<ImageView>(R.id.crossImage)
        val btnEnable = dialog.findViewById<Button>(R.id.button_enable_services)

        crossIcon.setOnClickListener {
            dialog.dismiss()
        }

        btnEnable.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.show()
    }

    private fun setShopAdapter() {
        if (list.size > 0) {
            binding.tvNoShop.visibility = View.GONE
        } else {
            binding.tvNoShop.visibility = View.VISIBLE
        }

        nearShopAdapter = AdapterNearbyShops(list)
        binding.rvHomeNearbyShops.adapter = nearShopAdapter

        nearShopAdapter.onClickListener = { shopModel, clickOn ->
          /*  if (getPreference(AppConstants.PROFILE_TYPE, "") == "guest") {
                (context as MainActivity).showLoginDialog()
            } else {*/

                if (clickOn == "favourite") {
                    if (getPreference(AppConstants.PROFILE_TYPE, "") == "guest") {
                        (context as MainActivity).showLoginDialog()
                    } else {
                        selectedPos = list.indexOf(shopModel)
                        favUnFavApiHit(shopModel)
                    }

                } else if (clickOn == "itemClick") {
                    val intent = Intent(requireContext(), ShopDetailActivity::class.java)
                    intent.putExtra(AppConstants.SHOP_ID, shopModel.id.toString())
                    startActivity(intent)
                } else if (clickOn == "rating") {
                    if (getPreference(AppConstants.PROFILE_TYPE, "") == AppConstants.GUEST) {
                        (requireContext().applicationContext as MainActivity).showLoginDialog()
                    } else {
                        val intent = Intent(requireContext(), ShopReviewsActivity::class.java)
                        intent.putExtra(AppConstants.SHOP_LISTING_RESPONSE, shopModel)
                        startActivity(intent)
                    }

                }

            /*}*/
        }
    }

    private fun searchShopsFilter(text: String) {
        val filterList = ArrayList<ShopListingResponse.Body.Shop>()

        list.forEach {
            if (it.shopName.contains(text, true)) {
                filterList.add(it)
            }
        }

        nearShopAdapter.notifyData(filterList)
    }

    private fun shopListingApi() {
        val hashMap = HashMap<String, String>()
//        hashMap["latitude"] = "30.7333" //for testing
//        hashMap["longitude"] = "76.7794" //for testing
        hashMap["latitude"] = mLatitude
        hashMap["longitude"] = mLongitude
        hashMap["min_distance"] = "0"
        hashMap["max_distance"] = mDistance

        appViewModel.shopListApi(requireActivity(), true, hashMap)
        appViewModel.getResponse().observe(requireActivity(), this)

    }

    private fun favUnFavApiHit(shopResponse: ShopListingResponse.Body.Shop) {
        val hashMap = HashMap<String, String>()
        hashMap["vendorId"] = shopResponse.userId.toString()

        if (shopResponse.isLiked == 1)
            hashMap["status"] = "0"
        else {
            hashMap["status"] = "1"
        }

        appViewModel.addFavApi(requireActivity(), true, hashMap)
        appViewModel.getResponse().observe(requireActivity(), this)
    }

    override fun onChanged(t: RestObservable?) {
        when (t!!.status) {
            Status.SUCCESS -> {
                if (t.data is ShopListingResponse) {
                    val response: ShopListingResponse = t.data

                    if (response.code == AppConstants.SUCCESS_CODE) {
                        list.clear()
                        list.addAll(t.data.body.shopList)

                        setShopAdapter()

                        if (response.body.notificationCount == 0) {
                            binding.notifyRedBG.visibility = View.GONE
                        } else {
                            binding.notifyRedBG.visibility = View.VISIBLE
                        }

                        if (response.body.cartCount == 0) {
                            binding.ivRedCart.visibility = View.GONE
                        } else {
                            binding.ivRedCart.visibility = View.VISIBLE
                        }

                    }

                } else if (t.data is AddFavouriteResponse) {
                    val response: AddFavouriteResponse = t.data

                    if (response.code == AppConstants.SUCCESS_CODE) {

                        list[selectedPos!!].isLiked = response.body.status
                        nearShopAdapter.notifyDataSetChanged()
//                        setShopAdapter()
                        AppUtils.showMsgOnlyWithoutClick(requireActivity(), response.message)

                    }

                }

            }
            Status.ERROR -> {
                if (list.size > 0) {
                    binding.tvNoShop.visibility = View.GONE
                } else {
                    binding.tvNoShop.visibility = View.VISIBLE
                }
            }
            else -> {}
        }


    }

    override fun onResume() {
        super.onResume()
//        getLiveLocation(requireActivity())
    }

    private fun completedAddress(latitude: Double, longitude: Double): String {
        var addresses: List<Address>? = null
        var city: String? = ""
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        var location = ""

        try {
            addresses = geoCoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val countryName = addresses[0].countryName
                val state = addresses[0].subAdminArea
                val address = addresses[0].featureName
                val island = addresses[0].adminArea
// val city = addresses[0].locality


                try {
                    city = addresses[0].locality
                    if (city == null) city = addresses[0].subLocality
                    if (city == null) city = addresses[0].subAdminArea
                    if (city == null) city = addresses[0].adminArea
// addressCity = city
                } catch (e: Exception) {
// addressCity = ""
                }

                val pinCode = addresses[0].postalCode
                val latitudeAddress = addresses[0].latitude
                val longitudeAddress = addresses[0].longitude

// If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                location = addresses[0].getAddressLine(0)


            }
        } catch (e: IOException) {
            e.printStackTrace()
            location = ""
        }

        return location
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
    // Feature : Switch Search Type
    private fun switchSearchType(){
        if(isShopSearch){
            binding.shopsLayout.visibility = View.VISIBLE
            binding.productsLayout.visibility = View.GONE
        }
        else {
            binding.shopsLayout.visibility = View.GONE
            binding.productsLayout.visibility = View.VISIBLE
        }
    }
    private fun searchApiHit(s: String) {
        val hashMap = HashMap<String, String>()
        hashMap["keyword"] = s
        appViewModel.searchProductApi(requireActivity(), hashMap, false)
        appViewModel.getResponse().observe(this, this)
    }

    private fun filterApiHit(s: String) {
        catid = if (categoryID == "0") {
            ""
        } else {
            categoryID
        }
        colId = if (colorID == "0") {
            ""
        } else {
            colorID
        }
        sizId = if (sizeID == "0") {
            ""
        } else {
            sizeID
        }
        val hashMap = HashMap<String, String>()
        hashMap["keyword"] = s
        hashMap["categoryId"] = catid
        hashMap["categoryColorId"] = colId
        hashMap["categorySizeId"] = sizId
        hashMap["max_price"] = bottomDialog!!.seekBar.value.toInt().toString().trim()
        hashMap["price_sort"] = priceSort
        appViewModel.filterProductApi(requireActivity(), hashMap, false)
        appViewModel.getResponse().observe(viewLifecycleOwner, this)
    }
    private fun setSearchAdapter() {
        searchAdapter = SearchProductAdapter(arrayList)
        rvSearchProduct.adapter = searchAdapter
        searchAdapter?.onItemClick = { pos ->
            val intent = Intent(requireActivity(), ProductDetailActivity::class.java)
            intent.putExtra(AppConstants.USER2_NAME, arrayList[pos].vendorDetail.shopName)
            intent.putExtra(AppConstants.USER2_IMAGE, arrayList[pos].vendorDetail.image)
            intent.putExtra(AppConstants.SHOP_NAME_VISIBLE, true)
            intent.putExtra("productId", arrayList[pos].id.toString())
            startActivity(intent)
        }
    }

    private fun showBottomDialog() {
        AppUtils.hideSoftKeyboard(requireActivity())
        bottomDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_filter_product_dialog, null)
        bottomDialog?.setCancelable(true)
        bottomDialog?.setContentView(view)
        (bottomDialog as BottomSheetDialog).behavior.peekHeight = 1800

        var tvSelectCategory = view.findViewById<AppCompatTextView>(R.id.tvSelectCategory)
        val tvSelectColor = view.findViewById<AppCompatTextView>(R.id.tvSelectColor)
        val tvSelectSize = view.findViewById<AppCompatTextView>(R.id.tvSelectSize)
        val seekBar = view.findViewById<Slider>(R.id.seekBar)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val radioButton = view.findViewById<RadioButton>(R.id.radioButton)
        val radioButton2 = view.findViewById<RadioButton>(R.id.radioButton2)
        val radioButton3 = view.findViewById<RadioButton>(R.id.radioButton3)
        val btnDone = view.findViewById<Button>(R.id.btnDone)


        if (getPreference(AppConstants.IS_FILTER, false)) {
            tvSelectCategory.text = getPreference(AppConstants.CATEGORY_NAME, "").toString()
            categoryID = getPreference(AppConstants.CATEGORY_ID, "").toString()
            colorID = getPreference(AppConstants.COLOUR_ID, "").toString()
            tvSelectColor.text = getPreference(AppConstants.COLOUR_NAME, "").toString()
            tvSelectSize.text = getPreference(AppConstants.SIZE_NAME, "").toString()
            sizeID = getPreference(AppConstants.SIZE_ID, "").toString()

            when {
                getPreference(AppConstants.PRICE_RANGE, "") == "1" -> {
                    radioButton.isChecked = true
                    priceSort = "1"
                }
                getPreference(AppConstants.PRICE_RANGE, "") == "2" -> {
                    radioButton2.isChecked = true
                    priceSort = "2"
                }
                else -> {
                    radioButton3.isChecked = true
                    priceSort = ""
                }
            }

            if (getPreference(AppConstants.PRICE, "") != "0.0f") {
                seekBar.value = getPreference(AppConstants.PRICE, "").toFloat()
            } else {
                seekBar.value = 0.0f
            }


        }

        tvSelectCategory.setOnClickListener {
            dropDownType = AppConstants.CATEGORY_DROP_DOWN
            appViewModel.categoryListApi(requireActivity(), true)
            appViewModel.getResponse().observe(viewLifecycleOwner, this)
        }

        tvSelectColor.setOnClickListener {
            if (categoryID.isNotEmpty() && categoryID != "0") {
                dropDownType = AppConstants.COLOUR_DROP_DOWN
                getColorSizeApiHit()

            } else {
                Toast.makeText(requireContext(), "Please select category", Toast.LENGTH_SHORT)
                    .show();
            }
        }
        tvSelectSize.setOnClickListener {
            if (categoryID.isNotEmpty() && categoryID != "0") {
                dropDownType = AppConstants.SIZE_DROP_DOWN
                getColorSizeApiHit()
            } else {
                Toast.makeText(requireContext(), "Please select category", Toast.LENGTH_SHORT)
                    .show();
            }

        }

        btnDone.setOnClickListener {
            savePreference(AppConstants.IS_FILTER, true)
            if (tvSelectCategory.text.toString().trim().isNotEmpty()) {
                savePreference(AppConstants.CATEGORY_NAME, tvSelectCategory.text.toString().trim())
                savePreference(AppConstants.CATEGORY_ID, categoryID)
            }

            if (tvSelectColor.text.toString().trim().isNotEmpty()) {
                savePreference(AppConstants.COLOUR_NAME, tvSelectColor.text.toString().trim())
                savePreference(AppConstants.COLOUR_ID, colorID)
            }

            if (tvSelectSize.text.toString().trim().isNotEmpty()) {
                savePreference(AppConstants.SIZE_NAME, tvSelectSize.text.toString().trim())
                savePreference(AppConstants.SIZE_ID, sizeID)
            }
            if (seekBar.value != 0.0f) {
                savePreference(AppConstants.PRICE, seekBar.value.toString())


            } else {
                savePreference(AppConstants.PRICE, 0.0f.toString())
            }
            Log.d("seekBar.value", seekBar.value.toString())
            //   if (radioButton.isChecked || radioButton2.isChecked) {
            if (radioButton.isChecked) {
                savePreference(AppConstants.PRICE_RANGE, "1")
                priceSort = "1"
            } else if (radioButton2.isChecked) {
                savePreference(AppConstants.PRICE_RANGE, "2")
                priceSort = "2"

            } else if (radioButton3.isChecked) {
                savePreference(AppConstants.PRICE_RANGE, " ")
                priceSort = ""

            }
            //  }

            filterApiHit(edtSearch.text.toString().trim())
            bottomDialog!!.dismiss()
        }

        bottomDialog?.show()
    }

    private fun getColorSizeApiHit() {
        val hashMap = HashMap<String, String>()
        hashMap["categoryId"] = categoryID
        appViewModel.categoryColorSizeApi(requireActivity(), true, hashMap)
        appViewModel.getResponse().observe(viewLifecycleOwner, this)
    }

    lateinit var dialog: Dialog
}