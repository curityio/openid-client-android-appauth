# openid-client-android-appauth

[![Quality](https://img.shields.io/badge/quality-demo-red)](https://curity.io/resources/code-examples/status/)
[![Availability](https://img.shields.io/badge/availability-source-blue)](https://curity.io/resources/code-examples/status/)

# Curity Android AppAuth Code Example

[![Quality](https://img.shields.io/badge/quality-demo-red)](https://curity.io/resources/code-examples/status/)
[![Availability](https://img.shields.io/badge/availability-source-blue)](https://curity.io/resources/code-examples/status/)

Demonstrates how to implement an OpenID Connect mobile client using AppAuth libraries.

## Overview

A simple mobile app that demonstrates OAuth lifecycle events, starting with an `Unauthenticated View`:

![Unauthenticated View](doc/android-unauthenticated-view.png)

Once authenticated the `Authenticated View` show how to work with tokens and sign out:

![Authenticated View](doc/android-authenticated-view.png)

The sample also demonstrates handling of AppAuth errors to ensure a reliable app.

## Security

AppAuth classes are used to perform the following security related operations:

* Dynamic Client Registration
* Logins and Logouts via a Chrome Custom Tab
* Working with Access Tokens and Token Refresh

## Tutorial

See the [Curity Android AppAuth Article](https://curity.io/resources/learn/kotlin-android-appauth/) for full details on how to run the app.

## More Information

Please visit [https://curity.io](https://curity.io) for more information about the Curity Identity Server.