#! /bin/bash

#
# COPYRIGHT Ericsson 2024
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#

function command_wrapper {
    log_message=''
    timestamp=`date +%G-%m-%eT%T.%3N`
    output_log=$($@ 2>&1 )
    log_message+="{\"timestamp\":\"$timestamp\","
    log_message+="\"version\":\"1.0.0\","
    log_message+="\"message\":\"$output_log\","
    log_message+="\"logger\":\"bash_logger\","
    log_message+="\"thread\":\"init app pm-stats-calculator db command: $@\","
    log_message+="\"path\":\"/init-db.sh\","
    log_message+="\"service_id\":\"eric-oss-pm-stats-calculator\","
    log_message+="\"severity\":\"info\"}"

    echo $log_message
}

function init_sql {
    until
     pg_isready; do
       command_wrapper echo "Database instance $PGHOST is not ready. Waiting ..."
       sleep 3
    done

    #===DB-SQL-SCRIPT======
    cat << EOF | psql
SELECT 'CREATE DATABASE "$KPIDATABASE"'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$KPIDATABASE')\gexec

DO \$\$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = '$KPI_SERVICE_DB_USER') THEN

      CREATE ROLE "$KPI_SERVICE_DB_USER" LOGIN;
      GRANT ALL PRIVILEGES ON DATABASE "$KPIDATABASE" TO "$KPI_SERVICE_DB_USER";
   END IF;
END
\$\$;
EOF

    #===

   if [ $? -eq 0 ];
     then
       command_wrapper echo "Create DB sql-script loaded into the $PGDATABASE DB";
     else
       command_wrapper echo "PG login failed";
       exit 1;
   fi
}

init_sql

command_wrapper echo "The Init container completed preparation"
