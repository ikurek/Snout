# Privacy policy

Effective October 27, 2026

Snout is a mobile client for PostHog, developed by Igor Kurek (NightCode) and published as open source at
[github.com/ikurek/snout](https://github.com/ikurek/snout). This policy covers the Snout apps for Android and iOS.

## The short version

Your PostHog data never leaves the path between your device and your PostHog instance: there is no Snout server,
and nothing you see in the app is sent anywhere else. To learn how the app is used and fix what breaks, the app
sends anonymous usage and error reports to Snout's own PostHog project. They never contain your PostHog data, and you
can switch each kind off in **Settings → Privacy**. The app contains no advertising and no tracking across apps or
websites.

## Your PostHog data

- **Your PostHog instance URL and personal API key.** You enter them to connect. The key is stored encrypted on your
  device (in the iOS Keychain or with the Android Keystore) and is sent only to the instance you entered, to
  authenticate your requests. The instance URL is stored on your device.
- **What you look at.** Analytics, dashboards, insights, feature flags, errors, logs, experiments, annotations,
  the activity log, your account details and your organizations' logos are requested from your instance and shown in
  the app. Apart from your account, organizations and projects, which are kept on the device so the app opens
  quickly, this data is held only in memory while you use it; images may be kept in the app's cache.
- **Changes you make.** When you toggle a feature flag, resolve or suppress an error, add an annotation or favorite an
  insight, the app sends that change to your instance.
- **Your preferences.** The selected project, theme, layout and privacy settings are stored on your device.

All of this stays on your device or travels directly between your device and your PostHog instance over HTTPS. The
developer of Snout never receives any of it. How your instance handles the data it holds is governed by your
agreement with PostHog, or by whoever operates your instance if it is self-hosted.

## Usage and error reports

Store versions of the app send two kinds of reports to a PostHog project run by the developer of Snout, over HTTPS:

- **Usage**: which screens of the app you open (by kind, such as "insight detail"), and what you do with its
  features, as kinds, counts and switches: for example that a feature flag was switched off, an insight of the funnel
  kind was opened, the theme was set to dark, or onboarding finished on PostHog Cloud EU (cloud US, cloud EU or
  self-hosted, never the address), and where a project was picked (onboarding, Home or Settings). Also when the
  app opens and closes.
- **Error reports**: crashes and unexpected errors, as the type of error, where in the app's code it happened and,
  for a failed request, which kind of request it was with every id removed. Error messages are removed before
  sending, since they can quote what your instance sent.

Each report also carries what PostHog's SDK attaches to describe the app and device: the app's version and build;
the operating system and its version; the device's manufacturer, model, type and name (on recent iOS versions a
generic name such as "iPhone"); its screen size; whether it is an emulator or the app was sideloaded; the language and
time zone settings; on Android, the mobile network operator's name and the system's user agent string; on iOS,
whether the device is on Wi-Fi or a mobile network; and a random identifier created on the device, so reports from
the same installation can be counted together.

Reports never contain your PostHog data: no names, keys, URLs, ids, queries or content from your instance, and not
your account. They aren't linked to you: there is no account with Snout and no person profile, and the identifier is
random and can't be traced back to you.

**Your choice.** Reporting is on until you switch it off: **Settings → Privacy** has a switch for usage and one for
error reports, and turning one off stops it at once. We rely on our legitimate interest in keeping the app working
and improving it; switching reports off is how you object, and you can do so at any time. Builds made from the
source code, including debug builds and builds from other app stores that don't add our PostHog key, send no
reports at all.

**Who processes them.** The reports are stored and processed for us by PostHog Inc. in PostHog Cloud, under its data
processing agreement. We use them only to understand how the app is used and to fix errors. We don't sell them,
share them with anyone else or use them for advertising, and we keep them only as long as they help with that.

## Removing your data

Logging out in Settings deletes the API key, your account and your projects from the device. Uninstalling the app
removes everything it stored, including the random report identifier. To revoke the app's access to your PostHog,
delete the personal API key in your PostHog settings. To ask about the reports from your installation, open an
issue (see Contact); since they aren't linked to you, we can only find them with the identifier, which uninstalling
removes.

## Children

Snout is a tool for people who operate PostHog and isn't directed at children.

## Changes

Changes to this policy are published in this file, and its history is public in the repository. The effective date
above shows when it last changed.

## Contact

Questions about this policy can be asked by opening an issue at
[github.com/ikurek/snout/issues](https://github.com/ikurek/snout/issues).
