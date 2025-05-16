# SimplEA

Kleines Buchhaltungstool. Ungetestet und nicht fertig. 
Verwendet einen OICD Provider und MongoDB.

<img src="https://raw.githubusercontent.com/krisraich/simplea/refs/heads/master/docu/buchungen.png" width="600" />
<img src="https://raw.githubusercontent.com/krisraich/simplea/refs/heads/master/docu/auszug.png" width="600" />


## Authentik Config:

 - QUARKUS_OIDC_AUTH_SERVER_URL=https://authentik.myserver.at/application/o/simplea/
 - QUARKUS_OIDC_CLIENT_ID=...
 - QUARKUS_OIDC_CREDENTIALS_SECRET=...

Callback in Authentik: https://simplea.myserver.at/oidc/callback

SimplEA verwendet Refresh Tokens.. Daher auch den Grant `offline_access` in Authentik hinzufügen (diese ist ab Version 2024.2 nicht mehr standardmäßig aktiv)