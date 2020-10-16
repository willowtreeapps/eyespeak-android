package com.willowtree.vocable.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import com.willowtree.vocable.BaseFragment
import com.willowtree.vocable.BindingInflater
import com.willowtree.vocable.R
import com.willowtree.vocable.databinding.FragmentThemesBinding
import com.willowtree.vocable.utils.VocableSharedPreferences
import org.koin.android.ext.android.inject

class ThemesFragment : BaseFragment<FragmentThemesBinding>() {

    override val bindingInflater: BindingInflater<FragmentThemesBinding> = FragmentThemesBinding::inflate
    private lateinit var viewModel: SettingsViewModel
    private val sharedPrefs: VocableSharedPreferences by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.themesButtonDefault.action = {
            sharedPrefs.setSelectedTheme(R.style.DefaultTheme)
            requireActivity().recreate()
        }

        binding.themesButtonRed.action = {
            sharedPrefs.setSelectedTheme(R.style.RedAndYellow)
            requireActivity().recreate()
        }

        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {

    }

    override fun getAllViews(): List<View> {
        return listOf()
    }
}