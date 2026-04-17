#!/usr/bin/env python3
"""
Android Testing Setup Checker

Verifies all prerequisites for Android testing with Appium:
- Appium 3.x installed
- Espresso driver available
- Python client installed
- Device/emulator connected

Usage:
    python check_setup.py
    python check_setup.py --help
"""

import subprocess
import sys
import argparse


def check_command(command, expected_output=None, error_message=""):
    """
    Run command and check if it succeeds.

    Args:
        command: Command to run as list
        expected_output: Optional string to check in output
        error_message: Error message to display if check fails

    Returns:
        (success: bool, output: str)
    """
    try:
        import os
        result = subprocess.run(
            command,
            capture_output=True,
            text=True,
            timeout=10,
            shell=(os.name == 'nt')
        )

        if result.returncode != 0:
            return False, result.stderr

        output = result.stdout + result.stderr

        if expected_output and expected_output not in output:
            return False, f"Expected '{expected_output}' not found in output"

        return True, output

    except FileNotFoundError:
        return False, f"Command not found: {command[0]}"
    except Exception as e:
        return False, str(e)


def main():
    parser = argparse.ArgumentParser(
        description="Check Android testing setup prerequisites"
    )
    parser.add_argument(
        "--verbose", "-v",
        action="store_true",
        help="Show detailed output"
    )
    args = parser.parse_args()

    print("🔍 Checking Android Testing Setup...\n")

    all_checks_passed = True

    # Check 1: Appium installed
    print("1. Checking Appium installation...")
    success, output = check_command(["appium", "--version"])
    if success:
        version = output.strip()
        if version.startswith("3."):
            print(f"   ✅ Appium {version} installed (3.x required)")
        else:
            print(f"   ⚠️  Appium {version} installed (3.x recommended)")
            print("      Espresso driver requires Appium 3.x")
            all_checks_passed = False
    else:
        print(f"   ❌ Appium not found: {output}")
        print("      Install: npm install -g appium")
        all_checks_passed = False

    if args.verbose and success:
        print(f"      Output: {output.strip()}")

    # Check 2: Espresso driver
    print("\n2. Checking Espresso driver...")
    success, output = check_command(["appium", "driver", "list"])
    if success:
        if "espresso" in output.lower():
            print("   ✅ Espresso driver installed")
        else:
            print("   ❌ Espresso driver not found")
            print("      Install: appium driver install espresso")
            all_checks_passed = False
    else:
        print(f"   ❌ Could not list drivers: {output}")
        all_checks_passed = False

    if args.verbose and success:
        # Show only installed drivers
        lines = [l for l in output.split('\n') if '@installed' in l.lower()]
        if lines:
            print("      Installed drivers:")
            for line in lines:
                print(f"        {line.strip()}")

    # Check 3: Python Appium client
    print("\n3. Checking Appium Python client...")
    try:
        from appium import webdriver
        from appium.options.android import UiAutomator2Options
        from appium.webdriver.common.appiumby import AppiumBy
        print("   ✅ Appium-Python-Client installed")
    except ImportError as e:
        print(f"   ❌ Appium-Python-Client not installed: {e}")
        print("      Install: pip install Appium-Python-Client")
        all_checks_passed = False

    # Check 4: ADB and device connection
    print("\n4. Checking ADB and device connection...")
    success, output = check_command(["adb", "version"])
    if success:
        # Extract version from output
        version_line = [l for l in output.split('\n') if 'Android Debug Bridge' in l]
        if version_line:
            print(f"   ✅ {version_line[0].strip()}")
        else:
            print("   ✅ ADB installed")
    else:
        print(f"   ❌ ADB not found: {output}")
        print("      Ensure Android SDK Platform Tools are installed")
        print("      Set ANDROID_HOME environment variable")
        all_checks_passed = False

    # Check 4b: Connected devices
    if success:
        print("\n5. Checking connected devices...")
        success, output = check_command(["adb", "devices"])
        if success:
            lines = output.strip().split('\n')[1:]  # Skip "List of devices attached"
            devices = [l for l in lines if l.strip() and 'device' in l]

            if devices:
                print(f"   ✅ {len(devices)} device(s) connected:")
                for device in devices:
                    print(f"      • {device.strip()}")
            else:
                print("   ⚠️  No devices connected")
                print("      Connect a device or start an emulator")
                print("      Check with: adb devices")
                all_checks_passed = False
        else:
            print(f"   ❌ Could not list devices: {output}")
            all_checks_passed = False

    # Check 5: Java (required for Espresso)
    print("\n6. Checking Java installation...")
    success, output = check_command(["java", "-version"])
    if success:
        # Java version is in stderr
        version_line = output.split('\n')[0]
        if 'version' in version_line.lower():
            print(f"   ✅ {version_line.strip()}")
            # Check if Java 11+
            if '"11.' in version_line or '"1.8' not in version_line:
                print("      (Java 11+ required for Espresso driver)")
            else:
                print("      ⚠️  Java 11+ recommended for Espresso driver")
                all_checks_passed = False
        else:
            print("   ✅ Java installed")
    else:
        print(f"   ❌ Java not found: {output}")
        print("      Install Java JDK 11 or newer")
        print("      Set JAVA_HOME environment variable")
        all_checks_passed = False

    # Summary
    print("\n" + "="*50)
    if all_checks_passed:
        print("✅ All checks passed! Ready for Android testing.")
        print("\nNext steps:")
        print("  1. Start Appium server: appium")
        print("  2. Write test script using examples/")
        print("  3. Run test: python your_test.py")
    else:
        print("❌ Some checks failed. Please fix issues above.")
        print("\nQuick fixes:")
        print("  • Install Appium 3: npm install -g appium")
        print("  • Install Espresso driver: appium driver install espresso")
        print("  • Install Python client: pip install Appium-Python-Client")
        print("  • Connect device: adb devices")
        sys.exit(1)


if __name__ == "__main__":
    main()
