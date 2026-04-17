"""
Example: List Scrolling and Selection

Demonstrates scrolling through a list and selecting specific items.
"""

from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy


def test_scroll_to_item():
    """Scroll to specific item in list and tap it."""
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "Espresso"
    options.app = "/path/to/your/app.apk"
    options.device_name = "Android Emulator"

    driver = webdriver.Remote("http://localhost:4723", options=options)

    try:
        # Navigate to list screen
        list_tab = driver.find_element(by=AppiumBy.ACCESSIBILITY_ID, value="List Tab")
        list_tab.click()

        # Scroll to item using UiScrollable (Android-specific)
        target_item = driver.find_element(
            by=AppiumBy.ANDROID_UIAUTOMATOR,
            value='new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text("Item 50"))'
        )

        # Tap the item
        target_item.click()

        # Verify detail screen
        detail_title = driver.find_element(by=AppiumBy.ID, value="detail_title")
        assert "Item 50" in detail_title.text

        print("✅ Scroll and select test passed")

    finally:
        driver.quit()


def test_pull_to_refresh():
    """Test pull-to-refresh functionality."""
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "Espresso"
    options.app = "/path/to/your/app.apk"
    options.device_name = "Android Emulator"

    driver = webdriver.Remote("http://localhost:4723", options=options)

    try:
        # Get list container
        list_view = driver.find_element(by=AppiumBy.ID, value="recycler_view")

        # Get initial timestamp
        timestamp = driver.find_element(by=AppiumBy.ID, value="last_updated_text")
        initial_time = timestamp.text

        # Perform pull-to-refresh (swipe down using mobile gesture)
        driver.execute_script('mobile: swipeGesture', {
            'elementId': list_view.id,
            'direction': 'down',
            'percent': 0.8
        })

        # Wait a moment for refresh
        import time
        time.sleep(2)

        # Verify timestamp updated
        updated_time = timestamp.text
        assert updated_time != initial_time

        print("✅ Pull-to-refresh test passed")

    finally:
        driver.quit()


if __name__ == "__main__":
    print("Running list scroll tests...")
    print("\n1. Testing scroll to specific item...")
    test_scroll_to_item()

    print("\n2. Testing pull-to-refresh...")
    test_pull_to_refresh()

    print("\n✅ All list tests passed!")
