create schema weather;

set search_path='weather';

CREATE TABLE weatherstations (
  id serial primary key,
  name text NOT NULL,
  average_temperature decimal NOT NULL,
  longitude decimal NOT NULL,
  latitude decimal NOT NULL
);

INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Hamburg', 13, 10.0, 53.6);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Snowdonia', 5, -3.9, 52.9);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Boston', 11, -71.0, 42.4);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Tokio', 16, 140.0, 35.7);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Cusco', 12, -72, -13.5);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Svalbard', -7, 21.0, 77.9);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Porthsmouth', 11, -1.1, 50.8);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Oslo', 7, 10.8, 59.9);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Marrakesh', 20, -8.0, 31.6);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Johannesburg', 25, 28.0, -26.2);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Anchorage', -2, -149.9, 61.2);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('San Francisco', 15, -122.4, 37.8);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Canberra', 25, 149.1, -35.3);
INSERT INTO weatherstations (name, average_temperature, longitude, latitude) VALUES('Novosibirsk', 10, 82.9, 55.0);
