package com.vylexai.app.ui.screens

import app.cash.turbine.test
import com.vylexai.app.data.auth.AuthRepository
import com.vylexai.app.data.net.VylexException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val repo: AuthRepository = mockk()
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `canSubmit requires valid email and 8+ char password`() {
        every { repo.isAuthenticated() } returns false
        val vm = AuthViewModel(repo)
        assertFalse(vm.state.value.canSubmit)
        vm.onEmailChange("me@vylexai.com")
        assertFalse(vm.state.value.canSubmit)
        vm.onPasswordChange("short")
        assertFalse(vm.state.value.canSubmit)
        vm.onPasswordChange("password12345")
        assertTrue(vm.state.value.canSubmit)
    }

    @Test fun `mode toggle flips login -- register`() {
        every { repo.isAuthenticated() } returns false
        val vm = AuthViewModel(repo)
        assertEquals(AuthMode.Login, vm.state.value.mode)
        vm.onModeToggle()
        assertEquals(AuthMode.Register, vm.state.value.mode)
        vm.onModeToggle()
        assertEquals(AuthMode.Login, vm.state.value.mode)
    }

    @Test fun `successful login emits Authenticated event`() = runTest(dispatcher) {
        every { repo.isAuthenticated() } returns false
        coEvery { repo.login(any(), any()) } just Runs
        val vm = AuthViewModel(repo)
        vm.onEmailChange("me@vylexai.com")
        vm.onPasswordChange("password12345")

        vm.events.test {
            assertNull(awaitItem())
            vm.submit()
            dispatcher.scheduler.advanceUntilIdle()
            assertNotNull(awaitItem())
        }
        coVerify(exactly = 1) { repo.login("me@vylexai.com", "password12345") }
    }

    @Test fun `register failure surfaces typed error message`() = runTest(dispatcher) {
        every { repo.isAuthenticated() } returns false
        coEvery { repo.register(any(), any()) } throws VylexException.BadRequest("email_taken")
        val vm = AuthViewModel(repo)
        vm.onModeToggle()
        vm.onEmailChange("me@vylexai.com")
        vm.onPasswordChange("password12345")
        vm.submit()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("That email is already registered.", vm.state.value.error)
        assertFalse(vm.state.value.submitting)
    }

    @Test fun `network unavailable maps to friendly message`() = runTest(dispatcher) {
        every { repo.isAuthenticated() } returns false
        coEvery { repo.login(any(), any()) } throws VylexException.Unavailable()
        val vm = AuthViewModel(repo)
        vm.onEmailChange("me@vylexai.com")
        vm.onPasswordChange("password12345")
        vm.submit()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.error!!.contains("network"))
    }
}
