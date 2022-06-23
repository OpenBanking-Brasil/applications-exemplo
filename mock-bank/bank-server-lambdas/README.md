# Bank Server Lambdas
## Build instructions

First, build the bank-swagger project and publish it to your local maven repository:

```
user@host:~/bank-swagger$ ./gradlew clean build
user@host:~/bank-swagger$ ./gradlew publishToLocalMaven
```

Then build the bank lambdas:

```
user@host:~/bank-server-lambdas$ ./gradlew clean build
```
