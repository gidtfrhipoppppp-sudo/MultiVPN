package com.multivpn.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.multivpn.app.domain.repository.VpnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for MainActivity
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val vpnRepository: VpnRepository
) : ViewModel() {

    private val _vpnState = MutableLiveData<Boolean>()
    val vpnState: LiveData<Boolean> = _vpnState

    private val _vpnError = MutableLiveData<String?>()
    val vpnError: LiveData<String?> = _vpnError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadVpnState()
    }

    private fun loadVpnState() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val state = vpnRepository.getVpnState()
                _vpnState.value = state
            } catch (e: Exception) {
                Timber.e(e, "Failed to load VPN state")
                _vpnError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleVpn() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentState = _vpnState.value ?: false
                
                if (currentState) {
                    vpnRepository.disconnectVpn()
                } else {
                    vpnRepository.connectVpn()
                }
                
                // Update UI after successful operation
                _vpnState.value = !currentState
                _vpnError.value = null
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle VPN")
                _vpnError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _vpnError.value = null
    }
}
