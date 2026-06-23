package com.minhhuycoder.vidi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minhhuycoder.vidi.core.PlaceAdapter
import com.minhhuycoder.vidi.viewmodel.PlaceViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: PlaceViewModel
    private lateinit var recyclerView: RecyclerView

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

        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(this)[PlaceViewModel::class.java]

        // Lắng nghe dữ liệu
        viewModel.places.observe(this) { list ->
            if (list.isNotEmpty()) {
                val adapter = PlaceAdapter(list)
                recyclerView.adapter = adapter
                android.widget.Toast.makeText(this, "Đã tải ${list.size} địa điểm", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                // Báo lỗi ra màn hình luôn
                android.widget.Toast.makeText(this, "Firebase trả về danh sách RỖNG!", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
}