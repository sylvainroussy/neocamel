package org.gomsource.camel.init;

import java.util.Collection;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;

import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;

import org.neo4j.rest.graphdb.util.QueryResult;

public class NeoRouteBuilder extends RouteBuilder{

	RestGraphDatabase db;
	private String builderURI;
	
	private String query="MATCH (route:ROUTE{uri:{uri}})-[:FROM]->from-[:TO*]->tos RETURN route, from, collect(tos) AS To";
		
	public NeoRouteBuilder (CamelContext ctx, String builderURI)
	{
		super (ctx);
		db = new RestGraphDatabase("http://localhost:7474/db/data");
		this.builderURI = builderURI;
	}
	
	@Override
	public void configure() throws Exception {
		
		RestCypherQueryEngine engine = new RestCypherQueryEngine(db.getRestAPI());
		Map<String,Object> params = MapUtil.genericMap("uri", "route://neoToFile");
		
		QueryResult<Map<String,Object>> qresult = engine.query(this.query, params);
	
		for (Iterator<Map<String,Object>> iterator = qresult.iterator(); iterator.hasNext();) {
			
			Map<String,Object> type = iterator.next();
			System.out.println(type);
			RestNode from = (RestNode)type.get("from");
			String fromuri =""+from.getProperty("uri");  
				System.out.println("FROM URI : "+fromuri);
				
			RouteDefinition rd = 	from (fromuri);
			
			Collection<LinkedHashMap> col = (Collection<LinkedHashMap>)type.get("To");
			for (LinkedHashMap line : col) {				
				RestNode toNode = (RestNode) new RestNode (""+line.get("self"), db.getRestAPI()) ;
				System.out.println("TO : "+toNode.getProperty("uri"));
				rd.to(""+toNode.getProperty("uri"));
			}			
		}		
	}
}
