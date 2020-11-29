Guida per startare flowable:

mvn install
npm install

----------------------------------------------------------------

-----1 con Docker:

cd D:/Uni/Tesi/flowable-engine/distro

scaricare ant ed esguire il comando: ant

cd D:/Uni/Tesi/flowable-engine/docker/all-in-one

eseguire il comando: ./build.sh

con questi comandi è stata creata l'immagine da poter eseguire con docker.

per stertare l'immagine:
docker run -p 8080:8080 flowable/all-in-one

una volta startata la prima volta è stato creato il container con la versione buildata dentro,
per riavviare la stessa build con i dati salvati bisogna:

-recuperare l'id del container: docker ps -a
-avviare il container: dockser start "id_container"
-mettarsi in tail sui log: docker logs "id_container" -f

------2 con eclipse e spring e quindi poter fare debug lato back end:

mvn clean install -DskipTests
mvn build

dopodichè avviare i seguenti progetti con sringboot:
- flowable-ui-admin
- flowable-ui-modeler

------------------------------------------------------------------

collegarsi poi:

http://localhost:8080/flowable-modeler/#/processes 

si verrà indirizzati alla finestra di login:

user: admin
password: test

-------------------------------------------------------------------

Link utili:

https://forum.flowable.org/
https://robferguson.org/blog/2019/01/05/how-to-build-flowable/





