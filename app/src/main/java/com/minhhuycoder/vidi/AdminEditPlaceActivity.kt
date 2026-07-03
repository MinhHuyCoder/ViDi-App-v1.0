package com.minhhuycoder.vidi

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.models.PlaceModel
import java.net.URL

class AdminEditPlaceActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var spinnerCategory: AutoCompleteTextView
    private lateinit var etAddress: EditText
    private lateinit var etDescription: EditText
    private lateinit var etImageUrl: EditText
    private lateinit var etOpenHour: EditText
    private lateinit var etCloseHour: EditText
    private lateinit var switchStatus: CompoundButton

    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvRatingValue: TextView
    private lateinit var tvReviewCountValue: TextView
    private lateinit var tvPlaceIdValue: TextView

    private lateinit var ivPlacePreview: ImageView

    private lateinit var btnBack: View
    private lateinit var btnCancel: View
    private lateinit var btnPreviewImage: View
    private lateinit var btnSubmit: TextView

    private val db = FirebaseFirestore.getInstance()

    private var placeId: String = ""
    private var isEditMode: Boolean = false

    // Lưu lại dữ liệu hệ thống, không cho sửa trực tiếp
    private var oldRating: Double = 0.0
    private var oldReviewCount: Int = 0
    private var oldLatitude: Double = 0.0
    private var oldLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_place)

        // Nhận placeId từ AdminActivity nếu đang sửa
        placeId = intent.getStringExtra("placeId") ?: ""
        isEditMode = placeId.isNotEmpty()

        initViews()
        setupCategory()
        setupScreenMode()
        setupEvents()
    }

    private fun initViews() {
        // Ánh xạ đúng ID trong activity_admin_edit_place.xml
        etName = findViewById(R.id.etName)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etAddress = findViewById(R.id.etAddress)
        etDescription = findViewById(R.id.etDescription)
        etImageUrl = findViewById(R.id.etImageUrl)
        etOpenHour = findViewById(R.id.etOpenHour)
        etCloseHour = findViewById(R.id.etCloseHour)
        switchStatus = findViewById(R.id.switchStatus)

        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        tvRatingValue = findViewById(R.id.tvRatingValue)
        tvReviewCountValue = findViewById(R.id.tvReviewCountValue)
        tvPlaceIdValue = findViewById(R.id.tvPlaceIdValue)

        ivPlacePreview = findViewById(R.id.ivPlacePreview)

        btnBack = findViewById(R.id.btnBack)
        btnCancel = findViewById(R.id.btnCancel)
        btnPreviewImage = findViewById(R.id.btnPreviewImage)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    private fun setupCategory() {
        // Giữ danh mục đơn giản, không thêm field mới
        val categories = listOf("Cafe", "Quán ăn", "Check-in", "Công viên", "Khác")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            categories
        )

        spinnerCategory.setAdapter(adapter)
    }

    private fun setupScreenMode() {
        if (isEditMode) {
            // Màn sửa địa điểm
            tvTitle.text = "Sửa địa điểm"
            tvSubtitle.text = "Cập nhật thông tin địa điểm"
            btnSubmit.text = "Cập nhật"
            tvPlaceIdValue.text = placeId

            loadPlaceForEdit()
        } else {
            // Màn thêm địa điểm
            tvTitle.text = "Thêm địa điểm"
            tvSubtitle.text = "Điền thông tin địa điểm"
            btnSubmit.text = "Lưu thay đổi"

            oldRating = 0.0
            oldReviewCount = 0
            oldLatitude = 0.0
            oldLongitude = 0.0

            tvRatingValue.text = "0.0 ★"
            tvReviewCountValue.text = "0"
            tvPlaceIdValue.text = "Tự tạo sau khi lưu"
        }
    }

    private fun setupEvents() {
        btnBack.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnPreviewImage.setOnClickListener {
            val imageUrl = etImageUrl.text.toString().trim()

            if (!isValidUrl(imageUrl)) {
                etImageUrl.error = "Link ảnh phải bắt đầu bằng http hoặc https"
                etImageUrl.requestFocus()
                Toast.makeText(this, "URL ảnh không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Xem trước ảnh theo đúng link người dùng dán
            previewImage(imageUrl)
        }

        btnSubmit.setOnClickListener {
            savePlace()
        }
    }

    private fun loadPlaceForEdit() {
        if (placeId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy Place ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Lấy dữ liệu cũ từ Firestore để đổ lên form sửa
        db.collection("places")
            .document(placeId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(this, "Địa điểm không tồn tại", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val place = document.toObject(PlaceModel::class.java)

                if (place == null) {
                    Toast.makeText(this, "Không đọc được dữ liệu địa điểm", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                // Lưu lại dữ liệu hệ thống để không sửa trực tiếp rating/reviewCount
                oldRating = place.rating
                oldReviewCount = place.reviewCount
                oldLatitude = place.latitude
                oldLongitude = place.longitude

                // Đổ dữ liệu cũ lên form
                etName.setText(place.name)
                spinnerCategory.setText(place.category, false)
                etAddress.setText(place.address)
                etDescription.setText(place.description)
                etImageUrl.setText(place.imageUrl)
                etOpenHour.setText(place.openTime)
                etCloseHour.setText(place.closeTime)
                switchStatus.isChecked = place.status

                tvRatingValue.text = "${place.rating} ★"
                tvReviewCountValue.text = place.reviewCount.toString()
                tvPlaceIdValue.text = placeId

                // Nếu có link ảnh thì tự xem trước
                if (place.imageUrl.isNotEmpty() && isValidUrl(place.imageUrl)) {
                    previewImage(place.imageUrl)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi tải địa điểm: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun savePlace() {
        val name = etName.text.toString().trim()
        val category = spinnerCategory.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val imageUrl = etImageUrl.text.toString().trim()
        val openTime = etOpenHour.text.toString().trim()
        val closeTime = etCloseHour.text.toString().trim()
        val status = switchStatus.isChecked

        // Validate dữ liệu bắt buộc trước khi lưu Firebase
        if (name.isEmpty()) {
            etName.error = "Vui lòng nhập tên địa điểm"
            etName.requestFocus()
            return
        }

        if (category.isEmpty()) {
            spinnerCategory.error = "Vui lòng chọn danh mục"
            spinnerCategory.requestFocus()
            return
        }

        if (address.isEmpty()) {
            etAddress.error = "Vui lòng nhập địa chỉ"
            etAddress.requestFocus()
            return
        }

        if (description.isEmpty()) {
            etDescription.error = "Vui lòng nhập mô tả"
            etDescription.requestFocus()
            return
        }

        if (!isValidUrl(imageUrl)) {
            etImageUrl.error = "Link ảnh phải bắt đầu bằng http hoặc https"
            etImageUrl.requestFocus()
            return
        }

        if (openTime.isEmpty()) {
            etOpenHour.error = "Vui lòng nhập giờ mở"
            etOpenHour.requestFocus()
            return
        }

        if (closeTime.isEmpty()) {
            etCloseHour.error = "Vui lòng nhập giờ đóng"
            etCloseHour.requestFocus()
            return
        }

        if (isEditMode) {
            updatePlace(
                name,
                category,
                address,
                description,
                imageUrl,
                openTime,
                closeTime,
                status
            )
        } else {
            addNewPlace(
                name,
                category,
                address,
                description,
                imageUrl,
                openTime,
                closeTime,
                status
            )
        }
    }

    private fun addNewPlace(
        name: String,
        category: String,
        address: String,
        description: String,
        imageUrl: String,
        openTime: String,
        closeTime: String,
        status: Boolean
    ) {
        // Tạo document mới để lấy placeId trước khi lưu
        val docRef = db.collection("places").document()

        val newPlace = PlaceModel(
            placeId = docRef.id,
            name = name,
            category = category,
            address = address,
            description = description,
            imageUrl = imageUrl,
            latitude = 0.0,
            longitude = 0.0,
            openTime = openTime,
            closeTime = closeTime,
            rating = 0.0,
            reviewCount = 0,
            status = status
        )

        // Lưu Place mới vào collection places
        docRef.set(newPlace)
            .addOnSuccessListener {
                Toast.makeText(this, "Thêm địa điểm thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi thêm địa điểm: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun updatePlace(
        name: String,
        category: String,
        address: String,
        description: String,
        imageUrl: String,
        openTime: String,
        closeTime: String,
        status: Boolean
    ) {
        if (placeId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy Place ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Chỉ update các field được phép sửa
        val updateData = hashMapOf<String, Any>(
            "placeId" to placeId,
            "name" to name,
            "category" to category,
            "address" to address,
            "description" to description,
            "imageUrl" to imageUrl,
            "latitude" to oldLatitude,
            "longitude" to oldLongitude,
            "openTime" to openTime,
            "closeTime" to closeTime,
            "status" to status
        )

        // Không update rating và reviewCount ở đây
        db.collection("places")
            .document(placeId)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật địa điểm thành công", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi cập nhật địa điểm: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun isValidUrl(url: String): Boolean {
        val imageUrl = url.trim()

        // Không cho để trống link ảnh
        if (imageUrl.isEmpty()) {
            return false
        }

        // Theo yêu cầu: link ảnh phải bắt đầu bằng http hoặc https
        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            return false
        }

        return true
    }

    private fun previewImage(imageUrl: String) {
        // Gắn tag để tránh ảnh tải chậm set nhầm vào ImageView
        ivPlacePreview.tag = imageUrl
        ivPlacePreview.setImageResource(android.R.drawable.ic_menu_gallery)

        Thread {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())

                ivPlacePreview.post {
                    // Chỉ hiện ảnh nếu vẫn đúng link đang xem trước
                    if (ivPlacePreview.tag == imageUrl) {
                        ivPlacePreview.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                ivPlacePreview.post {
                    if (ivPlacePreview.tag == imageUrl) {
                        ivPlacePreview.setImageResource(android.R.drawable.ic_menu_gallery)
                    }

                    Toast.makeText(
                        this,
                        "Không tải được ảnh từ link này",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }
}