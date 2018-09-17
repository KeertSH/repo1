package com.pelatro.adaptor.ncell.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
import com.pelatro.adaptor.ncell.db.dto.Subscriber;
import com.pelatro.adaptor.ncell.db.dto.SubscriberInfo;

public class SubscriberIDLookup {
	private static Logger logger=LogManager.getLogger(SubscriberIDLookup.class);
	private List<SubscriberInfo> subscriber;
	private StatelessSession session = null;
	private Query query;
	private Map<String, SubscriberInfo> subscriberMap = new HashMap<>();
	private static SubscriberIDLookup subscriberIDLookup;
	@SuppressWarnings("unchecked")
	private SubscriberIDLookup()
	{
		logger.info("Constructing SubscriberID-DB");
		session=DbHenchman.getHenchman().getSessionFactory().openStatelessSession();
		logger.info("Loading Subscriber Details....");
		query=session.createQuery("From SubscriberInfo");
		subscriber=query.list();
		logger.info(
				String.format( "... %d Subscriber Details slurped ...",
						subscriber.size() ) );
		for(SubscriberInfo sub:subscriber)
		{
			subscriberMap.put(sub.getMsisdn(), sub);
		}
		logger.info( String.format( "... %d Subscriber Details loaded ..", subscriber.size() ) );
				session.close();
	}
	
	public Integer getSubscriberID(String msisdn)
	{
		if(subscriberMap.containsKey(msisdn))
			return (subscriberMap.get(msisdn).getId());
		return 0;
	}	
	public static SubscriberIDLookup initialize()
	{
		subscriberIDLookup=new SubscriberIDLookup();
		return subscriberIDLookup;
	}
}
