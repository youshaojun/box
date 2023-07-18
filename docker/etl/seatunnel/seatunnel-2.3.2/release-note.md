# 2.3.2 Release Note

## Bug fix

### Core

- [Core] [API] Fixed generic class loss for lists (#4421)
- [Core] [API] Fix parse nested row data type key changed upper (#4459)
 
### Connector-V2

- [Json-format] [Canal-Json] Fix json deserialize NPE (#4195)
- [Connector-V2] [Jdbc] Field aliases are not supported in the query of jdbc source. (#4210)
- [Connector-V2] [Jdbc] Fix connection failure caused by connection timeout. (#4322)
- [Connector-V2] [Jdbc] Set default value to false of JdbcOption: generate_sink_sql (#4471)
- [Connector-V2] [JDBC] Fix TiDBCatalog without open (#4718)
- [Connector-V2] [Jdbc] Fix XA DataSource crash(Oracle/Dameng/SqlServer) (#4866)
- [Connector-V2] [Pulsar] Fix the bug that can't consume messages all the time. (#4125)
- [Connector-V2] [Eleasticsearch] Document description error (#4390)
- [Connector-V2] [Eleasticsearch] Source deserializer error and inappropriate (#4233)
- [Connector-V2] [Kafka] Fix KafkaProducer resources have never been released. (#4302)
- [Connector-V2] [Kafka] Fix the permission problem caused by client.id. (#4246)
- [Connector-V2] [Kafka] Fix KafkaConsumerThread exit caused by commit offset error. (#4379)
- [Connector-V2] [kafka] Fix the problem that the partition information can not be obtained when kafka is restored (#4764)
- [Connector-V2] [SFTP] Fix incorrect exception handling logic (#4720)
- [Connector-V2] [File] Fix read temp file (#4876)
- [Connector-V2] [CDC Base] Solving the ConcurrentModificationException caused by snapshotState being modified concurrently. (#4877)
- [Connector-V2] [Doris] update last checkpoint id when doing snapshot (#4881)
- [Connector-v2] [kafka] Fix the short interval of pull data settings and revise the format (#4875)
- [Connector-v2] [RabbitMQ] Fix reduplicate ack msg bug and code style (#4842)
- [Connector-V2] [Jdbc] Fix the error of extracting primary key column in sink (#4815)
- [Connector-V2] [Jdbc] Fix reconnect throw close statement exception (#4801)
- [Connector-V2] [Jdbc] Fix sqlserver system table case sensitivity (#4806)
- [Connector-v2] [File] Fix configuration file format and error comments (#4762)
- [Connector-v2] [Jdbc] Fix oracle sql table identifier (#4754)
- [Connector-v2] [Clickhouse] fix get clickhouse local table name with closing bracket from distributed table engineFull (#4710)

### Zeta(ST-Engine)

- [Zeta] Fix LogicalDagGeneratorTest testcase (#4401)
- [Zeta] Fix MultipleTableJobConfigParser parse only one transform (#4412)
- [Zeta] Fix missing common plugin jars (#4448)
- [Zeta] Fix handleCheckpointError be called while checkpoint already complete (#4442)
- [Zeta] Fix job error message is not right bug (#4463)
- [Zeta] Fix finding TaskGroup deployment node bug (#4449)
- [Zeta] Fix the bug of conf (#4488)
- [Zeta] Fix Connector load logic from zeta (#4510)
- [Zeta] Fix conflict dependency of hadoop-hdfs (#4509)
- [Zeta] Fix TaskExecutionService synchronized lock will not release (#4886)
- [Zeta] Fix TaskExecutionService will return not active ExecutionContext (#4869)
- [Zeta] Fix deploy operation timeout but task already finished bug (#4867)
- [Zeta] Fix restoreComplete Future can't be completed when cancel task (#4863)
- [Zeta] Fix IMap operation timeout bug (#4859)
- [Zeta] fix pipeline state not right bug (#4823)
- [Zeta] Fix the incorrect setting of transform parallelism (#4814)
- [Zeta] Fix master active bug (#4855)
- [Zeta] Fix completePendingCheckpoint concurrent action (#4854)
- [Zeta] Fix engine runtime error (#4850)
- [Zeta] Fix TaskGroupContext always hold classloader so classloader can't recycle (#4849)
- [Zeta] Fix task `notifyTaskStatusToMaster` failed when job not running or failed before run (#4847)
- [Zeta] Fix cpu load problem (#4828)
- [zeta] Fix the deadlock issue with JDBC driver loading (#4878)

### E2E

- [E2E] [Kafka] Fix kafka e2e testcase (#4520)
- [Container Version] Fix risk of unreproducible test cases #4591
- [E2e] [Mysql-cdc] Removing the excess MySqlIncrementalSourceIT e2e reduces the CI time (#4738)

## Improve

### Core

- [Core] [Spark] Push transform operation from Spark Driver to Executors (#4503)
- [Core] [Starter] Optimize code structure & remove redundant code (#4525)
- [Core] [Translation] [Flink] Optimize code structure & remove redundant code (#4527)
- [Core] [Starter] Add check of sink and source config to avoid null pointer exception. (#4734)

### Connector-V2

- [Connector-V2] [CDC] Improve startup.mode/stop.mode options (#4360)
- [Connector-V2] [CDC] Optimize jdbc fetch-size options (#4352)
- [Connector-V2] [CDC] Fix chunk start/end parameter type error (#4777)
- [Connector-V2] [SQLServer] Fix sqlserver catalog (#4441)
- [Connector-V2] [StarRocks] Improve StarRocks Serialize Error Message (#4458)
- [Connector-V2] [Jdbc] add the log for sql and update some style (#4475)
- [Connector-V2] [Jdbc] Fix the table name is not automatically obtained when multiple tables (#4514)
- [Connector-V2] [S3 & Kafka] Delete unavailable S3 & Kafka Catalogs (#4477)
- [Connector-V2] [Pulsar] Support Canal Format
- [Connector-V2] [CDC base] Implement Sample-based Sharding Strategy with Configurable Sampling Rate (#4856)
- [Connector-V2] [SelectDB] Add a jobId to the selectDB label to distinguish between tasks (#4864)
- [Connector-V2] [Doris] Add a jobId to the doris label to distinguish between tasks (#4839) (#4853)
- [Connector-v2] [Mongodb]Refactor mongodb connector (#4620)
- [Connector-v2] [Jdbc] Populate primary key when jdbc sink is created using CatalogTable (#4755)
- [Transform-V2] Optimize SQL Transform package and Fix Spark type conversion bug of transform (#4490)

### CI

- [CI] Fix error repository name in ci config files (#4795)

### Zeta(ST-Engine)

- [Zeta] Support run the server through daemon mode (#4161)
- [Zeta] Change ClassLoader To Improve the SDK compatibility of the client (#4447)
- [Zeta] Client Support Async Submit Job (#4456)
- [Zeta] Add more detailed log output. (#4446)
- [Zeta] Improve seatunnel-cluster.sh (#4435)
- [Zeta] Reduce CPU Cost When Task Not Ready (#4479)
- [Zeta] Add parser log (#4485)
- [Zeta] Remove redundant code (#4489)
- [Zeta] Remove redundancy code in validateSQL (#4506)
- [Zeta] Improve JobMetrics fetch performance (#4467)
- [Zeta] Reduce the operation count of imap_running_job_metrics (#4861)
- [Zeta] Speed up listAllJob function (#4852)
- [Zeta] async execute checkpoint trigger and other block method (#4846)
- [Zeta] Reduce the number of IMAPs used by checkpointIdCounter (#4832)
- [Zeta] Cancel pipeline add retry to avoid cancel failed. (#4792)
- [Zeta] Improve Zeta operation max count and ignore NPE (#4787)
- [Zeta] Remove serialize(deserialize) cost when use shuffle action (#4722)

## Feature

### Core

- [Core] [API] Support convert strings as List<T> option (#4362)
- [Core] [API] Add copy method to Catalog codes (#4414)
- [Core] [API] Add options check before create source and sink and transform in FactoryUtil (#4424)
- [Core] [Shade] Add guava shade module (#4358)

### Connector-V2

- [Connector-V2] [CDC] [SQLServer] Support multi-table read (#4377)
- [Connector-V2] [Kafka] Kafka source supports data deserialization failure skipping (#4364)
- [Connector-V2] [Jdbc] [TiDB] Add TiDB catalog (#4438)
- [Connector-V2] [File] Add file excel sink and source (#4164)
- [Connector-v2] [Snowflake] Add Snowflake Source&Sink connector (#4470)
- [Connector-V2] [Pular] support read format for pulsar (#4111)
- [Connector-V2] [Paimon] Introduce paimon connector (#4178)
- [Connector V2] [Cassandra] Expose configurable options in Cassandra (#3681)
- [Connector V2] [Jdbc] Supports GEOMETRY data type for PostgreSQL (#4673)
- [Transform-V2] Add UDF SPI and an example implement for SQL Transform plugin (#4392)
- [Transform-V2] Support copy field list (#4404)
- [Transform-V2] Add support CatalogTable for FieldMapperTransform (#4423)
- [Transform-V2] Add CatalogTable support for ReplaceTransform (#4411)
- [Transform-V2] Add Catalog support for FilterRowKindTransform (#4420)
- [Transform-V2] Add support CatalogTable for FilterFieldTransform (#4422)
- [Transform-V2] Add catalog support for SQL Transform plugin (#4819)

### Zeta(ST-Engine)

- [Zeta] Support for mixing Factory and Plugin SPI (#4359)
- [Zeta] Add get running job info by jobId rest api (#4140)
- [Zeta] Add REST API To Get System Monitoring Information (#4315)
- [Transform V2 & Zeta] Make SplitTransform Support CatalogTable And CatalogTable Evolution (#4396)
- [Zeta] Move driver into lib directory and change operation count (#4845)
- [Zeta] Add Metaspace size default value to config file (#4848)
- [Zeta] Reduce the frequency of fetching data from imap (#4851)
- [Zeta] Add OSS support for Imap storage to cluster-mode type (#4683)
- [Zeta] Improve local mode startup request ports (#4660)

## Docs 

- [Docs] Optimizes part of the Doris and SelectDB connector documentation (#4365)
- [Docs] Fix docs code style (#4368)
- [Docs] Update jdbc doc and kafka doc (#4380)
- [Docs] Fix max_retries default value is 0. (#4383)
- [Docs] Fix markdown syntax (#4426)
- [Docs] Fix Kafka Doc Error Config Key "kafka." (#4427)
- [Docs] Add Transform to Quick Start v2 (#4436)
- [Docs] Fix Mysql sink format doc (#4800)
- [Docs] Add the generate sink sql parameter for the jdbc sink document (#4797)
- [Docs] Add the generate sink sql parameter And example (#4769)
- [Docs] Redshift add defaultRowFetchSize (#4616)
- [Docs] Refactor connector-v2 docs using unified format Mysql (#4590)
