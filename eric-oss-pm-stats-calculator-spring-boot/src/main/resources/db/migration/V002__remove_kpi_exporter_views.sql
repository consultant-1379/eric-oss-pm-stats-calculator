--
-- COPYRIGHT Ericsson 2024
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

DO
$$
    DECLARE
        sql text;
    BEGIN
        SELECT INTO sql string_agg('DROP VIEW ' || pg_class.oid::regclass || ';' || E'\n', ' ')
        FROM pg_class
        JOIN pg_namespace ON pg_namespace.oid = pg_class.relnamespace
        WHERE pg_class.relkind = 'v'
          AND pg_namespace.nspname = 'public';

        IF
            sql IS NOT NULL THEN
            RAISE NOTICE '%', sql;
            EXECUTE sql;
        ELSE
            RAISE NOTICE 'No views found. Nothing dropped.';
        END IF;
    END
$$;