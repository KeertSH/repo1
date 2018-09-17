package com.pelatro.adaptor.ncell.common;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONUtil {

	public static JSONObject parseObject( JSONObject json, String key ) {
		if ( json.get( key ) == null )
			throw new RuntimeException( String.format( " MISSING key : %s", key ) );

		return ( JSONObject ) json.get( key );
	}

	public static JSONArray parseArray( JSONObject json, String key ) {
		if ( json.get( key ) == null )
			throw new RuntimeException( String.format( " MISSING key : %s", key ) );

		return ( JSONArray ) json.get( key );
	}

	@SuppressWarnings( "unchecked" )
	public static <T> T parse( JSONObject json, String key, T fallback ) {
		if ( json.get( key ) == null )
			return fallback;

		return ( T ) json.get( key );
	}

	@SuppressWarnings( "unchecked" )
	public static <T> T parse( JSONObject json, String key ) {
		if ( json.get( key ) == null )
			throw new RuntimeException( String.format( " MISSING key : %s", key ) );

		return ( T ) json.get( key );
	}

	public static String[] parseStringArray( JSONObject json, String key ) {
		JSONArray raw = parse( json, key );

		String[] cast = new String[raw.size()];
		for ( int i = 0; i < raw.size(); ++i )
			cast[i] = ( String ) raw.get( i );

		return cast;
	}

	public static boolean[] parseBoolArray( JSONObject json, String key ) {
		JSONArray raw = parse( json, key );

		boolean[] cast = new boolean[raw.size()];
		for ( int i = 0; i < raw.size(); ++i )
			cast[i] = ( boolean ) raw.get( i );

		return cast;
	}

	public static int[] parseIntArray( JSONObject json, String key ) {
		JSONArray raw = parse( json, key );

		int[] cast = new int[raw.size()];
		for ( int i = 0; i < raw.size(); ++i )
			cast[i] = ( ( Long ) raw.get( i ) ).intValue();

		return cast;
	}

}
