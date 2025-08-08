# Configuração do DataSource para WildFly/JBoss

## Arquivo standalone.xml (WildFly/JBoss)

Adicione a seguinte configuração ao arquivo `standalone.xml` na seção `<datasources>`:

```xml
<datasource jndi-name="java:jboss/datasources/SeguemeDS" pool-name="SeguemeDS" enabled="true" use-java-context="true">
    <connection-url>jdbc:postgresql://localhost:5432/segueme</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>postgres</user-name>
        <password>postgres</password>
    </security>
    <validation>
        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"/>
        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"/>
    </validation>
</datasource>
```

Certifique-se de que o driver PostgreSQL esteja configurado:

```xml
<drivers>
    <!-- ... outros drivers ... -->
    <driver name="postgresql" module="org.postgresql">
        <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
    </driver>
</drivers>
```

## Configuração para TomEE

Para o TomEE, crie um arquivo `resources.xml` em `src/main/webapp/WEB-INF/resources.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <Resource id="SeguemeDS" type="DataSource">
        JdbcDriver = org.postgresql.Driver
        JdbcUrl = jdbc:postgresql://localhost:5432/segueme
        UserName = postgres
        Password = postgres
        JtaManaged = true
    </Resource>
</resources>
```

## Configuração para Payara/GlassFish

Para Payara ou GlassFish, use o console de administração ou execute os seguintes comandos:

```
asadmin create-jdbc-connection-pool --datasourceclassname org.postgresql.ds.PGSimpleDataSource --restype javax.sql.DataSource --property user=postgres:password=postgres:url="jdbc:postgresql://localhost:5432/segueme" SeguemePool

asadmin create-jdbc-resource --connectionpoolid SeguemePool java:jboss/datasources/SeguemeDS
```

## Script de criação do banco de dados

Execute o seguinte script para criar o banco de dados:

```sql
CREATE DATABASE segueme;
```

## Configuração para ambiente de desenvolvimento

Para ambiente de desenvolvimento, você pode usar o seguinte arquivo `db.properties` em `src/main/resources/`:

```properties
# Configurações de conexão com o banco de dados
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/segueme
db.username=postgres
db.password=postgres

# Configurações do Hibernate
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.show_sql=true
hibernate.format_sql=true
hibernate.hbm2ddl.auto=update
```

## Observações importantes

1. Ajuste os valores de usuário e senha conforme seu ambiente
2. O banco de dados deve ser criado antes da primeira execução do sistema
3. A configuração `hibernate.hbm2ddl.auto=update` permite que o Hibernate atualize automaticamente o esquema do banco de dados, mas em ambiente de produção considere usar `validate` ou `none`
