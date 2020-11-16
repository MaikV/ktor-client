package com.dorcaapps.android.ktorclient.ui.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.dorcaapps.android.ktorclient.R

abstract class BaseFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
    }

    protected fun navigate(navDirections: NavDirections) {
        val oldNavOptions =
            findNavController().currentDestination?.getAction(navDirections.actionId)?.navOptions

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in)
            .setExitAnim(R.anim.slide_out)
            .setPopEnterAnim(R.anim.pop_slide_in)
            .setPopExitAnim(R.anim.pop_slide_out)
            .setPopUpTo(oldNavOptions?.popUpTo ?: -1, oldNavOptions?.isPopUpToInclusive ?: false)
            .build()

        findNavController().navigate(navDirections, navOptions)
    }

    private fun setupToolbar() {
        val appBarConfiguration =
            AppBarConfiguration(setOf(R.id.loginFragment, R.id.pagingFragment))
        getToolbar().setupWithNavController(findNavController(), appBarConfiguration)
    }

    abstract fun getToolbar(): Toolbar
}