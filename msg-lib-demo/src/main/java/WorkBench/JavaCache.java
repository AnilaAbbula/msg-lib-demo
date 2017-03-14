package WorkBench;
//import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;

import java.io.StringWriter;
//import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
//import java.sql.SQLXML;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.ResourceBundle;

//import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
//import org.mule.api.lifecycle.Callable;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;

import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import javax.xml.parsers.ParserConfigurationException;

public class JavaCache extends AbstractMessageTransformer{

                private  static Connection dbConn;
                private static JCS cache; 
                private static Document dataDoc = null;
                private static Element dataRoot =null;
               // private static final Log log = LogFactory.getLog(JavaCache.class);
                public String password;
                public String connectionPort;
                public String connection;
                public String user;
                public String DBuser;
                
                
                //public static String connectionPort=ResourceBundle.getBundle("dev").getString("ConnectionPort");
                //public static String username=ResourceBundle.getBundle("dev").getString("User");
                //public static String password=ResourceBundle.getBundle("dev").getString("Password");
                //public static String connection=ResourceBundle.getBundle("dev").getString("Connection");
                public String getPassword() {
                                return password;
                }
                public void setPassword(String password) {
                                this.password = password;
                }
                
                //public static String DBuser=ResourceBundle.getBundle("dev").getString("DataBaseUser");
                //private  static HashMap<String,SenderProperties> sender= new HashMap<String, SenderProperties>();

                public String getConnectionPort() {
                                return connectionPort;
                }
                public void setConnectionPort(String connectionPort) {
                                this.connectionPort = connectionPort;
                }
                
                
                public String getConnection() {
                                return connection;
                }
                public void setConnection(String connection) {
                                this.connection = connection;
                }
                
                
                public String getDBuser() {
                                return DBuser;
                }
                public void setDBuser(String dBuser) {
                                DBuser = dBuser;
                }
                public String getUser() {
                                return user;
                }
                public void setUser(String user) {
                                this.user = user;
                }
                public  void connectDB()   
                {
                                //to modify to read from properties
                                try{
                                                Class.forName(connection.toString());
                                                dbConn = DriverManager.getConnection(connectionPort.toString(),
                                                                                user.toString(), password.toString());
                                }
                                catch(Exception ex){ex.printStackTrace();}

                }
                
