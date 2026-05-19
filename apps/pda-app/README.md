# Warehouse PDA App

Flutter Android app for scan-based field workflows.

## Current scope

The first MVP focuses on receiving and putaway work performed by field workers.

- Worker and PDA device context
- Receiving task queue
- Receiving number, SKU, and location scan input
- Inspection quantity confirmation
- Recommended location putaway
- Scan event log
- Offline queue status mock

## Local run

```bash
flutter pub get
flutter run
```

For fast UI checks on a Mac, use Chrome if the Flutter web target is available.

```bash
flutter run -d chrome
```

## Release artifact

The expected Android artifact is an APK or AAB.

```bash
flutter build apk --debug
flutter build apk --release
flutter build appbundle --release
```

Expected paths:

```text
build/app/outputs/flutter-apk/*.apk
build/app/outputs/bundle/release/*.aab
```
