"""
Example: Element Discovery

Demonstrates discovering buttons, inputs, and other UI elements on screen.
Useful for reconnaissance when testing unfamiliar apps.
"""

from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy


def discover_elements():
    """Discover all interactive elements on current screen."""
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "Espresso"
    options.app = "/path/to/your/app.apk"
    options.device_name = "Android Emulator"

    driver = webdriver.Remote("http://localhost:4723", options=options)

    try:
        # Wait for app to load (Espresso auto-waits, but give extra time)
        import time
        time.sleep(2)

        # Take screenshot for visual reference
        screenshot_path = '/tmp/app_screen.png'
        driver.save_screenshot(screenshot_path)
        print(f"📸 Screenshot saved: {screenshot_path}")

        print("\n" + "="*60)
        print("ELEMENT DISCOVERY REPORT")
        print("="*60)

        # Discover all buttons
        buttons = driver.find_elements(by=AppiumBy.CLASS_NAME, value="android.widget.Button")
        print(f"\n🔘 Found {len(buttons)} buttons:")
        for i, button in enumerate(buttons):
            try:
                text = button.text if button.is_displayed() else "[hidden]"
                resource_id = button.get_attribute('resource-id') or "[no ID]"
                content_desc = button.get_attribute('content-desc') or "[no desc]"
                print(f"  [{i}] Text: '{text}'")
                print(f"      ID: {resource_id}")
                print(f"      Content-desc: {content_desc}")
                print()
            except:
                print(f"  [{i}] [Error reading button properties]")

        # Discover text views
        text_views = driver.find_elements(by=AppiumBy.CLASS_NAME, value="android.widget.TextView")
        print(f"\n📝 Found {len(text_views)} text views (showing first 5):")
        for i, text_view in enumerate(text_views[:5]):
            try:
                text = text_view.text.strip()
                resource_id = text_view.get_attribute('resource-id') or "[no ID]"
                if text:  # Only show non-empty text
                    print(f"  [{i}] '{text}' (ID: {resource_id})")
            except:
                print(f"  [{i}] [Error reading text view]")

        # Discover input fields
        edit_texts = driver.find_elements(by=AppiumBy.CLASS_NAME, value="android.widget.EditText")
        print(f"\n✏️  Found {len(edit_texts)} input fields:")
        for i, edit_text in enumerate(edit_texts):
            try:
                hint = edit_text.get_attribute('hint') or "[no hint]"
                text = edit_text.text or "[empty]"
                resource_id = edit_text.get_attribute('resource-id') or "[no ID]"
                print(f"  [{i}] Hint: '{hint}'")
                print(f"      Value: '{text}'")
                print(f"      ID: {resource_id}")
                print()
            except:
                print(f"  [{i}] [Error reading input field]")

        # Discover clickable elements (any type)
        clickables = driver.find_elements(
            by=AppiumBy.XPATH,
            value="//*[@clickable='true']"
        )
        print(f"\n👆 Found {len(clickables)} clickable elements total")

        # Print current activity for context
        package = driver.current_package
        activity = driver.current_activity
        print(f"\n📱 Current Activity: {package}/{activity}")

        # Get page source (XML hierarchy) - useful for debugging
        page_source = driver.page_source
        xml_path = '/tmp/app_hierarchy.xml'
        with open(xml_path, 'w') as f:
            f.write(page_source)
        print(f"\n🌳 Full UI hierarchy saved: {xml_path}")

        print("\n" + "="*60)
        print("✅ Discovery complete!")
        print("\nNext steps:")
        print("  1. Review screenshot and hierarchy files")
        print("  2. Identify element IDs or content-desc for your test")
        print("  3. Use discovered IDs in test scripts")
        print("="*60)

    finally:
        driver.quit()


if __name__ == "__main__":
    print("Starting element discovery...")
    print("This will connect to the app and discover all UI elements.\n")
    discover_elements()
