package mx.datafox.climapersonal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mx.datafox.climapersonal.databinding.BarriersLayoutBinding

class Barriers: AppCompatActivity() {
    private lateinit var binding: BarriersLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BarriersLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}