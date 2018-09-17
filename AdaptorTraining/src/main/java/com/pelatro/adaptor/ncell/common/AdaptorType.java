package com.pelatro.adaptor.ncell.common;

public enum AdaptorType {

	OCS_VOICE( "ocs_voice", 1 ),
	OCS_SMS( "ocs_sms", 2 ),
	OCS_DATA( "ocs_data", 3 ),
	OCS_RECHARGE( "ocs_recharge", 4 ),
	OCS_MGR( "ocs_mgr", 5 ),
	OCS_MON( "ocs_mon", 5 ),
	OCS_COM( "ocs_com", 5 ),
	CRM( "customer_details", 80 ),
	DEVICE( "device_details", 201 ),
	SUBSCRIBER( "ocs_subscriber", 7 );

	public final String label;

	public final int id;

	private AdaptorType( String label, int id ) {
		this.label = label;
		this.id = id;
	}

	public static AdaptorType decode( String raw ) {
		for ( AdaptorType e : values() )
			if ( e.label.equals( raw ) )
				return e;

		return null;
	}

}
