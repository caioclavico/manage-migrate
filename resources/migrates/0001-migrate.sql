CREATE KEYSPACE IF NOT EXISTS manage
WITH REPLICATION = {
   'class' : 'SimpleStrategy',
   'replication_factor' : 1};

CREATE TABLE IF NOT EXISTS manage.migrates (
    id text,
    conteudo text,
    PRIMARY KEY ((id)));
