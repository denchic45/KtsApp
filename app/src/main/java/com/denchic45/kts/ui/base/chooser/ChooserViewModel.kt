package com.denchic45.kts.ui.base.chooser

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

abstract class ChooserViewModel<T : DomainModel> : BaseViewModel() {
    protected abstract val itemsFlow: Flow<List<T>>
    val items = itemsFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}