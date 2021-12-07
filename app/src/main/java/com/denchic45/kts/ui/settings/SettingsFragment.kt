package com.denchic45.kts.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.denchic45.kts.R
import com.denchic45.kts.di.viewmodel.ViewModelFactory
import com.denchic45.kts.ui.HasViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(), HasViewModel<SettingsViewModel> {

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory<SettingsViewModel>
    override val viewModel: SettingsViewModel by viewModels { viewModelFactory }
//    private lateinit var bnv: BottomNavigationView
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        bnv = requireActivity().findViewById(R.id.bottom_nav_view)
//        bnv.visibility = View.GONE
        findPreference<Preference>(getString(R.string.key_signOut))!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference: Preference ->
                viewModel.onSignOutCLick()
                true
            }
    }

    override fun onDestroy() {
        super.onDestroy()
//        bnv.visibility = View.VISIBLE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }
}