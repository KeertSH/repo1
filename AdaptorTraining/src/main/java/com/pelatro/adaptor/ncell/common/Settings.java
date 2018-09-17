package com.pelatro.adaptor.ncell.common;

public class Settings {

	private static String fileNameLabel = "%label%";
	private static boolean summaryLogged2db = false;

	public static String getFileNameLabel() {
		return fileNameLabel;
	}

	public static void setFileNameLabel( String fileNameLabel ) {
		Settings.fileNameLabel = fileNameLabel;
	}

	public static boolean isSummaryLogged2db() {
		return summaryLogged2db;
	}

	public static void setSummaryLogged2db( boolean summaryLogged2db ) {
		Settings.summaryLogged2db = summaryLogged2db;
	}
}
