for qt in testsuite/to_be_choosen_sparql_templates/*.sparql-template;
do
   qt2=${qt##*templates/}
   bin/Release/watdiv -q model/wsdbm-data-model.txt ${qt} 500 1 > testsuite/to_be_choosen_queries/${qt2%.sparql-template}.queryset ;
done;
