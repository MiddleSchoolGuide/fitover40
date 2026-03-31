# Keystore Generation Instructions

To generate a production keystore for FitOver40, run the following command in your terminal:

```bash
keytool -genkey -v -keystore fitover40.jks -alias fitover40 -keyalg RSA -keysize 2048 -validity 9125
```

### Next Steps:
1.  Copy `local.properties.template` to `local.properties` (if not already existing or update it).
2.  Fill in the `storePassword`, `keyAlias`, and `keyPassword` in `local.properties`.
3.  Ensure `fitover40.jks` is placed in the project root or adjust `storeFile` in `local.properties`.

**CRITICAL: Never share or commit your keystore or `local.properties` files.**
