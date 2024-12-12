for qt in testsuite/to_be_choosen_templates/*.sparql-template;
do
   qt2=${qt##*templates/}
   bin/Release/watdiv -q model/wsdbm-data-model.txt ${qt} 20 1 > testsuite/custom_queries/${qt2%.sparql-template}.queryset ;
done;
