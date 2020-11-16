package com.dorcaapps.android.ktorclient.ui.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.dorcaapps.android.ktorclient.R
import com.dorcaapps.android.ktorclient.ui.base.BaseFragment

abstract class DetailBaseFragment: BaseFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_delete -> {
                onDeleteClicked { layoutId ->
                    if (layoutId == null) {
                        item.actionView = null
                    } else {
                        item.setActionView(layoutId)
                    }
                }
                true
            }
            else -> false
        }
    }

    abstract fun onDeleteClicked(setDeleteActionView: (layoutId: Int?) -> Unit)
}