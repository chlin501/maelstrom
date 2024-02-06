# Maelstrom Scala demos

This sbt project contains a simple Echo system

# Prerequisite

* Java: 8 or above

* Scala: 2.13.10

* sbt: 1.8.0

* Leiningen: 2.10.0

# Execution
## Steps

1. Assemble a jar

    ```shell
    cd /path/to/maelstrom/demo/scala
    sbt clean assembly
    ```

2. Start testing echo

    ```shell
    cd /path/to/maelstrom
    lein run test -w echo --bin demo/scala/server --nodes n1 --time-limit 10 --log-stderr
    ```
