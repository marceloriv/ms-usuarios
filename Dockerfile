# se utiliza una imagen base de maven con java 17 para compilar el proyecto

FROM maven:3.9.14-eclipse-temurin-17-alpine as build


# se crea una carpetra dentro de l ocntenedor para almacenar el codigo fuente
WORKDIR /workspace

# se copia el archivo pom.xml al contenedor
COPY pom.xml .

# todo lo que va dentro de la carppeta src se copia en la carpeta de src en workspace
COPY src src/


# se ejecuta el comando mvn clean package para compilar el proyecto y generar el archivo .jar
RUN mvn clean package -DskipTests

# jre es solo para correr mi aplicación
#alpine es una imagen ligera de linux que se utiliza para reducir el tamaño de la imagen final
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

# a nivel de docker ppuede tener el nombre que sea  , pero cuando lo copie en el contenedor se llama app.jar
COPY --from=build /workspace/target/*.jar app.jar

# se expone el puerto en el que corre la aplicación
EXPOSE 8080

# se ejecuta el java

#EntryPoint para correr la aplicación equivalente al comando java -jar app.jar que se pone en la consola 
ENTRYPOINT ["java", "-jar", "app.jar"]


# en la consola se puede ejecutar el comando docker build -t nombre-de-la-imagen . para construir la imagen a partir del Dockerfile
# docker build -t usuarios .


#luego crear un docker compose en l abase de datos de mysql 

#investigar sobre docker compose 

