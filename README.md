# SimplEA

Kleines Buchhaltungstool. Ungetestet und nicht fertig. 
Verwendet einen OICD Provider und MongoDB

## Required Config:

 - QUARKUS_OIDC_AUTH_SERVER_URL=https://authentik.myserver.at/application/o/simplea/
 - QUARKUS_OIDC_AUTHENTICATION_REDIRECT_PATH=/oidc/callback
 - QUARKUS_OIDC_CLIENT_ID=...
 - QUARKUS_OIDC_CREDENTIALS_SECRET=...

Callback in Authentik: https://simplea.myserver.at/oidc/callback!