package com.multivpn.app.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.multivpn.app.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        // Set up test environment
    }

    @Test
    fun testVpnToggleButton_isDisplayed() {
        onView(withId(R.id.vpn_toggle_button))
            .perform(click())
    }

    @Test
    fun testSettingsButton_isDisplayed() {
        onView(withId(R.id.settings_button))
            .perform(click())
    }
}