                public static  void  loadProperties(String ProjectName, String Source, String Query)          
                {

                                try{

                                                initializeOutput();

                                                PreparedStatement preStatement = dbConn.prepareStatement(Query);
                                                ResultSet rs = preStatement.executeQuery();

                                                ResultSetMetaData resultmetadata = null;
                                                resultmetadata = rs.getMetaData();
                                                int numCols = resultmetadata.getColumnCount();
                                                while (rs.next()) {

                                                                Element rowEl = dataDoc.createElement(Source); 
                                                                for (int i=1; i <= numCols; i++) {
                                                                                //For each column, retrieve the name and data
                                                                                String colName = resultmetadata.getColumnName(i);
                                                                                String colVal = rs.getString(i);
                                                                                //If there was no data, add "and up"
                                                                                if (rs.wasNull()) {
                                                                                                colVal = "and up";
                                                                                }
                                                                                //Create a new element with the same name as the column
                                                                                Element dataEl = dataDoc.createElement(colName);
                                                                                //Add the data to the new element
                                                                                dataEl.appendChild(dataDoc.createTextNode(colVal));
                                                                                //Add the new element to the row
                                                                                rowEl.appendChild(dataEl);
                                                                }

                                                                //Add the row to the root element                                             
                                                                dataRoot.appendChild(rowEl);
                                                }

                                                //System.out.println("Data root" + dataRoot.toString()); 
                                                StringWriter buffer = new StringWriter();
                                                TransformerFactory.newInstance().newTransformer().transform(
                                                                                new DOMSource(dataRoot), new StreamResult(buffer)
                                                                                );

                                                String xml2 = buffer.toString();
                                                String key1=ProjectName+"_"+Source;
                                                loadDataToCache(key1,xml2.toString());

                                                /*Writer writer = null;
                                                    try {
                                                                    if (Source == "Sender"){
                                                                    writer = new BufferedWriter(new OutputStreamWriter(
                                                              new FileOutputStream("D:\\test2.xml"), "utf-8"));}
                                                                    else{
                                                                                    writer = new BufferedWriter(new OutputStreamWriter(
                                                                                              new FileOutputStream("D:\\test23.xml"), "utf-8"));}

                                                        writer.write(xml2.toString());
                                                    } catch (IOException ex) {
                                                      // report
                                                                    ex.printStackTrace();
                                                    } finally {
                                                       try {dataRoot = null;dataDoc=null;
                                                       writer.close();} catch (Exception ex) {ex.printStackTrace();}
                                                    }*/
                                }catch(Exception ex){ex.printStackTrace();}
                                finally{dataRoot = null;dataDoc=null;}
                }
                public static void  initializeOutput()          
                {
                                try {
                                                //Create the DocumentBuilderFactory
                                                DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
                                                //Create the DocumentBuilder
                                                DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
                                                //Parse the file to create the Document
                                                // mapDoc = docbuilder.parse("mapping.xml");
                                                //Instantiate a new Document object
                                                dataDoc = docbuilder.newDocument();
                                } catch (Exception e) {
                                                System.out.println("Problem creating document: "+e.getMessage());
                                }
                                //Create a new element called "data"      
                                dataRoot = dataDoc.createElement("Project");

                }
                public static  void  loadDataToCache(String Key,Object Value)     
                {

                                try
                                {
                                                // Load the cache
                                                cache = JCS.getInstance( "wbProperties");
                                                cache.put(Key, Value);
                                                // System.out.println("Called key" +Key);
                                                //  System.out.println("Called Value" +Value);

                                }
                                catch( CacheException e )
                                {
                                                e.printStackTrace();
                                }

                }
                public static  String  getValueFromCacheQueue(String QueueName,String key,String Source)      
                {
                                String getvalue=new String();
                                Object xpathresult=null;
                                String outXMLString=null;
                                try
                                {
                                                // Load the cache
                                                cache = JCS.getInstance("wbProperties");
                                                getvalue = (String) cache.get(key);

                                                // System.out.println("Specific value" +temp.toString());
                                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                                factory.setNamespaceAware(true); // never forget this!
                                                DocumentBuilder builder;
                                                try {
                                                                builder = factory.newDocumentBuilder();
                                                                Document doc = builder.parse(new InputSource(new StringReader(getvalue)));
                                                                XPathFactory xpathfactory = XPathFactory.newInstance();
                                                                XPath xpath =  xpathfactory.newXPath();

                                                                XPathExpression expr = null;
                                                                if (Source == "Sender")
                                                                {
                                                                                expr = xpath.compile("/Project/"+Source+"[INPUT_DESTINATION='"+QueueName+"']");
                                                                                xpathresult = expr.evaluate(doc,XPathConstants.NODE);
                                                                                //System.out.println("-------"+xpathresult);
                                                                }

                                                                else {
                                                                                //expr = xpath.compile("/Project/Receiver");
                                                                                expr = xpath.compile("/Project/"+Source+"[INPUT_DESTINATION='"+QueueName+"']");
                                                                                xpathresult = expr.evaluate(doc,XPathConstants.NODESET);
                                                                }

                                                                //XPathExpression expr = xpath.compile("/Project/Sender[INTERFACE_ID='INSAPDGFILE']");
                                                                //XPathExpression expr = xpath.compile("/Project/Sender[2]");

                                                                DOMSource docSource = null;
                                                                if (xpathresult instanceof Node)
                                                                {
                                                                                docSource=new DOMSource((Node)xpathresult);
                                                                                StringWriter buffer = new StringWriter();
                                                                                TransformerFactory.newInstance().newTransformer().transform(
                                                                                                                docSource, new StreamResult(buffer)
                                                                                                                );

                                                                                outXMLString = buffer.toString();

                                                                }
                                                                else
                                                                {

                                                                                Document newXmlDocument = DocumentBuilderFactory.newInstance()
                                                                                                                .newDocumentBuilder().newDocument();
                                                                                Element root = newXmlDocument.createElement("root");
                                                                                newXmlDocument.appendChild(root);
                                                                                for (int i = 0; i < ((NodeList)xpathresult).getLength(); i++) {
                                                                                                Node node = ((NodeList)xpathresult).item(i);
                                                                                                Node copyNode = newXmlDocument.importNode(node, true);
                                                                                                root.appendChild(copyNode);
                                                                                }
                                                                                docSource=new DOMSource((Element) root);

                                                                                StringWriter buffer = new StringWriter();
                                                                                TransformerFactory.newInstance().newTransformer().transform(
                                                                                                                docSource, new StreamResult(buffer)
                                                                                                                );

                                                                                outXMLString = buffer.toString();
                                                                }





                                                } catch (ParserConfigurationException e) {
                                                                // TODO Auto-generated catch block
                                                                e.printStackTrace();
                                                }
                                                catch (NullPointerException e) {
                                                                //System.out.println("Caught a NullPointer Exception");
                                                                //log.info("Empty Cache for the first instance");
                                                                //e.printStackTrace();
                                                                //logger.info("Caught a NullPointer Exception");
                                                }
                                                catch (Exception e) {
                                                                // TODO Auto-generated catch block
                                                                e.printStackTrace();
                                                }


                                }
                                catch( CacheException e )
                                {
                                                e.printStackTrace();
                                }
                                return outXMLString;

                }
                public static  String  getValueFromCache(String interfaceId,String key,String Source)      
                {
                                String getvalue=new String();
                                Object xpathresult=null;
                                String outXMLString=null;
                                try
                                {
                                                // Load the cache
                                                cache = JCS.getInstance("wbProperties");
                                                getvalue = (String) cache.get(key);

                                                // System.out.println("Specific value" +temp.toString());
                                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                                factory.setNamespaceAware(true); // never forget this!
                                                DocumentBuilder builder;
                                                try {
                                                                builder = factory.newDocumentBuilder();
                                                                Document doc = builder.parse(new InputSource(new StringReader(getvalue)));
                                                                XPathFactory xpathfactory = XPathFactory.newInstance();
                                                                XPath xpath =  xpathfactory.newXPath();

                                                                XPathExpression expr = null;



                                                                if (Source == "Sender")
                                                                {
                                                                                expr = xpath.compile("/Project/"+Source+"[INTERFACE_ID='"+interfaceId+"']");
                                                                                xpathresult = expr.evaluate(doc,XPathConstants.NODE);
                                                                }

                                                                else {
                                                                                //expr = xpath.compile("/Project/Receiver");
                                                                                expr = xpath.compile("/Project/"+Source+"[INTERFACE_ID='"+interfaceId+"']");
                                                                                xpathresult = expr.evaluate(doc,XPathConstants.NODESET);
                                                                }

                                                                //XPathExpression expr = xpath.compile("/Project/Sender[INTERFACE_ID='INSAPDGFILE']");
                                                                //XPathExpression expr = xpath.compile("/Project/Sender[2]");

                                                                DOMSource docSource = null;
                                                                if (xpathresult instanceof Node)
                                                                {
                                                                                docSource=new DOMSource((Node)xpathresult);
                                                                                StringWriter buffer = new StringWriter();
                                                                                TransformerFactory.newInstance().newTransformer().transform(
                                                                                                                docSource, new StreamResult(buffer)
                                                                                                                );

                                                                                outXMLString = buffer.toString();
                                                                }
                                                                else
                                                                {

                                                                                Document newXmlDocument = DocumentBuilderFactory.newInstance()
                                                                                                                .newDocumentBuilder().newDocument();
                                                                                Element root = newXmlDocument.createElement("root");
                                                                                newXmlDocument.appendChild(root);
                                                                                for (int i = 0; i < ((NodeList)xpathresult).getLength(); i++) {
                                                                                                Node node = ((NodeList)xpathresult).item(i);
                                                                                                Node copyNode = newXmlDocument.importNode(node, true);
                                                                                                root.appendChild(copyNode);
                                                                                }
                                                                                docSource=new DOMSource((Element) root);

                                                                                StringWriter buffer = new StringWriter();
                                                                                TransformerFactory.newInstance().newTransformer().transform(
                                                                                                                docSource, new StreamResult(buffer)
                                                                                                                );

                                                                                outXMLString = buffer.toString();
                                                                }





                                                } catch (ParserConfigurationException e) {
                                                                // TODO Auto-generated catch block
                                                                e.printStackTrace();
                                                }
                                                catch (NullPointerException e) {
                                                                //System.out.println("Caught a NullPointer Exception");
                                                                //log.info("Empty Cache for the first instance");
                                                                //e.printStackTrace();
                                                                //logger.info("Caught a NullPointer Exception");
                                                }
                                                catch (Exception e) {
                                                                // TODO Auto-generated catch block
                                                                e.printStackTrace();
                                                }


                                }
                                catch( CacheException e )
                                {
                                                e.printStackTrace();
                                }
                                //System.out.println("receive out r----------"+outXMLString);
                                return outXMLString;

                }
                public static  void clearCacheData()         
                {                                              
                                try
                                {
                                                // Load the cache
                                                cache = JCS.getInstance("wbProperties");
                                                cache.clear();
                                }
                                catch( CacheException e )
                                {
                                                e.printStackTrace();
                                }


                }
                @Override
                public Object transformMessage(MuleMessage msg, String outputEncoding)
                                                throws TransformerException 
                {
                                // TODO Auto-generated method stub

                                //System.out.println("Insider Java n cal");
                                //MuleMessage msg = eventContext.getMessage();
                                String ProjectName= msg.getProperty("projectName", PropertyScope.INVOCATION);
                                String SenderKey=ProjectName+"_Sender";
                                String ReceiverKey=ProjectName+"_Receiver";

                                String InterfaceID =null;
                                String QueueName =null;
                                String Source =msg.getProperty("Source", PropertyScope.INVOCATION);
                                //System.out.println("Source value"  +Source + "    and clss is "+Source.getClass()+" And toalue is  "+Source.toString());

                                if(Source.equals("ClearCache")){
                                                clearCacheData();
                                                connectDB();    
                                                String sql="select SM.INTERFACE_ID, SM.PARTNER_NAME, SM.PROJECT_NAME, SM.CUSTOM_VALIDATION,SM.WB_VALIDATION, SM.CUSTOM_ENRICHMENT, SM.WB_ENRICHMENT, SM.CUSTOM_TRANSFORMATION,SM. WB_TRANSFORMATION, SM.CUSTOM_ROUTING, SM.WB_ROUTING, VAL.XSD_FILE, TRAN.XSLT_FILE,TRAN.ROUTING_XSLT_FILE,TRP.INPUT_DESTINATION FROM "+DBuser+".TBL_SENDER_MASTER SM left JOIN "+DBuser+".TBL_VALIDATION VAL ON SM.PARTNER_NAME=VAL.PARTNER_ID  AND SM.INTERFACE_ID=VAL.INTERFACE_ID left JOIN "+DBuser+".TBL_TRANSFORMATION TRAN ON SM.PARTNER_NAME=TRAN.PARTNER_ID AND SM.INTERFACE_ID=TRAN.INTERFACE_ID JOIN "+DBuser+".TBL_TRANSPORTS TRP ON SM.PARTNER_NAME=TRP.PARTNER_ID AND SM.INTERFACE_ID=TRP.INTERFACE_ID where SM.PROJECT_NAME='"+ProjectName+"'";
                                                //System.out.println("query result "+ sql);
                                                loadProperties(ProjectName,"Sender",sql);

                                                // Receiver Prop
                                                String sqlRcr="select SM.INTERFACE_ID, SM.PARTNER_NAME, SM.PROJECT_NAME, SM.CUSTOM_VALIDATION,SM.WB_VALIDATION, SM.CUSTOM_ENRICHMENT, SM.WB_ENRICHMENT, SM.CUSTOM_TRANSFORMATION,SM. WB_TRANSFORMATION, SM.CUSTOM_ROUTING, SM.WB_ROUTING, VAL.XSD_FILE,TRP.INPUT_DESTINATION ,TRP.OUTPUT_DESTINATION ,TRAN.XSLT_FILE FROM "+DBuser+".TBL_RECEIVER_MASTER SM left JOIN  "+DBuser+".TBL_VALIDATION VAL ON SM.PARTNER_NAME=VAL.PARTNER_ID AND SM.INTERFACE_ID=VAL.INTERFACE_ID left JOIN "+DBuser+".TBL_TRANSFORMATION TRAN ON SM.PARTNER_NAME=TRAN.PARTNER_ID AND SM.INTERFACE_ID=TRAN.INTERFACE_ID JOIN "+DBuser+".TBL_TRANSPORTS TRP ON SM.PARTNER_NAME=TRP.PARTNER_ID AND SM.INTERFACE_ID=TRP.INTERFACE_ID where SM.PROJECT_NAME='"+ProjectName+"'";
                                                //System.out.println("query result at RCR "+ sqlRcr);
                                                loadProperties(ProjectName,"Receiver",sqlRcr);
                                                return null;
                                }

                                InterfaceID= msg.getProperty("interfaceID", PropertyScope.INVOCATION);

                                if             (InterfaceID != null)        {
                                                String intfData= getValueFromCache(InterfaceID,SenderKey,"Sender"); //InterfaceId

                                                if  (intfData==null) {
                                                                clearCacheData();
                                                                connectDB();                     

                                                                String sql="select SM.INTERFACE_ID, SM.PARTNER_NAME, SM.PROJECT_NAME, SM.CUSTOM_VALIDATION,SM.WB_VALIDATION, SM.CUSTOM_ENRICHMENT, SM.WB_ENRICHMENT, SM.CUSTOM_TRANSFORMATION,SM. WB_TRANSFORMATION, SM.CUSTOM_ROUTING, SM.WB_ROUTING, VAL.XSD_FILE, TRAN.XSLT_FILE,TRAN.ROUTING_XSLT_FILE,TRP.INPUT_DESTINATION FROM "+DBuser+".TBL_SENDER_MASTER SM left JOIN "+DBuser+".TBL_VALIDATION VAL ON SM.PARTNER_NAME=VAL.PARTNER_ID  AND SM.INTERFACE_ID=VAL.INTERFACE_ID left JOIN "+DBuser+".TBL_TRANSFORMATION TRAN ON SM.PARTNER_NAME=TRAN.PARTNER_ID AND SM.INTERFACE_ID=TRAN.INTERFACE_ID JOIN "+DBuser+".TBL_TRANSPORTS TRP ON SM.PARTNER_NAME=TRP.PARTNER_ID AND SM.INTERFACE_ID=TRP.INTERFACE_ID where SM.PROJECT_NAME='"+ProjectName+"'";
                                                                //System.out.println("at interfaceid result " +sql);
                                                                loadProperties(ProjectName,"Sender",sql);

                                                                // Receiver Prop
                                                                String sqlRcr="select SM.INTERFACE_ID, SM.PARTNER_NAME, SM.PROJECT_NAME, SM.CUSTOM_VALIDATION,SM.WB_VALIDATION, SM.CUSTOM_ENRICHMENT, SM.WB_ENRICHMENT, SM.CUSTOM_TRANSFORMATION,SM. WB_TRANSFORMATION, SM.CUSTOM_ROUTING, SM.WB_ROUTING, VAL.XSD_FILE,TRP.INPUT_DESTINATION ,TRP.OUTPUT_DESTINATION ,TRAN.XSLT_FILE FROM "+DBuser+".TBL_RECEIVER_MASTER SM left JOIN  "+DBuser+".TBL_VALIDATION VAL ON SM.PARTNER_NAME=VAL.PARTNER_ID AND SM.INTERFACE_ID=VAL.INTERFACE_ID left JOIN "+DBuser+".TBL_TRANSFORMATION TRAN ON SM.PARTNER_NAME=TRAN.PARTNER_ID AND SM.INTERFACE_ID=TRAN.INTERFACE_ID JOIN "+DBuser+".TBL_TRANSPORTS TRP ON SM.PARTNER_NAME=TRP.PARTNER_ID AND SM.INTERFACE_ID=TRP.INTERFACE_ID where SM.PROJECT_NAME='"+ProjectName+"'";
                                                                //System.out.println("at interfaceid result receiver " +sqlRcr);
                                                                loadProperties(ProjectName,"Receiver",sqlRcr);

                                                                String intfData1= getValueFromCache(InterfaceID,SenderKey,"Sender"); //InterfaceId
                                                                //System.out.println("Value is "+intfData1.toString());

                                                                String intfData2= getValueFromCache(InterfaceID,ReceiverKey,"Receiver"); //InterfaceId
                                                                //System.out.println("Value Reciver is "+intfData2.toString());
                                                                if (Source.equals("Sender"))                                                      
                                                                {
                                                                                //System.out.println("Sender");
                                                                                return intfData1.toString();
                                                                }
                                                                else{
                                                                                //System.out.println("Rcr");
                                                                                return intfData2.toString();
                                                                }
                                                }
                                                else{
                                                                //System.out.println("Insider Java nt null");
                                                                String intfData1= getValueFromCache(InterfaceID,SenderKey,"Sender"); //InterfaceId
                                                                //System.out.println("Value is "+intfData1.toString());
                                                                String intfData2= getValueFromCache(InterfaceID,ReceiverKey,"Receiver"); //InterfaceId
                                                                //System.out.println("Value Reciver is "+intfData2);
                                                                if (Source.equals("Sender")) {
                                                                                //System.out.println("sender-------------");
                                                                                return intfData1.toString();
                                                                }
                                                                else {
                                                                                //System.out.println("receiver----------"+intfData2);

                                                                                return intfData2.toString();
                                                                }
                                                }
                                }

                                else
                                {
                                                QueueName= msg.getProperty("inputQueue", PropertyScope.INVOCATION);
                                                //System.out.println("Queuename is---------------"+QueueName);
                                                String intfData= getValueFromCacheQueue(QueueName,SenderKey,"Sender"); //InterfaceId

                                                if  (intfData==null) {
                                                                clearCacheData();
                                                                connectDB();                     

                                                                String sql="select SM.INTERFACE_ID, SM.PARTNER_NAME, SM.PROJECT_NAME, SM.CUSTOM_VALIDATION,SM.WB_VALIDATION, SM.CUSTOM_ENRICHMENT, SM.WB_ENRICHMENT, SM.CUSTOM_TRANSFORMATION,SM. WB_TRANSFORMATION, SM.CUSTOM_ROUTING, SM.WB_ROUTING, VAL.XSD_FILE, TRAN.XSLT_FILE,TRAN.ROUTING_XSLT_FILE,TRP.INPUT_DESTINATION FROM "+DBuser+".TBL_SENDER_MASTER SM left JOIN "+DBuser+".TBL_VALIDATION VAL ON SM.PARTNER_NAME=VAL.PARTNER_ID  AND SM.INTERFACE_ID=VAL.INTERFACE_ID left JOIN "+DBuser+".TBL_TRANSFORMATION TRAN ON SM.PARTNER_NAME=TRAN.PARTNER_ID AND SM.INTERFACE_ID=TRAN.INTERFACE_ID JOIN  "+DBuser+".TBL_TRANSPORTS TRP ON SM.INTERFACE_ID=TRP.INTERFACE_ID where SM.PROJECT_NAME='"+ProjectName+"'";
                                                                loadProperties(ProjectName,"Sender",sql);

                                                                // Receiver Prop
                                                                String sqlRcr="select SM.INTERFACE_ID, SM.PARTNER_NAME, SM.PROJECT_NAME, SM.CUSTOM_VALIDATION,SM.WB_VALIDATION, SM.CUSTOM_ENRICHMENT, SM.WB_ENRICHMENT, SM.CUSTOM_TRANSFORMATION,SM. WB_TRANSFORMATION, SM.CUSTOM_ROUTING, SM.WB_ROUTING, VAL.XSD_FILE,TRP.INPUT_DESTINATION,TRP.OUTPUT_DESTINATION ,TRAN.XSLT_FILE FROM "+DBuser+".TBL_RECEIVER_MASTER SM left JOIN  "+DBuser+".TBL_VALIDATION VAL ON SM.PARTNER_NAME=VAL.PARTNER_ID AND SM.INTERFACE_ID=VAL.INTERFACE_ID left JOIN "+DBuser+".TBL_TRANSFORMATION TRAN ON SM.PARTNER_NAME=TRAN.PARTNER_ID AND SM.INTERFACE_ID=TRAN.INTERFACE_ID JOIN "+DBuser+".TBL_TRANSPORTS TRP ON SM.PARTNER_NAME=TRP.PARTNER_ID AND SM.INTERFACE_ID=TRP.INTERFACE_ID where SM.PROJECT_NAME='"+ProjectName+"'";
                                                                loadProperties(ProjectName,"Receiver",sqlRcr);

                                                                String intfData1= getValueFromCacheQueue(QueueName,SenderKey,"Sender"); //InterfaceId
                                                                //System.out.println("Value is "+intfData1.toString());

                                                                String intfData2= getValueFromCacheQueue(QueueName,ReceiverKey,"Receiver"); //InterfaceId
                                                                //System.out.println("Value Reciver is "+intfData2.toString());
                                                                if (Source.equals("Sender"))                                                      
                                                                {
                                                                                //System.out.println("Sender");
                                                                                //System.out.println("@@@@@@@@@@@@@  sender");-------this will be executed at sender
                                                                                return intfData1.toString();
                                                                }
                                                                else{
                                                                                //System.out.println("Rcr");
                                                                                //System.out.println("@@@@@@@@@@@@@@  receiver");
                                                                                return intfData2.toString();}

                                                }else{
                                                                //System.out.println("Insider Java nt null");
                                                                String intfData1= getValueFromCacheQueue(QueueName,SenderKey,"Sender"); //InterfaceId
                                                                //System.out.println("Value is "+intfData1.toString());
                                                                String intfData2= getValueFromCacheQueue(QueueName,ReceiverKey,"Receiver"); //InterfaceId
                                                                //System.out.println("Value Reciver is "+intfData2);
                                                                if (Source.equals("Sender")) {
                                                                                //System.out.println("sender-------------");
                                                                                return intfData1.toString();
                                                                }
                                                                else {
                                                                                //System.out.println("receiver----------");-------this will be executed at receiver
                                                                                return intfData2.toString();
                                                                }
                                                }

                                }
                
                }

}
