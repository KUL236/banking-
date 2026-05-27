package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.MainDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BankingViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BankPulse AI", appName)
  }

  @Test
  fun `verify main dashboard renders without crashing`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = BankingViewModel(application)
    
    composeTestRule.setContent {
      MyApplicationTheme {
        MainDashboard(viewModel = viewModel)
      }
    }
    
    // Ensure root is displayed to confirm rendering succeeded
    composeTestRule.onRoot().assertExists()
  }
}

