"""
Example: Login Flow Test

Demonstrates testing login with both valid and invalid credentials.
"""

from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy


def test_login_success():
    """Test successful login flow."""
    # Configure for Espresso (fast!)
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "Espresso"
    options.app = "/path/to/your/app.apk"  # Replace with actual path
    options.device_name = "Android Emulator"
    options.auto_grant_permissions = True

    driver = webdriver.Remote("http://localhost:4723", options=options)

    try:
        # Find and fill username (Espresso auto-waits for element)
        username_field = driver.find_element(by=AppiumBy.ID, value="username_edit_text")
        username_field.clear()
        username_field.send_keys("testuser@example.com")

        # Find and fill password
        password_field = driver.find_element(by=AppiumBy.ID, value="password_edit_text")
        password_field.clear()
        password_field.send_keys("password123")

        # Hide keyboard (optional)
        try:
            driver.hide_keyboard()
        except:
            pass

        # Tap login button
        login_button = driver.find_element(by=AppiumBy.ID, value="login_button")
        login_button.click()

        # Verify home screen loads (Espresso auto-waits for UI idle)
        home_indicator = driver.find_element(by=AppiumBy.ID, value="home_screen_title")
        assert home_indicator.is_displayed()
        assert "Welcome" in home_indicator.text

        print("✅ Login test passed")

    finally:
        driver.quit()


def test_login_invalid_credentials():
    """Test login with invalid credentials shows error."""
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "Espresso"
    options.app = "/path/to/your/app.apk"
    options.device_name = "Android Emulator"

    driver = webdriver.Remote("http://localhost:4723", options=options)

    try:
        # Enter invalid credentials
        username_field = driver.find_element(by=AppiumBy.ID, value="username_edit_text")
        username_field.send_keys("invalid@example.com")

        password_field = driver.find_element(by=AppiumBy.ID, value="password_edit_text")
        password_field.send_keys("wrongpassword")

        # Tap login
        login_button = driver.find_element(by=AppiumBy.ID, value="login_button")
        login_button.click()

        # Verify error message appears
        error_message = driver.find_element(by=AppiumBy.ID, value="error_text_view")
        assert error_message.is_displayed()
        assert "Invalid credentials" in error_message.text

        print("✅ Invalid credentials test passed")

    finally:
        driver.quit()


if __name__ == "__main__":
    print("Running login tests...")
    print("\n1. Testing successful login...")
    test_login_success()

    print("\n2. Testing invalid credentials...")
    test_login_invalid_credentials()

    print("\n✅ All login tests passed!")
