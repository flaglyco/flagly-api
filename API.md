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

To use Flagly, you need an account. Accounts are managed on [Flagly Dashboard](https://github.com/flaglyco/flagly-dashboard).

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
X-Request-Id: {someRequestId}
X-Session-Token: {someAccountToken}

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
X-Request-Id: {someRequestId}
X-Session-Token: {someAccountToken}

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

```
POST /accounts/logout
Authorization: Bearer {someAccountToken}
```

#### Example Response

```
200 OK
X-Request-Id: {someRequestId}
```

## 3. Application APIs

Applications are a way of grouping flags. Each application belongs to an account. An account can have multiple applications. All application requests require [account authorization](#account-authorization).

### 3.1. Creating an Application

Creates a new application with given details for authorized account. It requires [account authorization](#account-authorization).

#### Example Request

All fields are required.

```
POST /applications
Authorization: Bearer {someAccountToken}

{
    "name": "test-application"
}
```

#### Example Response

A successful response will have `201 Created` status. `token` field in response payload will contain the token for the application that's just been created. You can use it for [application authorization](#application-authorization).

```
201 Created
X-Request-Id: {someRequestId}

{
    "id": "c1b69e5b-ce2e-42e0-ab14-745ff7a611df",
    "accountId": "d4c464a5-db61-4255-80c0-6e48aea4c578",
    "name": "test-application",
    "token": "someApplicationToken",
    "createdAt": "2019-07-03T18:44:14+03:00",
    "updatedAt": "2019-07-03T18:44:14+03:00"
}
```

#### Possible Errors

| What         | When                                              |
| ------------ | ------------------------------------------------- |
| Already Used | Application name is already used for this account |

### 3.2. Listing Applications

Finds one or lists all applications for authorized account depending on `name` query parameter. It requires [account authorization](#account-authorization).

#### Example Request 1

When `name` query parameter is provided

```
GET /applications?name={applicationName}
Authorization: Bearer {someAccountToken}
```

#### Example Response 1

Response payload will contain a Json object of the application with given name.

```
200 OK
X-Request-Id: {someRequestId}

{
    "id": "c1b69e5b-ce2e-42e0-ab14-745ff7a611df",
    "accountId": "d4c464a5-db61-4255-80c0-6e48aea4c578",
    "name": "test-application",
    "token": "someApplicationToken",
    "createdAt": "2019-07-03T18:44:14+03:00",
    "updatedAt": "2019-07-03T18:44:14+03:00"
}
```

#### Example Request 2

When `name` query parameter is not provided

```
GET /applications
Authorization: Bearer {someAccountToken}
```

#### Example Response 2

Response payload will contain a Json array of all the applications.

```
200 OK
X-Request-Id: {someRequestId}

[
  {
      "id": "c1b69e5b-ce2e-42e0-ab14-745ff7a611df",
      "accountId": "d4c464a5-db61-4255-80c0-6e48aea4c578",
      "name": "test-application",
      "token": "someApplicationToken",
      "createdAt": "2019-07-03T18:44:14+03:00",
      "updatedAt": "2019-07-03T18:44:14+03:00"
  }
]
```

#### Possible Errors

| What      | When                                                        |
| --------- | ----------------------------------------------------------- |
| Not Found | Application with given name does not exist for this account |

### 3.3. Getting an Application

Gets an application for authorized account. It requires [account authorization](#account-authorization).

#### Example Request

```
GET /applications/{applicationId}
Authorization: Bearer {someAccountToken}
```

#### Example Response

Response payload will contain a Json object of the application.

```
200 OK
X-Request-Id: {someRequestId}

{
    "id": "c1b69e5b-ce2e-42e0-ab14-745ff7a611df",
    "accountId": "d4c464a5-db61-4255-80c0-6e48aea4c578",
    "name": "test-application",
    "token": "someApplicationToken",
    "createdAt": "2019-07-03T18:44:14+03:00",
    "updatedAt": "2019-07-03T18:44:14+03:00"
}
```

#### Possible Errors

| What      | When                                        |
| --------- | ------------------------------------------- |
| Not Found | Application does not exist for this account |

### 3.4. Updating an Application

Updates an application with given details for authorized account. It requires [account authorization](#account-authorization).

#### Example Request

All fields are required.

```
PUT /applications/{applicationId}
Authorization: Bearer {someAccountToken}

{
    "name": "test-application"
}
```

#### Example Response

Response payload will contain a Json object of the application.

```
200 OK
X-Request-Id: {someRequestId}

{
    "id": "c1b69e5b-ce2e-42e0-ab14-745ff7a611df",
    "accountId": "d4c464a5-db61-4255-80c0-6e48aea4c578",
    "name": "test-application",
    "token": "someApplicationToken",
    "createdAt": "2019-07-03T18:44:14+03:00",
    "updatedAt": "2019-07-03T18:44:14+03:00"
}
```

#### Possible Errors

| What         | When                                              |
| ------------ | ------------------------------------------------- |
| Already Used | Application name is already used for this account |

### 3.5. Deleting an Application

Deletes an application for authorized account. It requires [account authorization](#account-authorization).

#### Example Request

```
DELETE /applications/{applicationId}
Authorization: Bearer {someAccountToken}
```

#### Example Response

```
200 OK
X-Request-Id: {someRequestId}
```

#### Possible Errors

| What   | When                                                    |
| ------ | ------------------------------------------------------- |
| In Use | Application is still in use (by flags) for this account |

## 4. Flag APIs

A flag is a switch and it can have `true` or `false` as value. Each flag belongs to an application. An application can have multiple flags. Working with a flag requires the id of the application in the request path. All flag requests require [account authorization](#account-authorization).

### 4.1. Creating a Flag

Creates a new flag with given details for authorized account. It requires [account authorization](#account-authorization).

#### Example Request

All fields except for `description` are required.

```
POST /applications/{applicationId}/flags
Authorization: Bearer {someAccountToken}

{
    "name": "test-flag",
    "description": "Test Flag",
    "value": true
}
```

#### Example Response

A successful response will have `201 Created` status.

```
201 Created
X-Request-Id: {someRequestId}

{
    "id": "1354b6cf-f23d-4a93-9e54-bfb9eafd3d75",
    "applicationId": "45f502c9-25c1-4a09-a7a3-f9d3768ffacf",
    "name": "test-flag",
    "description": "Test Flag",
    "value": true,
    "createdAt": "2019-06-27T17:34:25+03:00",
    "updatedAt": "2019-06-27T17:34:25+03:00"
}
```

#### Possible Errors

| What                | When                                           |
| ------------------- | ---------------------------------------------- |
| Invalid Application | Application does not exist for this account    |
| Already Used        | Flag name is already used for this application |

### 4.2. Listing Flags

Finds one or lists all flags for authorized account depending on `name` query parameter. It requires [account authorization](#account-authorization).

#### Example Request 1

When `name` query parameter is provided

```
GET /applications/{applicationId}/flags?name={flagName}
Authorization: Bearer {someAccountToken}
```

#### Example Response 1

Response payload will contain a Json object of the flag with given name.

```
200 OK
X-Request-Id: {someRequestId}

{
    "id": "1354b6cf-f23d-4a93-9e54-bfb9eafd3d75",
    "applicationId": "45f502c9-25c1-4a09-a7a3-f9d3768ffacf",
    "name": "test-flag",
    "description": "Test Flag",
    "value": true,
    "createdAt": "2019-06-27T17:34:25+03:00",
    "updatedAt": "2019-06-27T17:34:25+03:00"
}
```

#### Example Request 2

When `name` query parameter is not provided

```
GET /applications/{applicationId}/flags
Authorization: Bearer {someAccountToken}
```

#### Example Response 2

Response payload will contain a Json array of all the flags.

```
200 OK
X-Request-Id: {someRequestId}

[
  {
      "id": "1354b6cf-f23d-4a93-9e54-bfb9eafd3d75",
      "applicationId": "45f502c9-25c1-4a09-a7a3-f9d3768ffacf",
      "name": "test-flag",
      "description": "Test Flag",
      "value": true,
      "createdAt": "2019-06-27T17:34:25+03:00",
      "updatedAt": "2019-06-27T17:34:25+03:00"
  }
]
```

#### Possible Errors

| What      | When                                                     |
| --------- | -------------------------------------------------------- |
| Not Found | Flag with given name does not exist for this application |

### 4.3. Getting a Flag

Gets a flag for authorized account. It requires [account authorization](#account-authorization).

#### Example Request

```
GET /applications/{applicationId}/flags/{flagId}
Authorization: Bearer {someAccountToken}
```

#### Example Response

Response payload will contain a Json object of the flag.

```
200 OK
X-Request-Id: {someRequestId}

{
    "id": "1354b6cf-f23d-4a93-9e54-bfb9eafd3d75",
    "applicationId": "45f502c9-25c1-4a09-a7a3-f9d3768ffacf",
    "name": "test-flag",
    "description": "Test Flag",
    "value": true,
    "createdAt": "2019-06-27T17:34:25+03:00",
    "updatedAt": "2019-06-27T17:34:25+03:00"
}
```

#### Possible Errors

| What      | When                                     |
| --------- | ---------------------------------------- |
| Not Found | Flag does not exist for this application |

### 4.4. Updating a Flag

Updates a flag with given details for authorized account. It requires [account authorization](#account-authorization).

#### Example Request

None of the fields are required. Provided values will be used in the update.

```
PUT /applications/{applicationId}/flags/{flagId}
Authorization: Bearer {someAccountToken}

{
    "name": "test-flag",
    "description": "Test Flag",
    "value": true
}
```

#### Example Response

Response payload will contain a Json object of the flag.

```
200 OK
X-Request-Id: {someRequestId}

{
    "id": "1354b6cf-f23d-4a93-9e54-bfb9eafd3d75",
    "applicationId": "45f502c9-25c1-4a09-a7a3-f9d3768ffacf",
    "name": "test-flag",
    "description": "Test Flag",
    "value": true,
    "createdAt": "2019-06-27T17:34:25+03:00",
    "updatedAt": "2019-06-27T17:34:25+03:00"
}
```

#### Possible Errors

| What         | When                                           |
| ------------ | ---------------------------------------------- |
| Already Used | Flag name is already used for this application |

### 4.5. Deleting a Flag

Deletes a flag for authorized account. It requires [account authorization](#account-authorization).

#### Example Request

```
DELETE /applications/{applicationId}/flags/{flagId}
Authorization: Bearer {someAccountToken}
```

#### Example Response

```
200 OK
X-Request-Id: {someRequestId}
```

## 5. SDK APIs

Flagly SDKs make it easier to use Flagly as a client. For more details, check out the documentation of the SDK you're interested in. In order for an SDK to make requests on behalf of an application, SDK requests require [application authorization](#application-authorization).

### 5.1. Getting a Flag

Gets a flag for authorized application. It requires [application authorization](#application-authorization).

#### Example Request

```
GET /flags/{flagName}
Authorization: Bearer {someApplicationToken}
```

#### Example Response

Response payload will contain a Json object of the flag.

```
200 OK
X-Request-Id: {someRequestId}

{
    "id": "1354b6cf-f23d-4a93-9e54-bfb9eafd3d75",
    "applicationId": "45f502c9-25c1-4a09-a7a3-f9d3768ffacf",
    "name": "test-flag",
    "description": "Test Flag",
    "value": true,
    "createdAt": "2019-06-27T17:34:25+03:00",
    "updatedAt": "2019-06-27T17:34:25+03:00"
}
```

#### Possible Errors

| What      | When                                     |
| --------- | ---------------------------------------- |
| Not Found | Flag does not exist for this application |
