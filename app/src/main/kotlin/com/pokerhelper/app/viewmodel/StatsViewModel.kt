package com.pokerhelper.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokerhelper.app.data.HandHistoryRepository
import com.pokerhelper.app.data.HandStats
import com.pokerhelper.core.model.PlayedHand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    private val repository: HandHistoryRepository
) : ViewModel() {

    val hands: StateFlow<List<PlayedHand>> = repository.hands
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val stats: StateFlow<HandStats> = repository.hands
        .map { HandStats.from(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HandStats.from(emptyList()))

    fun clearHistory() = repository.clear()
}