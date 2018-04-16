package fr.vbe.android.ui.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.MenuRes
import android.support.v7.app.AppCompatActivity
import fr.vbe.android.ui.R
import fr.vbe.android.ui.databinding.LibUiActivityBottomNavigationBinding

abstract class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: LibUiActivityBottomNavigationBinding
    val container by lazy {
        binding.container
    }

    @MenuRes
    abstract fun menuRes(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.lib_ui__activity_bottom_navigation)
        binding.navigation.inflateMenu(menuRes())
    }
}