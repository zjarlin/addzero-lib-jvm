# starter-flyway

`starter-flyway` is a Ktor starter that executes Flyway migrations from `datasources.*` and `flyway.*`
configuration before the rest of the application starters finish bootstrapping.

It owns Flyway wiring only. Business schema SQL, migration directories, and datasource definitions remain in the
consumer application.
