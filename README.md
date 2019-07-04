# Identity and Access Management (IAM)

IAM (Identity and access management) is a service that runs before any API calls are executed. In order for the API call to be forwarded to the underlying service, the caller's identity must be established (authentication). Following authentication, the caller's permissions are checked against a database to ensure the requested action is authorized

# Core Concepts

The core concepts to understand when using IAM is __authentication__ and __authorization__, learning the distinction and relationship between the two is crucial to understanding how permissions are implemented on the system. Role Based Access Control - or RBAC is another deeply integrated concept to understand in order to fully understand authorization

## Authentication
Identities are issued by the IdP (Auth0) through OAuth 2.0
OpenID Connect protocol in the form of signed JWT tokens. IAM (this service) validates these JWT tokens to extract the identity of the caller

There are multiple ways to obtain an signed JWT token, typically defined through one of the token grant flows defined by OAuth 2.0. They are only enumerated here by name for brevity. Click on them for details:

- [Authorization Code Exchange](https://www.oauth.com/oauth2-servers/access-tokens/authorization-code-request/)
- [Client Credentials Grant](https://www.oauth.com/oauth2-servers/access-tokens/client-credentials/)

    > Note that the SDK will attempt either approaches to obtain an token depending on the environment it is run from

## Authorization

IAM follows the Role Based Access Control (RBAC) model for authorization. Learn more about RBAC [Here](https://en.wikipedia.org/wiki/Role-based_access_control)

### Role

A `Role` defines a set of resources and actions one can perform on the system. A `Role` is an abstract description of a job function or a specific type of user
of the platform, it does not define who those users are. For that, please see `Binding`

Once a `Role` is created

### Binding

A `Binding` describes a relationship between a principal (user / group or machine user) and a `Role`. Principals bound
to the `Role` in a `Binding` are granted the permissions defined in the `Role`

# Session and Header Handling

IAM attempts to authenticate requests using either a encrypted cookie set via prior interactions, or - if an `Authorization` header is present - validates the access token provided over `Authorization` header. This design allow IAM to handle both
the authentication and authorization of both API calls as well as regular web based access

# IAM and proxy_auth in NGINX

Ingress nginx will use IAM as the `proxy_auth` upstream to determine if a incoming request should be routed to their respective configured upstream