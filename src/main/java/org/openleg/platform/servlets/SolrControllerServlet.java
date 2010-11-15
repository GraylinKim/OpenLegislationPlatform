package org.openleg.platform.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
//import org.openleg.platform.parsers.InputParser;

@SuppressWarnings("serial")
public class SolrControllerServlet extends HttpServlet {
	
	String fileBase;
	SolrServer server;
	
	public SolrControllerServlet() {
		fileBase = "/home/openleg/OpenLegislationPlatform/src/main/resources/input/";
		try {
			server = (SolrServer)new CommonsHttpSolrServer("http://localhost:8080/solr/");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		StringTokenizer tokens = new StringTokenizer(request.getRequestURI(),"/");
		tokens.nextToken();//Throw out 'platform'
		tokens.nextToken();//Throw out 'solr'
		
		if(tokens.hasMoreTokens()) {
			String command = tokens.nextToken();
			
			if(command.equals("reset")) {
				out.println("Reseting solr database");
				try {
					server.deleteByQuery("*:*");
					server.commit();
				} catch (SolrServerException e) {
					e.printStackTrace();
				}
			} else if (command.equals("index")) {
				String filename = tokens.nextToken();
				
				//InputParser input = new InputParser(fileBase+filename+".xml");
				try {
					//server.add(input.getSolrDocuments());
					server.commit();
					out.println("Indexing of "+filename+" complete");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SolrServerException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
