package WorkBench;

import java.io.StringReader;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.ResourceBundle;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public class XSDMediator implements Callable {
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		 MuleMessage msg = eventContext.getMessage();
		    String xsd = msg.getProperty("senderXSD", PropertyScope.INVOCATION);
		    String src=eventContext.getMessageAsString();
			java.io.InputStream inputxsd= IOUtils.toInputStream(xsd);
			InputSource xml;
			 
			 xml = new InputSource(inputxsd);
			 StreamSource xmlInputFile = new StreamSource(new StringReader( src));
			
			 SAXSource source = new SAXSource(xml);
	
			 try {
		    	   SchemaFactory factory = 
		                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		    	   Schema schema = factory.newSchema(source);
		    	   Validator validator = schema.newValidator();
		    	   validator.validate(xmlInputFile);
		    	   //System.out.println("Valid File");
				  } catch (SAXException e) {
			            System.out.println("Exception: Invalid input: "+e.getMessage());
			            System.out.println("error" + e.toString());
			            return e.getMessage();
			        }
		/*}*/
		return true;
	}
}
