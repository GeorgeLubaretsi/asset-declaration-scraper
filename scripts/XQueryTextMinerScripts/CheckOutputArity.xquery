(: 
 Author: Maarten Marx for TI Georgia
 Date: 2013-11-06
 Purpose:  Test if  each row in an AD output file generated by our Xquery text miner is of the same arity.
           Do that for all 11 questions.
           
           Idea is simpy to count the number of td cells in each row, and output all differnt counts that we find.
           If we find just one, it is OK.
          
          :)

  

import module namespace tiADQ = "http://transparency.ge/AssetDeclaration/FunctionsForEachCSVFile" at "FunctionsForEachCSVFile.xquery";
 
 
   declare variable $XMLstore := '/Users/admin/Documents/TIGeorgia/DeclarationsScraper/Declarations/XML-sources/';

  let $col :=  collection($XMLstore)  
return
 for $i in 0 to 11 
 let $icount := distinct-values( for $tr in tiADQ:ExtractTextToFile($col,$i,'xml','ADheader')//tr return count($tr//td))
 return 
 (: human readable output :) 
 concat("&#10;Question ",$i,' has rows with these many columns: ', string-join(for $i in $icount return string($i),','))
 
 (: Output at 2013-11-14
 Question 0 has rows with these many columns: 8 
Question 1 has rows with these many columns: 6 
Question 2 has rows with these many columns: 9 
Question 3 has rows with these many columns: 7 
Question 4 has rows with these many columns: 7 
Question 5 has rows with these many columns: 7 
Question 6 has rows with these many columns: 5 
Question 7 has rows with these many columns: 9 
Question 8 has rows with these many columns: 7 
Question 9 has rows with these many columns: 7 
Question 10 has rows with these many columns: 7 
Question 11 has rows with these many columns: 6

:)