package com.denchic45.kts.ui.base.chooser

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.Equatable
import com.denchic45.kts.ui.base.BaseViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class ChooserViewModel<T : DomainModel> : BaseViewModel() {

    private val typeName: MutableSharedFlow<String> = MutableSharedFlow()

    private val _itemsFlow: Flow<List<T>> = typeName.flatMapLatest { sourceFlow(it) }

    protected abstract val sourceFlow: (String)->Flow<List<T>>

    val items: StateFlow<List<T>> = _itemsFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onItemClick(position: Int) {
        onItemSelect(items.value[position])
    }

    abstract fun onItemSelect(item: T)

    fun onNameType(typedName: String) {
        viewModelScope.launch { typeName.emit(typedName) }
    }
}