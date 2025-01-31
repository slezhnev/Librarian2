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
* Now you can access KeyCloak UI via https://keycloak.192.168.8.4.nip.io (default username/psw: admin/admin)
* Also - will be a good idea to change default admin password
8. Configure KeyCloak
* Create new realm "librarian2"
* Create new user / users (create at least one)
* Create client "librarian2-frontend"
    * `OpenID Connect, Client authorization: off, Standart flow: on, Direct access grant: on, Valid redirectURIs: https://librarian.192.168.8.4.nip.io/* https://localhost:8443/*, Web origins: *`
* Create client "librarian2-backend"
    * `OpenID Connect, Client authorization: on, Authorization: on, Standart flow: on, Direct access grant: on, Valid redirectURIs: https://librarian.192.168.8.4.nip.io/* https://localhost:8443/*, Web origins: *`
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
* Enable to use Nexus in microk8s cluster:
    * Create the directory `sudo mkdir -p /var/snap/microk8s/current/args/certs.d/192.168.8.4:17001/`
    * Open the configuration file `sudo nano /var/snap/microk8s/current/args/certs.d/192.168.8.4:17001/hosts.toml`
    * Place the following content inside
```
server = "http://192.168.8.4:17001"

[host."http://192.168.8.4:17001"]
capabilities = ["pull", "resolve"]
```
    * Restart the cluster
```
microk8s stop
microk8s start
```

9. Install Grafana-OTel-LGTM.
* `kubectl create namespace otel`
* `kubectl apply -n otel -f https://raw.githubusercontent.com/grafana/docker-otel-lgtm/refs/tags/v0.8.2/k8s/lgtm.yaml`
* `kubectl apply -n otel -f lgtm-ingress.yaml`
* `kubectl apply -n otel -f lgtm-ingress-grpc.yaml` (currently are not used AND NOT TESTED (!), but why not?)
* Grafana will be available on https://grafana.192.168.8.4.nip.io (admin/admin) by default. You can configure address in ingress specification.
* Import `grafana/librarian2_dashboard.json` to Grafana
* NB This instance will LOST all data on manual restart. So you will need to import all dashboards any time due to manual redeploy / restart.

9. Deploy the application via kubectl
* Build and publish the version with `build.cmd`
* Create namespace "librarian2"
* Create secret "librarian2" with db password and keycloak secret (you can use `librarian2-secret.yaml` as template)
* Update library files download location in `librarian2.yaml` if needed (`spec.template.spec.volumes[].hostPath.path`)
* Also update `spec.template.spec.securityContext.supplementalGroups` and pass here groupId for access to needed folder (if you are used `transmision-daemon` - it should be id of group, created to work with it (in my case it was `debian-transmission` with gId:112))
* Deploy deployment and ingress
```
kubectl apply -f librarian2.yaml
kubectl apply -f librarian2-ingress.yaml
```
* Application will be accessible via https://librarian.192.168.8.4.nip.io/ 

10. Deploy application via helm
* Build and publish the version with `build.cmd`
* Create namespace "librarian2"
* Create secret "librarian2" with db password and keycloak secret (you can use `librarian2-secret.yaml` as template)
* Take a look into `helm/librarian/values.yaml` and configure anything you want
* Perform install `helm install -n librarian2 librarian2 ./librarian2'
* That's all - link to application url will be provided after installation in console

11. Configure run on development machine in Visual Code Studio
* Create file `.env` with following content:
```
quarkus.datasource.password = <db password>
quarkus.oidc.credentials.secret = <keycloak secret from librarian2-backend client>
quarkus.http.ssl.certificate.files=tls.crt
quarkus.http.ssl.certificate.key-files=tls.key
quarkus.http.insecure-requests=disabled
```
* Somehow connect content of the library to local machine (map it as y: drive for example)
* Create file 'library.storagePath.override.properties' with following content:
```
library.storagePath.2=y:/_Lib.rus.ec - Официальная/lib.rus.ec/
library.inpxPath.2=y:/_Lib.rus.ec - Официальная/lib.rus.ec/librusec_local_fb2.inpx
```
This will overwrite location of librusec library from default to locally mapped location
* Copy content from secret "librarian2-certificate-tls" (in namespace "librarian2") to tls.crt and tls.key respectively.
* Local copy can be executed via `run_jar.cmd`