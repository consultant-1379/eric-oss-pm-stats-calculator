--
-- COPYRIGHT Ericsson 2022
--
-- The copyright to the computer program(s) herein is the property of
-- Ericsson Inc. The programs may be used and/or copied only with written
-- permission from Ericsson Inc. or in accordance with the terms and
-- conditions stipulated in the agreement/contract under which the
-- program(s) have been supplied.
--

CREATE TABLE IF NOT EXISTS "relation" ("id" bigint NOT NULL, "source_cell_id" bigint,"target_cell_id" bigint, "fdn" varchar(255), "intraSite" boolean, PRIMARY KEY (id));
INSERT INTO relation ("id","source_cell_id","target_cell_id","fdn","intraSite") values ('7328661376176907531','72166140783840515','72166140783840513','SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_142,MeContext=142186_BEACON_BEACH,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=142186_1_2,EUtranFreqRelation=2050,EUtranCellRelation=311480-142165-12','true');
INSERT INTO relation ("id","source_cell_id","target_cell_id","fdn","intraSite") values ('8035915982049115586','72166140783799830','72166140783815426','SubNetwork=ONRM_ROOT_MO,SubNetwork=MKT_143,MeContext=RELATION_58_37,ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=143007_2,EUtranFreqRelation=1,EUtranCellRelation=311480-143397-1','false');
