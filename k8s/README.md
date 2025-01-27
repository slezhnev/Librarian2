1. We'll assume that we have microk8s (based on Docker) on Linux
2. Enable the following addons:
```
microk8s enable dashboard
microk8s enable rbac
microk8s enable ingress
microk8s enable cert-manager
```
3. Configure admin user
```
microk8s kubectl apply -f sa_create.yaml
microk8s kubectl apply -f sa_rolebinding.yaml
```
4. Access to dashboard via port-forward
* Run port-forward.sh
* Script will provide a token and WILL NOT EXIT!
* You can access to dashboard via https://<cluster ip>:10443 with provided token
5. Configure cert-manager
* Create key in folder ./ca-certs/
`openssl genrsa -out ca.key 2048`
* Create CA certificate
`openssl req -x509 -new -nodes -key ca.key -sha256 -days 1825 -out ca.crt`
* Refresh microk8s certs
`microk8s refresh-certs ./ca-certs/`
* Convert key and cert to base64 format
```
cat ca.crt | base64 -w0
cat ca.key | base64 -w0
```
* Place cert and key to ca-secret.yaml
* Apply secret and ClusterIssuer
```
microk8s kubectl apply -f ca-secret.yaml
microk8s kubectl apply -f clusterissuer.yaml
```
* Check that issuer are fine
    * `microk8s kubectl get clusterissuers ca-issuer -n cert-manager -o wide`
    * You should got the following:
```
NAME        READY   STATUS                AGE
ca-issuer   True    Signing CA verified   38m
```
6. Create ingress for dashboard
`microk8s kubectl apply -f dashboardingress.yaml`
* Check that ingress are fine
    * `microk8s kubectl -n kube-system describe ingress kubernetes-dashboard-ingress`
    * You should get something like that
```
Name:             kubernetes-dashboard-ingress
Labels:           <none>
Namespace:        kube-system
Address:          127.0.0.1
Ingress Class:    nginx
Default backend:  <default>
Rules:
  Host        Path  Backends
  ----        ----  --------
  *
              /   kubernetes-dashboard:443 (10.1.101.112:8443)
Annotations:  cert-manager.io/cluster-issuer: ca-issuer
              nginx.ingress.kubernetes.io/backend-protocol: HTTPS
              nginx.ingress.kubernetes.io/ssl-passthrough: true
Events:
  Type    Reason  Age                 From                      Message
  ----    ------  ----                ----                      -------
  Normal  Sync    91s (x2 over 2m1s)  nginx-ingress-controller  Scheduled for sync
```
* Create permanent token for SA
`microk8s kubectl apply -f ca-secret.yaml`
* Get token
`microk8s kubectl get secret admin-user -n kube-system -o jsonpath="{.data.token}" | base64 -d`
* Now you can connect to Dashboard via https://<cluster ip> with provided token
7. Configure kubectl (to work without "microk8s" prefix)
```
cd ~
cd .kube
microk8s config > config
```
* You can copy this config to any other PC to use kubectl on it
7. Install KeyCloak
* Create Namespace
`kubectl create namespace keycloak`
* Create PostgreSQL DB with name "keycloak", user "keycloak" with password and grant all rights to it over newly created database (`GRANT ALL ON DATABASE keycloak TO keycloak;`)
* Create secret "db-password" with key "password" to store access DB password (created above)
* Create KeyCloak deployment
`kubectl create -n keycloak -f keycloak.yaml` (taken from https://raw.githubusercontent.com/keycloak/keycloak-quickstarts/refs/heads/main/kubernetes/keycloak.yaml)
* Create KeyCloak ingress
`kubectl apply -f keycloak-ingress.yaml` (taken from guide - https://www.keycloak.org/getting-started/getting-started-kube)
* Now you can access KeyCloak UI via http://keycloak.192.168.8.4.nip.io (default username/psw: admin/admin)
* Also - will be a good idea to change default admin password
8. Configure KeyCloak
* Create new realm "librarian2"
* Create new user / users (create at least one)
* Create client "librarian2-frontend"
    * `OpenID Connect, Client authorization: off, Standart flow: on, Direct access grant: on, Valid redirectURIs: <app url>/*, Web origins: *`
* Create client "librarian2-backend"
    * `OpenID Connect, Client authorization: on, Authorization: on, Standart flow: on, Direct access grant: on, Valid redirectURIs: <app url>/*, Web origins: *`
    * Open "Credentials", copy "Client secret" - it will used later in deployment configuration
* DO NOT MISS TO ADD `/*` to app url in "Valid redirect URIs" field!
* Additional details about initial configuration of KeyCloak are available here - https://www.keycloak.org/getting-started/getting-started-kube 
* Run app and check that KeyCloak auth works for frontend and backend requests

9. Install Sonatype Nexus 
* Use `nexus.sh` to bring up instance on Docker
* Login into it via `<ip>:8081` (initial user / password - admin / admin)
* Create new Docker registry `docker-staging` (with http connector on port 17001, "allow anonymous docker pull", deployment policy: "allow redeploy")
* Enable anonymous access
* Create new role "docker-write" - add `nx-repository-view-docker-docker-staging-add` and `nx-repository-view-docker-docker-staging-edit` priviledges. Add this role to "Anonymous user" (this will allow to perfrom a anonymous pushes to Docker registries)

9. Deploy the application
* Build and publish a version with `build.cmd`
* Create namespace "librarian2"