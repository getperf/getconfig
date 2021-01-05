// Configuration Management Database connection setting

// Connection settings for MySQL database for Redmine management

// For registering inventory values ​​of the result sheet.
// Connect directly to MySQL and load data without the Redmine API.
// Load data is referenced from the Redmine plugin "redmine_getconfig"

// Test database environment.　Default value for built-in in-memory database
cmdb.username = "sa"
cmdb.password = "sa"
cmdb.url = "jdbc:h2:mem:"
cmdb.driver = "org.h2.Driver"

// Production database environment. Required the Redmine plugin "redmine_getconfig".
// If the following sysenv parameters are set, this value has priority first.
//     CMDB_USER, CMDB_PASSWORD, CMDB_URL, CMDB_DRIVER
//
//cmdb.username = "redmine"
//cmdb.password = "getperf"
//cmdb.url = "jdbc:mysql://redmine:3306/redmine?useUnicode=true&characterEncoding=utf8"
//cmdb.driver = "com.mysql.jdbc.Driver"

// For Redmine ticket registration.
// If the following sysenv parameters are set, this value has priority first.
//     REDMINE_URL, REDMINE_API_KEY

redmine.url = "http://redmine/redmine"
redmine.api_key = ""
