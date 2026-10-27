# Security policy

Snout holds a PostHog personal API key, so security reports matter to us. Thanks for taking the time to send one.

## Reporting a vulnerability

Please don't open a public issue. Report it privately through GitHub instead:
[Security → Report a vulnerability](https://github.com/ikurek/snout/security/advisories/new).

Include what you found, how to reproduce it, the platform and app version, and the impact you see. We'll
acknowledge the report within a week, keep you posted while we fix it, and credit you in the advisory unless you'd
rather stay anonymous.

## Supported versions

Fixes go into the latest release only.

## Scope

In scope: how the app stores and sends the API key and account (`shared/storage/securestore`), the network layer,
incoming links and the iOS share extension, the usage and error reports (`shared/telemetry`), in particular anything
in them that could identify the person or carry their PostHog data, and anything else that sends data somewhere
other than the configured PostHog instance.

Out of scope: vulnerabilities in PostHog itself; report those to [PostHog](https://github.com/PostHog/posthog/security).
