# Privacy policy

Effective October 26, 2026

Snout is a mobile client for PostHog, developed by Igor Kurek (NightCode) and published as open source at
[github.com/ikurek/snout](https://github.com/ikurek/snout). This policy covers the Snout apps for Android and iOS.

## The short version

Snout doesn't collect, store or share any of your data. There is no Snout server, and the app contains no analytics,
advertising, crash reporting or tracking of any kind. The app talks only to the PostHog instance you connect it to.

## What the app handles, and where it goes

- **Your PostHog instance URL and personal API key.** You enter them to connect. The key is stored encrypted on your
  device (in the iOS Keychain or with the Android Keystore) and is sent only to the instance you entered, to
  authenticate your requests. The instance URL is stored on your device.
- **Your PostHog data.** Analytics, dashboards, insights, feature flags, errors, logs, experiments, annotations,
  the activity log, your account details and your organizations' logos are requested from your instance and shown in
  the app. Apart from your account, organizations and projects, which are kept on the device so the app opens
  quickly, this data is held only in memory while you use it; images may be kept in the app's cache.
- **Changes you make.** When you toggle a feature flag, resolve or suppress an error, add an annotation or favorite an
  insight, the app sends that change to your instance.
- **Your preferences.** The selected project, theme and layout settings are stored on your device.

All of this stays on your device or travels directly between your device and your PostHog instance over HTTPS. The
developer of Snout never receives any of it. How your instance handles the data it holds is governed by your
agreement with PostHog, or by whoever operates your instance if it is self-hosted.

Links you choose to open in PostHog or on other websites open in your browser and are subject to those sites'
policies.

## Removing your data

Logging out in Settings deletes the API key, your account and your projects from the device. Uninstalling the app
removes everything it stored. To revoke access entirely, delete the personal API key in your PostHog settings.

## Children

Snout is a tool for people who operate PostHog and isn't directed at children.

## Changes

Changes to this policy are published in this file, and its history is public in the repository. The effective date
above shows when it last changed.

## Contact

Questions about this policy can be asked by opening an issue at
[github.com/ikurek/snout/issues](https://github.com/ikurek/snout/issues).
