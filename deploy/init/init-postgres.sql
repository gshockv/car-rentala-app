CREATE DATABASE keycloak_db;
CREATE USER keycloak_service WITH PASSWORD 'keycloak_password';
ALTER DATABASE keycloak_db OWNER TO keycloak_service;
GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO keycloak_service;

CREATE DATABASE reservation_db;
CREATE USER reservation_service WITH PASSWORD 'reservation_password';
ALTER DATABASE reservation_db OWNER TO reservation_service;
GRANT ALL PRIVILEGES ON DATABASE reservation_db TO reservation_service;

CREATE DATABASE inventory_db;
CREATE USER inventory_service WITH PASSWORD 'inventory_password';
ALTER DATABASE inventory_db OWNER TO inventory_service;
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inventory_service;


