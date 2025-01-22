1. We'll assume that we have microk8s on Linux
2. Enable the following addons:
* microk8s enable dashboard
* microk8s enable rbac
* microk8s enable ingress
* microk8s enable cert-manager
3. Configure admin user
* microk8s kubectl apply -f sa_create.yaml
* microk8s kubectl apply -f sa_rolebinding.yaml
4. Access to dashboard via port-forward
* Run port-forward.sh
* Script will provide a token and WILL NOT EXIT!
* You can access to dashboard via https://<cluster ip>:10443 with provided token
5. Configure cert-manager
* Create key in folder ./ca-certs/
    * openssl genrsa -out ca.key 2048
* Create CA certificate
    * openssl req -x509 -new -nodes -key ca.key -sha256 -days 1825 -out ca.crt
* Refresh microk8s certs
    * microc8s refresh-certs ./ca-certs/
* Convert key and cert to base64 format
    * cat ca.crt | base64 -w0
    * cat ca.key | base64 -w0
* Place cert and key to ca-secret.yaml
* Apply secret and ClusterIssuer
    * microk8s kubectl apply -f ca-secret.yaml
    * microk8s kubectl apply -f clusterissuer.yaml
* Check that issuer are fine
    * microk8s kubectl get clusterissuers ca-issuer -n cert-manager -o wide
    * You should got the following:
```
NAME        READY   STATUS                AGE
ca-issuer   True    Signing CA verified   38m
```
6. Create ingress for dashboard
* microk8s kubectl apply -f dashboardingress.yaml
* Check that ingress are fine
    * microk8s kubectl -n kube-system describe ingress kubernetes-dashboard-ingress
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
    * microk8s kubectl apply -f ca-secret.yaml
* Get token
    * microk8s kubectl get secret admin-user -n kube-system -o jsonpath="{.data.token}" | base64 -d
* Now you can connect to Dashboard via https://<cluster ip> with provided token