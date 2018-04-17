package fr.vbe.android.ui.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.MenuRes
import android.support.v7.app.AppCompatActivity
import fr.vbe.android.ui.R
import fr.vbe.android.ui.databinding.VbeActivityBottomNavigationBinding

abstract class VBEBottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: VbeActivityBottomNavigationBinding
    val container by lazy {
        binding.vbeContainer
    }

    @MenuRes
    abstract fun menuRes(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.vbe_activity_bottom_navigation)
        binding.vbeNavigation.inflateMenu(menuRes())
    }
}