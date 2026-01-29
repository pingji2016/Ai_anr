package com.example.tfdemo.fragments

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.nativecalc.NativeCalc
import com.example.tfdemo.R

class ImageFlipFragment : Fragment() {
    private var sourceBitmap: Bitmap? = null
    private lateinit var ivOriginal: ImageView
    private lateinit var ivResult: ImageView
    private lateinit var rbCpp: RadioButton
    private lateinit var rbJava: RadioButton
    private lateinit var rgMode: RadioGroup
    private lateinit var tvTime: TextView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            sourceBitmap = loadBitmap(uri)
            ivOriginal.setImageBitmap(sourceBitmap)
            ivResult.setImageDrawable(null)
            tvTime.text = ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_image_flip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivOriginal = view.findViewById(R.id.iv_original)
        ivResult = view.findViewById(R.id.iv_result)
        rbCpp = view.findViewById(R.id.rb_cpp)
        rbJava = view.findViewById(R.id.rb_java)
        rgMode = view.findViewById(R.id.rg_mode)
        tvTime = view.findViewById(R.id.tv_time)
        val btnPick: Button = view.findViewById(R.id.btn_pick)
        val btnFlip: Button = view.findViewById(R.id.btn_flip)

        btnPick.setOnClickListener { pickImage.launch("image/*") }
        btnFlip.setOnClickListener { runFlip() }
    }

    private fun runFlip() {
        val src = sourceBitmap ?: return
        val useCpp = rgMode.checkedRadioButtonId == R.id.rb_cpp

        val normalized = ensureSoftwareArgb8888(src)
        val t0 = SystemClock.elapsedRealtimeNanos()
        val result = if (useCpp) {
            NativeCalc.flipHorizontalBitmap(normalized)
        } else {
            flipJava(normalized)
        }
        val costMs = (SystemClock.elapsedRealtimeNanos() - t0) / 1_000_000.0
        ivResult.setImageBitmap(result)
        val prev = tvTime.text.toString()
        tvTime.text = (if (prev.isEmpty()) "" else prev + "\n") + "${if (useCpp) "C++" else "Java"} ${"%.3f".format(costMs)} ms"
    }

    private fun flipJava(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val dst = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val row = IntArray(w)
        for (y in 0 until h) {
            src.getPixels(row, 0, w, 0, y, w, 1)
            for (x in 0 until w / 2) {
                val t = row[x]
                row[x] = row[w - 1 - x]
                row[w - 1 - x] = t
            }
            dst.setPixels(row, 0, w, 0, y, w, 1)
        }
        return dst
    }

    private fun loadBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }
    }

    private fun ensureSoftwareArgb8888(src: Bitmap): Bitmap {
        val needsCopy = src.config != Bitmap.Config.ARGB_8888 || (Build.VERSION.SDK_INT >= 26 && src.config == Bitmap.Config.HARDWARE)
        return if (needsCopy) src.copy(Bitmap.Config.ARGB_8888, false) else src
    }
}
