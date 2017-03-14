package WorkBench;


//import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
/*import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;*/

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.IOUtils;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.xml.sax.InputSource;

public class XSLTMediator implements Callable{
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		String result=null;
		InputStream inputxsl=null;
		TransformerFactory factory=null;
		InputSource xml=null;
		SAXSource source1=null;
		try{
			
		MuleMessage msg = eventContext.getMessage();
		String src=eventContext.getMessageAsString();
		 
		String xslt=msg.getProperty("senderRoutingXSLT", PropertyScope.INVOCATION);
		//System.out.println("------------xslt"+ xslt);
		inputxsl = IOUtils.toInputStream(xslt);
		//System.out.println("----------inputxsl"+ inputxsl);
		xml = new InputSource(inputxsl);
		//System.out.println("-----------xml"+ xml);
		 source1 = new SAXSource(xml);
		//System.out.println("111111111111111111111111");
		factory= new net.sf.saxon.TransformerFactoryImpl();
		Transformer transformer = factory.newTransformer(source1);
		transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");

		transformer.setOutputProperty(OutputKeys.METHOD,"xml");


		transformer.setOutputProperty(OutputKeys.INDENT,"yes");

		StringReader reader = new StringReader(src);
		StringWriter xmlResultResource = new StringWriter();

		transformer.transform(
				new javax.xml.transform.stream.StreamSource(reader), 
				new javax.xml.transform.stream.StreamResult(xmlResultResource));
		result = xmlResultResource.toString();
		//System.out.println("result------"+result);
		return result;
		}
		catch(Exception e){
			
		}finally{
			if(null!=inputxsl) inputxsl.close();
		}
		return result;
	}
}
	

