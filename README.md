# Curity Android AppAuth Code Example

[![Quality](https://img.shields.io/badge/quality-demo-red)](https://curity.io/resources/code-examples/status/)
[![Availability](https://img.shields.io/badge/availability-source-blue)](https://curity.io/resources/code-examples/status/)

Demonstrates how to implement an OpenID Connect mobile client using AppAuth libraries.

## Tutorial Documentation

The [Tutorial Walkthrough](https://curity.io/resources/learn/kotlin-android-appauth) explains the complete configuration and behavior.

## Quick Start

The easiest way to run the code example is via an automated script as explained in the [Mobile Setup Article](https://curity.io/resources/learn/mobile-setup-ngrok):

- Copy a license.json file into the code example root folder
- Edit the `./start-idsvr.sh` script to use either a local Docker URL on an ngrok internet URL
- Run the script to deploy a preconfigured Curity Identity Server via Docker
- Build and run the mobile app from Android Studio
- Sign in with the preconfigured user account `demouser / Password1`
- Run `./stop-idsvr.sh` when you want to free Docker resources

## User Experience

The example mobile app demonstrates OAuth lifecycle events, starting with an `Unauthenticated View`:

![Unauthenticated View](images/android-unauthenticated-view.png)

Once authenticated the `Authenticated View` show how to work with tokens and sign out:

![Authenticated View](images/android-authenticated-view.png)

The example app also demonstrates reliable handling of AppAuth errors.

## Security

AppAuth classes are used to perform the following security related operations accordng to [RFC8252](https://datatracker.ietf.org/doc/html/rfc8252):

* Logins and Logouts via a secure ASWebAuthenticationSession window
* Use of Authorization Code Flow (PKCE)

![Secure Window](images/secure-login-window.png)

## More Information

Please visit [https://curity.io](https://curity.io) for more information about the Curity Identity Server.
