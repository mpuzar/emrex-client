select t1.column_name as ColumnName, t1.column_id,
( SELECT acc.position     
  FROM all_constraints ac JOIN all_cons_columns acc 
                               ON (ac.CONSTRAINT_NAME = acc.CONSTRAINT_NAME)
                          JOIN all_tab_cols atc ON (ac.owner = atc.owner AND 
                               ac.table_name = atc.TABLE_NAME AND 
                               acc.COLUMN_NAME = atc.COLUMN_NAME)
 WHERE  ac.table_name = t1.table_name
    AND acc.COLUMN_NAME = t1.column_name
   AND ac.constraint_type = 'P'
) as pk,
   nullable as null_status,
   data_type || ' ' || substr(
           decode( data_type, 'NUMBER',
                   decode( data_precision, NULL, NULL,
                    '('||data_precision||','||data_scale||')' ),
                                       '('||data_length||' Byte)'),
                  1,11) as data_length,
                  decode(t1.default_length, NULL , 'None', 'SOME') as data_def,
                  decode(t1.default_length, NULL , 'None', 'SOME') as data_def2
  
from all_tab_columns t1
where  t1.table_name = 'SW3I_KARUT_BESTILLING'
ORDER BY COLUMN_ID;
