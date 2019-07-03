# Flagly APIs

## 1. General Information

Flagly APIs are RESTful. They consume and produce Json data.

All successful responses will have `200 OK` status unless explicitly mentioned.

`X-Request-Id` header is used to track requests. If this header is included in the request, the same value will be returned in the response so requests and responses can be matched. If request does not include a request id, a new one will be generated in the response in the form of a UUID.

### Errors

All handled errors return an error Json in following format with an HTTP status same as `code` field. `causeMessage` might not exists for all errors.

```json
{
  "code": 400,
  "message": "An error message",
  "causeMessage": "Details about the cause of the error"
}
```

### Account Authorization

Some requests that work on behalf of an account require account authorization. They expect `Authorization` header containing a bearer token that belongs to an account's session.

They are generated when registering a new account or logging into an existing account. See [Account APIs](#2-account-apis) for details.

Failing to provide a valid token will result in a `401 Unauthorized` error response.

### Application Authorization

Some requests that work on behalf of an application (e.g. SDK requests) require application authorization. They expect `Authorization` header containing a bearer token that belongs to an application.

They are generated when an application is created. See [SDK APIs](#5-sdk-apis) for details.

Failing to provide a valid token will result in a `401 Unauthorized` error response.

## 2. Account APIs

They are about managing Flagly accounts. They are used by [Flagly Dashboard](https://github.com/flaglyco/flagly-dashboard).

### 2.1. Registering a New Account

Registers a new account with given details. It does not require authorization.

#### Example Request

All fields are required.

```
POST /accounts/register

{
    "email": "john@doe.com",
    "name": "John Doe",
    "password": "Pass1234"
}
```

#### Example Response

A successful response will have `201 Created` status and include `X-Session-Token` header containing an active session token for the account that's just been created. You can use it for [account authorization](#account-authorization).

```
201 Created
X-Request-Id: some-request-id
X-Session-Token: some-session-token

{
    "id": "d4c464a5-db61-4255-80c0-6e48aea4c578",
    "email": "john@doe.com",
    "name": "John Doe",
    "createdAt": "2019-07-03T16:37:10+03:00",
    "updatedAt": "2019-07-03T16:37:10+03:00"
}
```

#### Possible Errors

| What         | When                          |
| ------------ | ----------------------------- |
| Already Used | Email address is already used |

### 2.2. Logging In to a Existing Account

Logs in an existing account with given credentials. It does not require authorization.

#### Example Request

All fields are required.

```
POST /accounts/login

{
    "email": "john@doe.com",
    "password": "Pass1234"
}
```

#### Example Response

A successful response will include `X-Session-Token` header containing an active session token for the account that's just been logged in. You can use it for [account authorization](#account-authorization).

```
200 OK
X-Request-Id: some-request-id
X-Session-Token: some-session-token

{
    "id": "d4c464a5-db61-4255-80c0-6e48aea4c578",
    "email": "john@doe.com",
    "name": "John Doe",
    "createdAt": "2019-07-03T16:37:10+03:00",
    "updatedAt": "2019-07-03T16:37:10+03:00"
}
```

#### Possible Errors

| What          | When                         |
| ------------- | ---------------------------- |
| Authorization | Email or password is invalid |

### 2.3. Logging Out of an Account

Logs out an already logged in account. It requires [account authorization](#account-authorization).

#### Example Request

All fields are required.

```
POST /accounts/logout
Authorization: Bearer some-session-token
```

#### Example Response

```
200 OK
X-Request-Id: some-request-id
```


## 3. Application APIs

TODO

## 4. Flag APIs

TODO

## 5. SDK APIs

TODO