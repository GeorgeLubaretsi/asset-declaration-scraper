package declaration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import net.sf.saxon.xqj.SaxonXQDataSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Main {

	private final static String ENGLISH_LANGUAGE = "en";
	private final static String GEORGIAN_LANGUAGE = "ka";
	private final static String ENGLISH_LANGUAGE_IN_XQUERY = "eng";
	private final static String GEORGIAN_LANGUAGE_IN_XQUERY = "geo";
	private final static String CSV_TYPE = "csv";
	private final static String AD_INFO_XML = "AssetDeclarationsQuestionsInformation.xml";
	private final static String FUNCTIONS_XQUERY_FILE = "FunctionsForEachCSVFile.xquery";
	private final static String MAIN_XQUERY_FILE = "RunOneQuestionOnOneAD.xquery";
	private final static String HEADER_XQUERY_FILE = "WriteHeaders.xquery";
	private final static String AD_XQUERY_FILE = "AssetDeclaration.xquery";
	
	private final static String CSV_NAME_XPATH_EXPR = "//q[@n=$QuestionID]/@t";

	public static void main(String[] args) {

		if (args != null && args.length == 5){
			
			defineConfigurations(args);
			
		}else{
			System.out.println("Error: parameters are invalid! Usage:");
			System.out.println("java -jar declarationXmlParsing.jar <xquery file path> <input xml folder path> <output csv folder path> <environment: dev or prod> <config file path>");
		}

	}

	private static void defineConfigurations(String[] args) {
		
		Properties prop = new Properties();
		
		String xqueryPath = args[0];
		String environment = args[3];
		
		String questionInfo = null;
		String functionxquery = null;
		String assetdeclaration = null;
		 
    	try {
    		prop.load(new FileInputStream(args[4]));
    		
    		String xqueryScriptsPath = prop.getProperty("scraper.ad.xqueryscripts."+environment);
    		questionInfo = xqueryScriptsPath + "/" + AD_INFO_XML;
    		functionxquery = xqueryScriptsPath + "/" + FUNCTIONS_XQUERY_FILE;
    		assetdeclaration = xqueryScriptsPath + "/" + AD_XQUERY_FILE;
    		
    		File mainXqueryFile = new File(xqueryPath + "/" + MAIN_XQUERY_FILE);
    		File adXqueryFile = new File(xqueryPath + "/" + AD_XQUERY_FILE);
    		File writeHeadersXqueryFile = new File(xqueryPath + "/" + HEADER_XQUERY_FILE);
    		
    		replaceSelected(mainXqueryFile, "scraper.ad.functionxquery.toreplace", functionxquery);
    		replaceSelected(adXqueryFile, "scraper.ad.questionsinfo.toreplace", questionInfo);
    		replaceSelected(writeHeadersXqueryFile, "scraper.ad.assetdeclaration.toreplace", assetdeclaration);
 
    		generateCsvFiles(args);
    		System.out.println("Done. The CSV files are in " + args[2]);
    		
    		// Re-initializing config files (ie revert the changes made previously, for the execution of generateCsvFiles())
    		replaceSelected(mainXqueryFile, functionxquery, "scraper.ad.functionxquery.toreplace");
    		replaceSelected(adXqueryFile, questionInfo, "scraper.ad.questionsinfo.toreplace");
    		replaceSelected(writeHeadersXqueryFile, assetdeclaration, "scraper.ad.assetdeclaration.toreplace");
    		
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
	}
	
	/**
	 * Method that replaces values in XQuery files, by whatever is in config.properties.
	 * @param file
	 * @param toReplace
	 * @param replacement
	 * @throws IOException
	 */
	public static void replaceSelected(File file, String toReplace, String replacement) throws IOException {

	    // we need to store all the lines
	    List<String> lines = new ArrayList<String>();

	    // first, read the file and store the changes
	    BufferedReader in = new BufferedReader(new FileReader(file));
	    String line = in.readLine();
	    while (line != null) {
	        if (line.contains(toReplace)) {
	            line = line.replaceAll(toReplace, replacement);
	        }
	        lines.add(line);
	        line = in.readLine();
	    }
	    in.close();

	    // now, write the file again with the changes
	    PrintWriter out = new PrintWriter(file);
	    for (String l : lines)
	        out.println(l);
	    out.close();

	}

	private static void generateCsvFiles(String[] args){
		
		String xqueryPath = args[0];
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();

			File file = new File(xqueryPath + "/" + AD_INFO_XML);

			Document document = builder.parse(file);
			XPath xPath =  XPathFactory.newInstance().newXPath();
			String expression = CSV_NAME_XPATH_EXPR;
			
			for (int i=0;i<=11;i++){
				DeclarationVariableResolver vr = new DeclarationVariableResolver() ;
				String questionid = Integer.toString(i);
				vr.setVariable("QuestionID", questionid);
				xPath.setXPathVariableResolver(vr);

				String documentName = xPath.compile(expression).evaluate(document);
				
				generateCsvFilesPerLanguage(args, ENGLISH_LANGUAGE, documentName, questionid);
				generateCsvFilesPerLanguage(args, GEORGIAN_LANGUAGE, documentName, questionid);
			}
			
		} catch (ParserConfigurationException e) {
			System.out.println("Erorr occured while creating DocumentBuilderFactory instance");
			e.printStackTrace();  
		} catch (SAXException e) {
			System.out.println("Erorr occured while parsing " + AD_INFO_XML);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not find " + AD_INFO_XML);
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			System.out.println("Error occurred while executing XPath on " + AD_INFO_XML);
			e.printStackTrace();
		}

	}

	private static void generateCsvFilesPerLanguage(String[] args, String lang, String csvName, String questionid) {
		XQPreparedExpression expr = null;
		XQConnection conn = null;

		String xqueryPath = args[0];
		String xmlInputPath = args[1];
		String csvFolderPath = args[2];

		String completeXMLPath = xmlInputPath + "/" + lang;

		try {
			SaxonXQDataSource ds = new SaxonXQDataSource();
			conn = ds.getConnection();
			
			String languageInXquery = null;
			if (lang.equalsIgnoreCase(ENGLISH_LANGUAGE)){
				languageInXquery = ENGLISH_LANGUAGE_IN_XQUERY;
			}else if (lang.equalsIgnoreCase(GEORGIAN_LANGUAGE)){
				languageInXquery = GEORGIAN_LANGUAGE_IN_XQUERY;
			}
			
			// Get the CSV file ready
			FileWriter result = new FileWriter(csvFolderPath + "/"+ lang + "/" + csvName+"_"+lang+".csv");

			// Creation of the file header
			DeclarationModel declarationInfo = new DeclarationModel(CSV_TYPE, questionid, completeXMLPath, languageInXquery, csvName, null);
			expr = getExpression(conn, declarationInfo, xqueryPath + "/" + HEADER_XQUERY_FILE);
			XQResultSequence xqjs  = expr.executeQuery();
			xqjs.writeSequence(result, null);
			
			// Creation of the file body
			File[] files = new File(completeXMLPath).listFiles();
			for (File file : files) {
				if (file.isFile()) {

					String filename = file.getName();
					String fileId = filename.replaceAll(".xml", "");
					declarationInfo.setDocId(fileId);
					declarationInfo.setFilename(null);
					expr = getExpression(conn, declarationInfo, xqueryPath + "/" + MAIN_XQUERY_FILE);
					xqjs  = expr.executeQuery();

					xqjs.writeSequence(result, null);

				}
			}
			result.flush();
			result.close();
			
			System.out.println("File " + csvName + "_" + lang +".csv has been generated");

			if (conn != null){
				conn.close();
			}

		} catch (FileNotFoundException e) {
			System.out.println("ERROR: The XQuery file was not found!");
			e.printStackTrace();
		} catch (XQException e) {
			System.out.println("ERROR: problem occured while using Saxon fucntionalities! Please check your inputs.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR: problem occured while writing a CSV file!");
			e.printStackTrace();
		} 
	}
	
	private static XQPreparedExpression getExpression(XQConnection conn, DeclarationModel model, String XQueryFile) throws FileNotFoundException, XQException{

		XQPreparedExpression expr = conn.prepareExpression(new FileInputStream(XQueryFile));	
		
		expr.bindAtomicValue(new QName("outputtype"), model.getType(), conn.createAtomicType(XQItemType.XQBASETYPE_STRING));
		expr.bindAtomicValue(new QName("QuestionID"), model.getQuestionId(), conn.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
		expr.bindAtomicValue(new QName("XMLstore"), model.getXmlStore(), conn.createAtomicType(XQItemType.XQBASETYPE_STRING));
		expr.bindAtomicValue(new QName("Language"), model.getLanguage(), conn.createAtomicType(XQItemType.XQBASETYPE_STRING));
		
		if (model.getFilename() != null){
			expr.bindAtomicValue(new QName("Filename"), model.getFilename(), conn.createAtomicType(XQItemType.XQBASETYPE_STRING));
		}
		
		if (model.getDocId() != null){
			expr.bindAtomicValue(new QName("DocID"), model.getDocId(), conn.createAtomicType(XQItemType.XQBASETYPE_STRING));
		}
		
		return expr;
	}
	

}
