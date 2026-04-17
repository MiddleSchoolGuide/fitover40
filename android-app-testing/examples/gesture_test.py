"""
Example: Gesture Testing

Demonstrates swipe, long press, and other touch gestures.
Uses Appium 2.0+ mobile commands (NOT TouchAction).
"""

from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy


def test_swipe_gesture():
    """Test swipe gesture through carousel."""
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "Espresso"
    options.app = "/path/to/your/app.apk"
    options.device_name = "Android Emulator"

    driver = webdriver.Remote("http://localhost:4723", options=options)

    try:
        # Navigate to carousel screen
        carousel_tab = driver.find_element(by=AppiumBy.ID, value="carousel_tab")
        carousel_tab.click()

        # Get initial position
        position_indicator = driver.find_element(by=AppiumBy.ID, value="position_text")
        assert "1 / 10" in position_indicator.text

        # Swipe left 3 times using mobile gesture (Appium 2.0+)
        carousel_view = driver.find_element(by=AppiumBy.ID, value="view_pager")
        for _ in range(3):
            driver.execute_script('mobile: swipeGesture', {
                'elementId': carousel_view.id,
                'direction': 'left',
                'percent': 0.75
            })
            import time
            time.sleep(0.5)  # Brief pause between swipes

        # Verify position changed to 4th item
        position_indicator = driver.find_element(by=AppiumBy.ID, value="position_text")
        assert "4 / 10" in position_indicator.text

        print("✅ Swipe gesture test passed")

    finally:
        driver.quit()


def test_long_press():
    """Test long press to open context menu."""
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "Espresso"
    options.app = "/path/to/your/app.apk"
    options.device_name = "Android Emulator"

    driver = webdriver.Remote("http://localhost:4723", options=options)

    try:
        # Find list item
        list_item = driver.find_element(by=AppiumBy.ID, value="list_item_0")

        # Long press (2 seconds) using mobile gesture (Appium 2.0+)
        driver.execute_script('mobile: longClickGesture', {
            'elementId': list_item.id,
            'duration': 2000  # milliseconds
        })

        # Verify context menu appears
        context_menu = driver.find_element(by=AppiumBy.ID, value="context_menu")
        assert context_menu.is_displayed()

        # Select "Delete" option
        delete_option = driver.find_element(by=AppiumBy.ID, value="menu_delete")
        delete_option.click()

        # Verify confirmation dialog
        confirm_dialog = driver.find_element(by=AppiumBy.ID, value="confirm_dialog_title")
        assert "Delete" in confirm_dialog.text

        print("✅ Long press test passed")

    finally:
        driver.quit()


def test_double_tap():
    """Test double tap gesture."""
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "Espresso"
    options.app = "/path/to/your/app.apk"
    options.device_name = "Android Emulator"

    driver = webdriver.Remote("http://localhost:4723", options=options)

    try:
        # Find image element
        image_element = driver.find_element(by=AppiumBy.ID, value="zoom_image")

        # Double tap using mobile gesture (Appium 2.0+)
        driver.execute_script('mobile: doubleClickGesture', {
            'elementId': image_element.id
        })

        # Verify zoom level changed
        zoom_indicator = driver.find_element(by=AppiumBy.ID, value="zoom_level")
        assert zoom_indicator.text == "200%"

        print("✅ Double tap test passed")

    finally:
        driver.quit()


if __name__ == "__main__":
    print("Running gesture tests...")
    print("\n1. Testing swipe gesture...")
    test_swipe_gesture()

    print("\n2. Testing long press...")
    test_long_press()

    print("\n3. Testing double tap...")
    test_double_tap()

    print("\n✅ All gesture tests passed!")
