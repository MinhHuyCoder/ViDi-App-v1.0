package com.minhhuycoder.vidi


import android.content.Intent // === THÊM: Thư viện để chuyển màn hình ===
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minhhuycoder.vidi.core.PlaceAdapter
import com.minhhuycoder.vidi.viewmodel.PlaceViewModel
import com.minhhuycoder.vidi.models.PlaceModel
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PlaceAdapter
    private lateinit var viewModel: PlaceViewModel
    private lateinit var recyclerView: RecyclerView

    private var fullPlaceList = listOf<PlaceModel>()

    private var searchKeyword = ""
    private var selectedCategory = "ALL"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // Setup insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // Khởi tạo RecyclerView và LayoutManager
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = PlaceAdapter()

        recyclerView.adapter = adapter



        // === THÊM: Đóng gói placeId của quán được click ném sang màn hình chi tiết ===
        adapter.setOnItemClickListener { place ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("PLACE_ID", place.placeId) // Đẩy biến "placeId" từ Hợp đồng dữ liệu
            startActivity(intent)
        }


        adapter.setOnFavoriteClickListener { place ->

            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                return@setOnFavoriteClickListener
            }

            val favorite = hashMapOf(
                "userId" to currentUser.uid,
                "placeId" to place.placeId,
                "createdAt" to System.currentTimeMillis()
            )

            val db = FirebaseFirestore.getInstance()

            db.collection("favorites")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("placeId", place.placeId)
                .get()
                .addOnSuccessListener { documents ->

                    if (documents.isEmpty) {

                        db.collection("favorites")
                            .add(favorite)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Đã thêm vào yêu thích",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Thêm thất bại",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                    } else {

                        Toast.makeText(
                            this,
                            "Địa điểm đã có trong yêu thích",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                }
        }

        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(this)[PlaceViewModel::class.java]

        // Lắng nghe dữ liệu
        viewModel.places.observe(this) { list ->


            fullPlaceList = list
            applyFilter()

            list.forEach {
                println("Category = ${it.category}")
            }

            if (list.isNotEmpty()) {

                Toast.makeText(
                    this,
                    "Đã tải ${list.size} địa điểm",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Firebase trả về danh sách RỖNG!",
                    Toast.LENGTH_LONG
                ).show()

            }

        }

        val etSearch = findViewById<EditText>(R.id.etSearch)

        etSearch.addTextChangedListener {

            searchKeyword = it.toString().trim()

            applyFilter()

        }

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupFilter)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->

            when (checkedIds.firstOrNull()) {

                R.id.chipall -> selectedCategory = "ALL"

                R.id.chipCheckin -> selectedCategory = "CHECKIN"

                R.id.chipCafe -> selectedCategory = "CAFE"

                R.id.chipEatery -> selectedCategory = "EATERY"

            }

            applyFilter()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {

                    true
                }

                R.id.nav_favorite -> {

                    startActivity(
                        Intent(
                            this,
                            WishlistActivity::class.java
                        )
                    )

                    true
                }

                R.id.nav_notifications -> {

                    Toast.makeText(
                        this,
                        "Đang phát triển",
                        Toast.LENGTH_SHORT
                    ).show()

                    true
                }


                R.id.nav_profile -> {

                    startActivity(
                        Intent(
                            this,
                            ProfileActivity::class.java
                        )
                    )

                    true
                }

                else -> false
            }
        }




    }
    private fun applyFilter() {

        var result = fullPlaceList

        when (selectedCategory) {

            "CHECKIN" -> {
                result = result.filter {
                    it.category.contains("check", true)
                }
            }

            "CAFE" -> {
                result = result.filter {
                    it.category.contains("cafe", true)
                }
            }

            "EATERY" -> {
                result = result.filter {
                    it.category.contains("quán", true)
                            || it.category.contains("ăn", true)
                            || it.category.contains("restaurant", true)
                }

            }
        }

        if (searchKeyword.isNotEmpty()) {
            result = result.filter {
                it.name.contains(searchKeyword, true)
            }
        }

        adapter.submitList(result)
    }
    override fun onResume() {
        super.onResume()

        viewModel.loadPlaces()
    }
}
