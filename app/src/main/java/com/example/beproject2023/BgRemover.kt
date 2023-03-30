package com.example.beproject2023;

import android.graphics.Bitmap
import com.example.beproject2023.ClothInfo.*
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.beproject2023.databinding.ActivityMainBinding

class BgRemover(photo_cloth: Bitmap) : AppCompatActivity() {

    val image = photo_cloth

    private lateinit var binding: ActivityMainBinding
    private val imageResult =
            registerForActivityResult(
                    ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let { uri ->
//                    binding.img.setImageURI(uri)
                }
            }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        imageResult.launch("image/*")
        removeBg(image)


//        binding.removeBgBtn.setOnClickListener {
//            removeBg()
//        }



    }


    private fun removeBg(photo_cloth: Bitmap): Bitmap? {
        var bit: Bitmap? =null;
//        binding.img.invalidate()
        BackgroundRemover.bitmapForProcessing(
                photo_cloth,
                true,
                object : OnBackgroundChangeListener {
                    override fun onSuccess(bitmap: Bitmap) {
//                        binding.img.setImageBitmap(bitmap)
                        bit = bitmap
                        ClothInfo(bit)

                    }

                    override fun onFailed(exception: Exception) {
                        Toast.makeText(this@BgRemover, "Error Occur", Toast.LENGTH_SHORT).show()
                    }

                })
        return bit
    }


}