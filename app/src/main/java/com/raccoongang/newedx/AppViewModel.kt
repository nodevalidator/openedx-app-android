package com.raccoongang.newedx

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.RoomDatabase
import com.raccoongang.core.BaseViewModel
import com.raccoongang.core.SingleEventLiveData
import com.raccoongang.core.data.storage.PreferencesManager
import com.raccoongang.newedx.system.notifier.AppNotifier
import com.raccoongang.newedx.system.notifier.LogoutEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(
    private val notifier: AppNotifier,
    private val room: RoomDatabase,
    private val preferencesManager: PreferencesManager,
    private val dispatcher: CoroutineDispatcher
) : BaseViewModel() {

    val logoutUser: LiveData<Unit>
        get() = _logoutUser
    private val _logoutUser = SingleEventLiveData<Unit>()

    private var logoutHandledAt: Long = 0

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        viewModelScope.launch {
            notifier.notifier.collect { event ->
                if (event is LogoutEvent && System.currentTimeMillis() - logoutHandledAt > 5000) {
                    logoutHandledAt = System.currentTimeMillis()
                    preferencesManager.clear()
                    withContext(dispatcher) {
                        room.clearAllTables()
                    }
                    _logoutUser.value = Unit
                }
            }
        }
    }

}