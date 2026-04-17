---
name: android-app-testing
description: Toolkit for testing Android applications using Appium with Espresso driver. Supports automated UI testing, gesture interactions, element discovery, and screenshot capture. Use when testing native Android apps (.apk files), verifying mobile UI functionality, automating gestures (swipe, scroll, tap), or running blackbox tests on Android devices/emulators.
---

# Android App Testing

To test Android applications, write Python scripts using Appium with the Espresso driver for fast, reliable automation.

**Key Advantage:** Espresso driver is **2x faster** than UI Automator 2 with automatic UI synchronization - no manual waits needed.

**Helper Scripts Available**:
- `scripts/check_setup.py` - Verifies Appium server, drivers, and device connectivity

**Always run scripts with `--help` first** to see usage. These scripts handle common setup tasks without cluttering your context window.

## Decision Tree: Choosing Your Approach

```
User task → Do you have app source code?
    ├─ Yes → Use Espresso driver (FAST, automatic waits)
    │         1. Start Appium server: appium
    │         2. Write test with Espresso automation
    │         3. Reference examples/ for patterns
    │
    └─ No (black-box testing) → Use UI Automator 2 driver
        1. Start Appium server: appium
        2. Write test with UiAutomator2 automation
        3. Expect slower execution, add explicit waits
```

**Recommendation:** Use Espresso driver whenever possible (requires app source code but much faster).

## Quick Start

### Environment Setup

Before running tests, create a virtual environment and install dependencies:
```bash
# Create virtual environment
python -m venv .venv

# Activate (Windows)
.\.venv\Scripts\activate

# Install requirements
pip install -r android-app-testing/requirements.txt
```

### Prerequisites Check

Run the setup checker to verify your environment:
```bash
python android-app-testing/scripts/check_setup.py
```

This verifies:
- Appium 3.x installed
- Espresso driver available
- Python client installed
- Device/emulator connected

### Writing Your First Test

```python
from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy

# Configure for Espresso (fast!)
options = UiAutomator2Options()
options.platform_name = "Android"
options.automation_name = "Espresso"  # Use Espresso for speed
options.app = "/path/to/app.apk"
options.device_name = "Android Emulator"
options.auto_grant_permissions = True

# Start session
driver = webdriver.Remote("http://localhost:4723", options=options)

# Espresso automatically waits - just find and interact
driver.find_element(by=AppiumBy.ID, value="username").send_keys("testuser")
driver.find_element(by=AppiumBy.ID, value="login_button").click()

# Verify result
assert driver.find_element(by=AppiumBy.ID, value="home_screen").is_displayed()

driver.quit()
```

## Comprehensive Documentation

For detailed guidance, see the project's comprehensive testing docs:

**Location:** `docs/research/android_testing/`

**Files:**
- `README.md` - Setup, installation, prerequisites, and key differences
- `common_actions.py` - **Reusable action library** - Copy these instead of rewriting!
- `example_tests.py` - Full test scenarios (login, lists, forms, gestures, navigation)
- `ai_agent_guide.md` - Specific guidance for QA agents in multi-agent teams

**When to read:**
- Setting up for first time → Read `README.md`
- Writing tests → Import functions from `common_actions.py`, reference `example_tests.py`
- QA agent workflow → Read `ai_agent_guide.md`

## Examples

The `examples/` directory contains practical patterns:

- `login_test.py` - Login flow with success/failure cases
- `list_scroll.py` - Scrolling through lists and selecting items
- `gesture_test.py` - Swipe, long press, and other gestures
- `element_discovery.py` - Finding buttons, inputs, and text on screen

**Pattern:** Copy an example closest to your test case, then modify.

## DO's and DON'Ts

### ✅ DO

1. **Use Espresso driver when possible** (2x faster, automatic waits):
   ```python
   options.automation_name = "Espresso"  # FAST ✅
   ```

2. **Prefer ID locators** (fastest):
   ```python
   driver.find_element(by=AppiumBy.ID, value="button_login")  # FAST ✅
   ```

3. **Copy patterns from `common_actions.py`** instead of rewriting:
   ```python
   from common_actions import tap_element, input_text
   ```

4. **Let Espresso handle waits** (no manual `time.sleep()` needed)

5. **Take screenshots on failures** for debugging:
   ```python
   driver.save_screenshot("/tmp/test_failure.png")
   ```

### ❌ DON'T

1. **Don't use XPath** unless absolutely necessary (very slow):
   ```python
   # AVOID ❌
   driver.find_element(by=AppiumBy.XPATH, value="//button[@text='Login']")

   # PREFER ✅
   driver.find_element(by=AppiumBy.ID, value="login_button")
   ```

2. **Don't use TouchAction class** (removed in Appium 2.0+):
   ```python
   # WRONG ❌ (Appium 1.x)
   from appium.webdriver.common.touch_action import TouchAction

   # CORRECT ✅ (Appium 2.0+)
   driver.execute_script('mobile: clickGesture', {'x': 100, 'y': 200})
   ```

3. **Don't add explicit waits with Espresso** (automatic waits built-in)

4. **Don't forget cleanup** (always close session in `finally` block)

## Common Pitfalls

❌ **Don't** start tests before Appium server is running
✅ **Do** start Appium server first: `appium`

❌ **Don't** forget to install Espresso driver
✅ **Do** install driver: `appium driver install espresso`

❌ **Don't** test before device is connected
✅ **Do** check device: `adb devices`

## Reconnaissance Pattern for Unknown Apps

When testing unfamiliar apps:

1. **Launch and wait**:
   ```python
   driver.find_element(by=AppiumBy.ID, value="com.app:id/main")
   time.sleep(2)  # Let app fully load
   ```

2. **Take screenshot**:
   ```python
   driver.save_screenshot('/tmp/screen.png')
   ```

3. **Discover elements**:
   ```python
   # List all buttons
   buttons = driver.find_elements(by=AppiumBy.CLASS_NAME, value="android.widget.Button")
   for btn in buttons:
       print(f"Button: {btn.text} (ID: {btn.get_attribute('resource-id')})")
   ```

4. **Identify IDs** from output and use in tests

See `examples/element_discovery.py` for complete pattern.

## Integration with Multi-Agent Teams

For QA agents in multi-agent workflows, see `ai_agent_guide.md` in comprehensive docs for:
- Test writing workflow
- Reporting results to Scrum Master
- Screenshot capture on failures
- WHITEBOARD updates

## Best Practices

- Use `sync` Appium client (not async)
- Always close driver in `finally` block
- Prefer resource IDs over text or XPath
- Use Espresso driver for speed (requires app source)
- Fall back to UI Automator 2 for black-box testing
- Take screenshots for debugging failed tests

## Resources

### Comprehensive Documentation
See `docs/research/android_testing/` for:
- Full setup guide with prerequisites
- 40+ reusable action functions
- 11 complete test examples
- QA agent workflow guide

### Key Research
Based on 2026-01-06 Android automation research:
- Appium with Espresso driver is fastest Python-compatible option
- Espresso: 2x faster than UI Automator 2, automatic sync
- UI Automator 2: Black-box testing, no source code needed
- Playwright: Does NOT support native Android apps
