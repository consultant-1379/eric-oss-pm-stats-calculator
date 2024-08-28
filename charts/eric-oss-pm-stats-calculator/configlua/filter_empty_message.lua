--
-- COPYRIGHT Ericsson 2023
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

function filter_empty_message(tag, timestamp, record)
    if record["message"] == "" or record["message"] == '' or record["message"] == nil then
        return -1, 0, 0
    end
    return 0, 0, 0
end
