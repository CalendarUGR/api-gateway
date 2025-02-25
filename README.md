# API Gateway - CalendarUgr

## Descripción
El **API Gateway** es un componente clave dentro del sistema **CalendarUgr**, encargado de enrutar las solicitudes a los diferentes microservicios y gestionar la autenticación y autorización mediante tokens JWT.

## Características
- Enrutamiento de solicitudes a los microservicios adecuados.
- Validación de tokens JWT para autenticación de usuarios.
- Control de acceso basado en roles.
- Integración con los microservicios del sistema CalendarUgr.

## Requisitos previos
Para ejecutar este servicio, es necesario configurar las siguientes variables de entorno:

- `SECRET_KEY` : Secret key del jwt.

## Instalación y ejecución
1. Clonar el repositorio:
   ```sh
   git clone <repository-url>
   cd api-gateway
   ```
2. Configurar las variables de entorno:
   ```sh
   export SECRET_KEY = <your_jwt_secret_key>
   ```
3. Construir y ejecutar el servicio:
   ```sh
   ./mvnw spring-boot:run
   ```

## Endpoints
Este servicio no expone endpoints directamente, sino que enruta las solicitudes a los microservicios correspondientes.