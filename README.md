This is an infrastructure repository, that can be used to provision and deploy Guestbook webapp.

Running:

```
vagrant up
```

in main directory should set up:

* fully functional Jenkins, with precreated jobs and all configuration, accessible at http://192.168.32.10:8080
* fully provisioned and deploy-ready web server - running 'frontend' and 'backend' jobs should make webapp accessible at http://192.168.33.10

