package ch.eawag.chemeql;

import java.util.HashMap;


class Mode extends java.lang.Object
{
	static final Mode TOTAL = new Mode("total",1);
	static final Mode CHECKPRECIP = new Mode("checkPrecip",2);
	static final Mode ADSORBENT1 = new Mode("adsorbent1",3);
	static final Mode ADSORBENT2 = new Mode("adsorbent2",3);
	static final Mode ADSORBENT3 = new Mode("adsorbent3",3);
	static final Mode ADSORBENT4 = new Mode("adsorbent4",3);
	static final Mode ADSORBENT5 = new Mode("adsorbent5",3);
	static final Mode ADSORBENT1_1 = new Mode("adsorbent1.1",3);
	static final Mode ADSORBENT1_2 = new Mode("adsorbent1.2",3);
	static final Mode ADSORBENT1_3 = new Mode("adsorbent1.3",3);
	static final Mode CHARGE1_1 = new Mode("charge1.1",4);
	static final Mode CHARGE2_1 = new Mode("charge2.1",4);
	static final Mode CHARGE3_1 = new Mode("charge3.1",4);
	static final Mode CHARGE4_1 = new Mode("charge4.1",4);
	static final Mode CHARGE5_1 = new Mode("charge5.1",4);
	static final Mode CHARGE1_2 = new Mode("charge1.2",5);
	static final Mode CHARGE2_2 = new Mode("charge2.2",5);
	static final Mode CHARGE3_2 = new Mode("charge3.2",5);
	static final Mode CHARGE4_2 = new Mode("charge4.2",5);
	static final Mode CHARGE5_2 = new Mode("charge5.2",5);
	static final Mode CHARGE1_3 = new Mode("charge1.3",6);
	static final Mode CHARGE2_3 = new Mode("charge2.3",6);
	static final Mode CHARGE3_3 = new Mode("charge3.3",6);
	static final Mode CHARGE4_3 = new Mode("charge4.3",6);
	static final Mode CHARGE5_3 = new Mode("charge5.3",6);
	static final Mode SOLID_PHASE = new Mode("solidPhase",7);
	static final Mode FREE = new Mode("free",8);

	private static final HashMap<String, Mode> MODES = new HashMap<String, Mode>();
	static
	{
		MODES.put("total",TOTAL);
		MODES.put("checkPrecip",CHECKPRECIP);
		MODES.put("adsorbent1",ADSORBENT1);
		MODES.put("adsorbent2",ADSORBENT2);
		MODES.put("adsorbent3",ADSORBENT3);
		MODES.put("adsorbent4",ADSORBENT4);
		MODES.put("adsorbent5",ADSORBENT5);
		MODES.put("adsorbent1.1",ADSORBENT1_1);
		MODES.put("adsorbent1.2",ADSORBENT1_2);
		MODES.put("adsorbent1.3",ADSORBENT1_3);
		MODES.put("charge1.1",CHARGE1_1);
		MODES.put("charge2.1",CHARGE2_1);
		MODES.put("charge3.1",CHARGE3_1);
		MODES.put("charge4.1",CHARGE4_1);
		MODES.put("charge5.1",CHARGE5_1);
		MODES.put("charge1.2",CHARGE1_2);
		MODES.put("charge2.2",CHARGE2_2);
		MODES.put("charge3.2",CHARGE3_2);
		MODES.put("charge4.2",CHARGE4_2);
		MODES.put("charge5.2",CHARGE5_2);
		MODES.put("charge1.3",CHARGE1_3);
		MODES.put("charge2.3",CHARGE2_3);
		MODES.put("charge3.3",CHARGE3_3);
		MODES.put("charge4.3",CHARGE4_3);
		MODES.put("charge5.3",CHARGE5_3);
		MODES.put("solidPhase",SOLID_PHASE);
		MODES.put("free",FREE);
	}
	
	static Mode get(String name)
	{
		return MODES.get(name);
	}
	
	private final String modeName;
	final int modeNo;
		
	Mode(String name, int number)
	{
		modeName = name;
		modeNo = number;
	}
	
	// return true for modes of form ADSORBENT? ADSORBENT1_?
	boolean isAdsorbent()
	{
		return modeNo == 3;
	}

	// return true for modes of form ADSORBENT?
	boolean isAdsorbentX()
	{
		return this == ADSORBENT1 || this == ADSORBENT2 || this == ADSORBENT3
			|| this == ADSORBENT4 || this == ADSORBENT5;
	}

	// return true for modes of form ADSORBENT1_?
	boolean isAdsorbent1_X()
	{
		return this == ADSORBENT1_1 || this == ADSORBENT1_2 
			|| this == ADSORBENT1_3;
	}

	// return true for modes of form CHARGE?_1
	boolean isChargeX_1()
	{
		return modeNo == 4;
	}

	// return true for modes of form CHARGE?_2
	boolean isChargeX_2()
	{
		return modeNo == 5;
	}

	// return true for modes of form CHARGE?_3
	boolean isChargeX_3()
	{
		return modeNo == 6;
	}
	
	public String toString()
	{
		return modeName;
	}
}