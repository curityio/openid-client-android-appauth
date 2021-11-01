# Curity Android AppAuth Code Example

[![Quality](https://img.shields.io/badge/quality-demo-red)](https://curity.io/resources/code-examples/status/)
[![Availability](https://img.shields.io/badge/availability-source-blue)](https://curity.io/resources/code-examples/status/)

Demonstrates how to implement an OpenID Connect mobile client using AppAuth libraries.

## Code Example Article

The [Tutorial Walkthrough](https://curity.io/resources/learn/kotlin-android-appauth) explains the complete configuration and behavior.

### Prerequisites

- Sign in to the [Curity Developer Portal](https://developer.curity.io/) with your Github account.
- You can get a [Free Community Edition License](https://curity.io/product/community/) if you are new to the Curity Identity Server.
- Then copy your `license.json` file into the root folder.
- Also ensure that Docker Desktop and [ngrok](https://ngrok.com/docs) are installed.

## Quick Start

Deploy the Curity Identity Server via Docker with settings preconfigured for mobile OAuth.\
An ngrok tunnel enables mobile connectivity to the Identity Server's endpoints.

```bash
./start-idsvr.sh
```

In Android Studio run the app by opening the root folder.\
When prompted to sign in, do so as the following preconfigured user account:

- User: `demouser`
- Password: `Password1`

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

## Teardown

Run the following commands to free Docker resources:

```bash
./stop-idsvr.sh
```

## More Information

Please visit [https://curity.io](https://curity.io) for more information about the Curity Identity Server.