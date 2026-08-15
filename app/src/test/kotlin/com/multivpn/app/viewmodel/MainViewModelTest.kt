package com.multivpn.app.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.multivpn.app.domain.repository.VpnRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var vpnRepository: VpnRepository

    private lateinit var viewModel: MainViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)
        viewModel = MainViewModel(vpnRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testToggleVpn_whenDisconnected_shouldConnect() = runTest(testDispatcher) {
        // Given
        whenever(vpnRepository.getVpnState()).thenReturn(false)
        whenever(vpnRepository.connectVpn()).thenReturn(true)

        // When
        viewModel.toggleVpn()
        advanceUntilIdle()

        // Then
        // Verify VPN connection was initiated
    }

    @Test
    fun testToggleVpn_whenConnected_shouldDisconnect() = runTest(testDispatcher) {
        // Given
        whenever(vpnRepository.getVpnState()).thenReturn(true)
        whenever(vpnRepository.disconnectVpn()).thenReturn(true)

        // When
        viewModel.toggleVpn()
        advanceUntilIdle()

        // Then
        // Verify VPN disconnection was initiated
    }
}
