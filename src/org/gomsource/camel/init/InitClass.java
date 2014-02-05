package org.gomsource.camel.init;

import java.util.List;



import javax.jms.ConnectionFactory;



import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.StartupListener;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxrs.CxfRsComponent;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitClass implements StartupListener {

	private CamelContext ctx;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InitClass.class);
	
	

	public InitClass() {
		initCtx();
		
	}

	private void initCtx() {
		LOGGER.info("Initialization Started..");
		try 
		{
			ctx = new DefaultCamelContext();
			this.registerComponents();
			
			RouteBuilder rb = new NeoRouteBuilder(ctx,"http://localhost:7474/db/data/");
			ctx.addRoutes(rb);			
			ctx.addStartupListener(this);

			ctx.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					ctx.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		LOGGER.info("... initialization finished.");
		this.waitForStop();
		
	}
	
	
	
	private RouteBuilder classicRouteBuilder ()
	{
		RouteBuilder rb = new RouteBuilder(ctx) {

		@Override
		public void configure() throws Exception {
			/*from("test-jms:queue:test.queue").log("Transfert File").to(
					"file://test");*/
			/*from("spring-neo4j:http://localhost:7474/database?options").log("Reading twit").to(
					"file://test");*/
			//from("cxfrs://localhost:7474/data/db").to("file://test");
			//from("direct:start").to("http://localhost:7474/db/data/").to("file://test");
			from("timer://foo?fixedRate=true&delay=0&period=10").to("http://localhost:7474/db/data/").to("file://test");
			
		}
		};
		return rb;
	}
	
	private void registerComponents ()
	{
		
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
		// ConnectionFactory connectionFactory = new
		// ActiveMQConnectionFactory("tcp://localhost:20");
		//TransportFactory tcp = new TcpTransportFactory();
		// Note we can explicit name the component
		ctx.addComponent("test-jms",
				JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		
		
		ctx.addComponent("cxfrs", new CxfRsComponent());
		
		HttpComponent httpcomp  =new HttpComponent();
		
		
		ctx.addComponent("http", httpcomp);
	}

	public CamelContext getCtx() {
		return ctx;
	}

	public void setCtx(CamelContext ctx) {
		this.ctx = ctx;
	}

	
	public static void main(String[] args) {
		InitClass ic = new InitClass();
		
		
	
            
		//Exchange exc = template.send("http://localhost:7474/db/data/",ExchangePattern.InOut,proc );
	
		 //Message out = exc.getOut();
		// System.out.println("BODY : "+out.getBody());
		/*ConsumerTemplate ct = ic.getCtx().createConsumerTemplate();
		Exchange ex = ct.receive("http://localhost:7474/db/data");
		System.out.println(ex.getOut().getBody());
		System.out.println(ex.getIn().getBody());*/
		//ct.receive("twitter://endpoint?consumerKey=@SylvainRoussy");
		
		/*for (int i = 0; i < 10; i++) {
			template.sendBody("test-jms:queue:test.queue", "Test Message: " + i);

		}
		try {
			System.out.println("Seconde partie");
			//ic.getCtx().stop();
			
			ic.getCtx().addComponent(
					"test-jms2",
					JmsComponent
							.jmsComponentAutoAcknowledge(ic.connectionFactory));
			ic.getCtx().addRoutes(new RouteBuilder(ic.getCtx()) {

				@Override
				public void configure() throws Exception {
					from("test-jms2:queue:test.queue2").log("Transfert File")
							.to("file://test2");

				}
			});

			
			//ic.getCtx().start();
			for (int i = 0; i < 10; i++) {
				template.sendBody("test-jms2:queue:test.queue2",
						"Test Message 2 : " + i);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/

	}

	@Override
	public void onCamelContextStarted(CamelContext cctx, boolean arg1)
			throws Exception {

		List<Route> routes = cctx.getRoutes();
		LOGGER.info("Routes number " + routes.size());
		for (Route route : routes) {
			LOGGER.info("route : " + route.getId() + " "
					+ route.getEndpoint().getEndpointUri());
		}
		
		

	}
	
	void waitForStop() {
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

}
