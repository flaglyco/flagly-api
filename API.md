# Flagly APIs

## 1. General Information

Flagly APIs RESTful. They consume and produce Json data.

All successful responses will have `200 OK` status unless explicitly mentioned.

`X-Request-Id` header is used to track requests. If this header is included in the request, the same value will be returned in the response so requests and responses can be matched. If request does not include a request id, a new one will be generated in the response in the form of a UUID.

### Errors

All handled errors return an error Json in following format with an HTTP status same as `code` field.

```json
{
  "code": 400,
  "message": "An error message",
  "causeMessage": "Details about the cause of the error"
}
```

### Account Authorization

Requests that require account authorization expect `Authorization` header containing a bearer token. This token needs to belong to an account's session. They are generated when registering a new account or logging into an existing account. See [Account APIs](#account-apis) for details.

Failing to provide a valid token will result in a `401 Unauthorized` error response.

### Application Authorization

Requests that require application authorization expect `Authorization` header containing a bearer token. This token needs to belong to an application. They are generated when an application is created. See [Application APIs](#application-apis) for details.

Failing to provide a valid token will result in a `401 Unauthorized` error response.

## 2. Account APIs

They are about managing Flagly accounts. They are used by [Flagly Dashboard](https://github.com/flaglyco/flagly-dashboard).

### 2.1. Registering a New Account

Registers a new account with given details. It does not require authorization.

#### Example Request

All fields are required.

```http
POST /accounts/register HTTP/1.1
Content-Length: 69
Content-Type: application/json
Host: api.flagly.co

{
    "email": "john@doe.com",
    "name": "John Doe",
    "password": "Pass1234"
}
```

#### Example Response

A successful response will have `201 Created` status and include `X-Session-Token` header containing an active session token for the account that's just been created. You can use it for [account authorization](#account-authorization).

```http
HTTP/1.1 201 Created
Content-Length: 166
Content-Type: application/json
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

## 3. Application APIs

TODO

## 4. Flag APIs

TODO

## 5. SDK APIs

TODO