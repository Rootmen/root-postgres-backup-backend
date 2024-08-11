# root-postgres-backup-backend
Бэкенд части приложения по бэкапированию




# Основные модули

###### Модуль REST сервиса принимающий запросы
```shell script
./gradlew source:modules:endpoint:rest:tasks --console=plain quarkusDev -Ptarget=rest -Ddebug=5005
```
> **_NOTE:_**  Swagger UI http://localhost:8090/q/swagger-ui/#/

###### Модуль выполнения задач из REST сервиса

```shell script
./gradlew source:modules:endpoint:task-runner:tasks --console=plain quarkusDev -Ptarget=task-runner -Ddebug=5006
```
> **_NOTE:_** У него нет коечной точки все общение проходит через RabbitMQ

###### Модуль websocket по выдаче сообщений

```shell script
./gradlew source:modules:endpoint:websocket:tasks --console=plain quarkusDev -Ptarget=websocket -Ddebug=5007
```
> **_NOTE:_**  Работает на ws://localhost:8060/general-scheme