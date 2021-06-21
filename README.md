# openid-client-android-appauth

[![Quality](https://img.shields.io/badge/quality-demo-red)](https://curity.io/resources/code-examples/status/)
[![Availability](https://img.shields.io/badge/availability-source-blue)](https://curity.io/resources/code-examples/status/)

Demonstrates how to implement an OpenID Connect mobile client using AppAuth libraries

## AppAuth Integration

AppAuth classes are used to handle lifecycle events in line with [Curity Mobile Best Practices](https://curity.io/resources/learn/oauth-for-mobile-apps-best-practices/):

* Dynamic Client Registration
* Logins via a Chrome Custom Tab
* Working with Access Tokens and managing Token Refresh
* Logouts via End Session Endpoints

The sample also deals with common error conditions

## UI Views

The app uses a single activity and presents one of these views as the main fragment:

- AuthenticationFragment
- UnauthenticationFragment