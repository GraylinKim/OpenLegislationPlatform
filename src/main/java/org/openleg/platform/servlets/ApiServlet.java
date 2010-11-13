package org.openleg.platform.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.openleg.platform.parsers.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class ApiServlet extends HttpServlet {

	SolrServer server;
	
	HashMap<String,String> componentMap;
	HashMap<Pattern,ArrayList<String>> urlComponentMap;
	HashMap<Pattern,String> urlQueryMap;
	
	public ApiServlet() {
		
		//Initialize Data stores.
		this.componentMap = new HashMap<String,String>();
		this.urlComponentMap = new HashMap<Pattern,ArrayList<String>>();
		this.urlQueryMap = new HashMap<Pattern,String>();
		
		//Load the config to memory
		load("/home/openleg/OpenLegislationPlatform/src/main/resources/apiconfig.xml");
		
		try {
			server = (SolrServer)new CommonsHttpSolrServer("http://localhost:8080/solr/");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
	    try {
	    	String url = request.getRequestURI();
	    	PrintWriter out = response.getWriter();
		    String query = match(url, "/platform");
		    if(query!=null) {
		    	SolrQuery solrQuery = new SolrQuery();
		    	solrQuery.setQuery(query);
		    	solrQuery.setRows(Integer.MAX_VALUE);	
				QueryResponse solrResponse = server.query(solrQuery);
				SolrDocumentList results = solrResponse.getResults();
				
				out.println("<response>");
				out.println("\t<meta>");
				out.println("\t\t<totalResults>"+results.size()+"</totalResults>");
				out.println("\t</meta>");
				out.println("\t<results>");
				for( SolrDocument doc : solrResponse.getResults()) {
					String xml = (String)doc.get("xml");
					String docdef = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
					if(xml.startsWith(docdef))
						xml = xml.substring(docdef.length());

					out.println(xml);
				}
				out.println("\t</results>");
				out.println("</response>");
				
		    } else {
		    	out.println("URL: "+url+" does not appear to be valid");
		    }
	    } catch (SolrServerException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		ApiServlet config = new ApiServlet();
		
		String[] testUrls = new String[] {
				"/RCOS/api/bill/S66023.xml",
				"/RCOS/api/1.0/bill/S66023",
				"/RCOS/api/1.0/bill/S66023.json",
				"/RCOS/api/2.0/search.xml/sponsor:ADAMS"
		};
		
		for( String url : testUrls ) {
			System.out.println(url+" maps to query: "+config.match(url,"/RCOS"));
		}
	}
	
	public void load(String filename) {
		
		//Grab the document root of the file to load
		Element root = XmlUtil.getXmlDocument(filename).getDocumentElement();
		
		/* Load all the components into memory */
		NodeList components = root.getElementsByTagName("component");
		for( int n = 0; n < components.getLength(); n++) {
			Element component = (Element)components.item(n);
			componentMap.put(getChildValue(component,"name"),getChildValue(component,"match"));
			System.out.println("Loaded Component: "+getChildValue(component,"name"));
		}
		
		/* Load all the mappings into memory */
		NodeList mappings = root.getElementsByTagName("mapping");
		for( int n = 0; n < mappings.getLength(); n++ ) {
			Element mapping = (Element)mappings.item(n);
			String url = getChildValue(mapping,"url");
			
			// find all url Components and replace them with their regular expressions
			ArrayList<String> urlComponents = new ArrayList<String>();
			while(true) {
				
				// Find the start of a named component or break 
				int start = url.indexOf("${", 0);
				if (start == -1)
					break;
				
				// Find the end of the named component or report error and break
				int end = url.indexOf("}", start+2);
				if (end == -1) {
					System.out.println("Syntax Error in the url! "+getChildValue(mapping,"name"));
					break;
				}
				
				//Grab the component and mark its placement in the urlComponent map
				String urlComponent = url.substring(start+2,end);
				urlComponents.add(urlComponent);

				//Replace the component with its regular expression
				System.out.println("replacing urlComponent: "+urlComponent);
				url = replaceUrlComponent(url,urlComponent,start,end);
			}
			
			System.out.println("Done replacing components");
			
			//Validate the query up front to ensure fast failure on faulty configuration
			validateQuery(getChildValue(mapping,"query"));
			
			//Map the compiled pattern to its query and corresponding components
			//so we can map future capture values into the query for execution
			Pattern pattern = Pattern.compile(url);
			urlComponentMap.put(pattern, urlComponents);
			
			urlQueryMap.put(pattern, getChildValue(mapping,"query"));
			
			System.out.println("Loaded Mapping: "+getChildValue(mapping,"name"));
		}
	}
	
	public String match(String url, String prefix) {
		
		//Stripping off the prefix makes this function much more flexible		
		url = url.substring(prefix.length());
		
		//Attempt to match the url against the mappings loaded from configuration
		for(Pattern pattern : urlQueryMap.keySet()) {
			Matcher matcher = pattern.matcher(url);
			
			//Successful matches need to have components extracted and inserted into the query
			if(matcher.matches()) {
				String query = urlQueryMap.get(pattern);
				
				// find all query Components and replace them with their matched values
				ArrayList<String> components = urlComponentMap.get(pattern);
				while(true) {
					
					// Find the start of a named component or break
					int start = query.indexOf("${", 0);
					if (start == -1)
						break;
				
					// Find the end of the named component or report error and break
					int end = query.indexOf("}", start+2);
					if (end == -1) {
						System.out.println("Syntax Error in the query! "+query);
						break;
					}
					
					//Replace the component with its matched value
					query = replaceQueryComponent(query,query.substring(start+2,end),start,end,matcher,components);
				}
				
				//Return the query, now populated with values captured from the request url
				return query;
			}
		}
		
		//Return null in the event of no matches against current mapping configurations
		return null;
	}
	
	public String replaceQueryComponent(String query,String component, int start, int end, Matcher matcher, ArrayList<String> components) {
		
		//If substring starts out of bounds an exception is thrown, so avoid it
		if ( end+1 < query.length())
			return query.substring(0, start)+matcher.group(components.indexOf(component)+1)+query.substring(end+1);
		else
			return query.substring(0, start)+matcher.group(components.indexOf(component)+1);
	}
	
	public String replaceUrlComponent(String template, String component, int start, int end) {
		
		//If substring starts out of bounds an exception is thrown, so avoid it
		if( end+1 < template.length() )
			return template.substring(0,start)+componentMap.get(component)+template.substring(end+1);
		else
			return template.substring(0,start)+componentMap.get(component);
	}
	
	public String getChildValue(Element parent, String childname) {
		
		//If exactly 1 node isn't found then we have an error in the configuration file
		NodeList nodes = parent.getElementsByTagName(childname);
		if(nodes.getLength() != 1) {
			System.out.println("Element "+parent.getTagName()+" must have 1 and only 1 "+childname+" child. "+nodes.getLength()+" found");
			return null;
		}
		
		//Get the aggregated text value and trim it for consistency
		return nodes.item(0).getTextContent().trim();
	}
	
	public void validateQuery(String query) {
		
		// search for and remove any components to validate the query
		while(true) {
			
			// Find the start of a named component or break
			int start = query.indexOf("${", 0);
			if (start == -1)
				break;
		
			// Find the end of the named component or report error and break
			int end = query.indexOf("}", start+2);
			if (end == -1) {
				System.out.println("Syntax Error in the query! "+query);
				break;
			}
			
			// Chop off the validated segment and return when we're finished
			if ( end+1 < query.length())
				query = query.substring(end+1);
			else
				return;
		}
	}
}