package org.mtransit.parser.ca_moncton_codiac_transpo_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// http://www.moncton.ca/Government/Terms_of_use/Open_Data_Purpose/Data_Catalogue/Transit_Files__GTFS_.htm
// http://www.moncton.ca/Gouvernement/Donn_es_ouvertes/Donn_es_ouvertes_-_Objectif/Catalogue_de_donn_es/Fichiers_Transit__GTFS_.htm
// http://www.moncton.ca/gtfs/google_transit.zip
public class MonctonCodiacTranspoBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-moncton-codiac-transpo-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new MonctonCodiacTranspoBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating Codiac Transpo Transit bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Codiac Transpo Transit bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final long RID_ENDS_WITH_B = 2000l;

	private static final String B = "B";

	private static final long RID_MM = 99000l;

	private static final String MM_RID = "MM";

	@Override
	public long getRouteId(GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return Long.parseLong(gRoute.getRouteShortName()); // use route short name as route ID
		}
		if (MM_RID.equalsIgnoreCase(gRoute.getRouteShortName())) {
			return RID_MM;
		}
		Matcher matcher = DIGITS.matcher(gRoute.getRouteShortName());
		matcher.find();
		long id = Long.parseLong(matcher.group());
		if (gRoute.getRouteShortName().endsWith(B)) {
			return RID_ENDS_WITH_B + id;
		}
		System.out.printf("\nUnexpected route ID for %s!\n", gRoute);
		System.exit(-1);
		return -1l;

	}

	private static final String AGENCY_COLOR_GREEN = "005238"; // GREEN (from PDF)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String RID_64B = "64B";

	private static final String COLOR_52B1B6 = "52B1B6";
	private static final String COLOR_FF0000 = "FF0000";
	private static final String COLOR_33BA16 = "33BA16";
	private static final String COLOR_0070FF = "0070FF";
	private static final String COLOR_EB8323 = "EB8323";
	private static final String COLOR_FFA77F = "FFA77F";
	private static final String COLOR_DD5344 = "DD5344";
	private static final String COLOR_8F4827 = "8F4827";
	private static final String COLOR_295185 = "295185";
	private static final String COLOR_ADAB28 = "ADAB28";
	private static final String COLOR_8F226B = "8F226B";
	private static final String COLOR_626B21 = "626B21";
	private static final String COLOR_AB8055 = "AB8055";
	private static final String COLOR_7B473F = "7B473F";
	private static final String COLOR_A428A6 = "A428A6";
	private static final String COLOR_28A855 = "28A855";
	private static final String COLOR_CE22A3 = "CE22A3";
	private static final String COLOR_428227 = "428227";
	private static final String COLOR_8585E9 = "8585E9";
	private static final String COLOR_739E97 = "739E97";

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
				int rsn = Integer.parseInt(gRoute.getRouteShortName());
				switch (rsn) {
				// @formatter:off
				case 40: return null; // agency color
				case 50: return COLOR_FF0000;
				case 51: return COLOR_33BA16; // COLOR_55FF00 to light
				case 52: return COLOR_0070FF;
				case 60: return COLOR_EB8323;
				case 61: return COLOR_FFA77F;
				case 62: return COLOR_DD5344;
				case 63: return COLOR_8F4827;
				case 64: return COLOR_295185;
				case 65: return COLOR_ADAB28;
				case 66: return COLOR_8F226B;
				case 67: return COLOR_626B21;
				case 68: return COLOR_AB8055;
				case 70: return COLOR_7B473F;
				case 71: return COLOR_A428A6;
				case 80: return COLOR_28A855;
				case 81: return COLOR_CE22A3;
				case 93: return COLOR_428227;
				case 94: return COLOR_8585E9;
				case 95: return COLOR_739E97;
				// @formatter:on
				}
			}
			if (MM_RID.equalsIgnoreCase(gRoute.getRouteShortName())) {
				return null; // agency color
			}
			if (RID_64B.equalsIgnoreCase(gRoute.getRouteShortName())) {
				return COLOR_52B1B6; // COLOR_73FFDF too light
			}
			System.out.printf("\nUnexpected route color for %s!\n", gRoute);
			System.exit(-1);
			return null;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String PLAZA_BLVD = "Plz Blvd";
	private static final String _1111_MAIN = "1111 Main";
	private static final String ELMWOOD = "Elmwood";
	private static final String CHAMPLAIN_PL = "Champlain Pl";
	private static final String CALEDONIA = "Caledonia";
	private static final String HIGHFIELD_SQ = "Highfield Sq";
	private static final String BESSBOROUGH = "Bessborough";
	private static final String CASINO = "Casino";
	private static final String GAGNON_SHEDIAC = "Gagnon / Shediac";
	private static final String KILLAM = "Killam";
	private static final String HOSPITALS = "Hospitals";
	private static final String EDINBURGH = "Edinburgh";
	private static final String KE_SPENCER_MEMORIAL_HOME = "KE Spencer Memorial Home";
	private static final String CRANDALL_U = "Crandall U";
	private static final String COLISEUM = "Coliseum";
	private static final String BRIDGEDALE = "Bridgedale";
	private static final String RIVERVIEW = "Riverview";
	private static final String ADÉLARD_SAVOIE_DIEPPE_BLVD = "Adélard-Savoie / Dieppe Blvd";
	private static final String BOURQUE_CHARTERSVILLE = "Bourque / Chartersville";
	private static final String FOX_CRK_AMIRAULT = "Fox Crk / Amirault";

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(50l, new RouteTripSpec(50l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHAMPLAIN_PL, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, PLAZA_BLVD) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810785", "6810205", "6810200" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810200", "6810202", "6810785" })) //
				.compileBothTripSort());
		map2.put(51l, new RouteTripSpec(51l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, _1111_MAIN, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, PLAZA_BLVD) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810785", "6810225", "6810234" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810234", "6810241", "6810785" })) //
				.compileBothTripSort());
		map2.put(52l, new RouteTripSpec(52l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHAMPLAIN_PL, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, _1111_MAIN) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810234", "6810253", "6810200" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810200", "6810263", "6810234" })) //
				.compileBothTripSort());
		map2.put(60l, new RouteTripSpec(60l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, _1111_MAIN, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BESSBOROUGH) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810277", "6810770", "6810286", "6810234" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810234", "6810763", "6810277" })) //
				.compileBothTripSort());
		map2.put(61l, new RouteTripSpec(61l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, ELMWOOD, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHAMPLAIN_PL) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810200", "6810303", "6810434" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810434", "6810781", "6810316", "6810200" })) //
				.compileBothTripSort());
		map2.put(62l, new RouteTripSpec(62l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, PLAZA_BLVD, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CASINO) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810324", "6810330", "6810808", "6810785" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810785", "6810916", "6810324" })) //
				.compileBothTripSort());
		map2.put(63l, new RouteTripSpec(63l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, GAGNON_SHEDIAC, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHAMPLAIN_PL) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810200", "6810347", "6810702" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810702", "6810703", "6810200" })) //
				.compileBothTripSort());
		map2.put(64l, new RouteTripSpec(64l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HOSPITALS, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, _1111_MAIN) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810234", "6810376", "6810380" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810380", "6810757", "6810234" })) //
				.compileBothTripSort());
		map2.put(64l + RID_ENDS_WITH_B, new RouteTripSpec(64l + RID_ENDS_WITH_B, // 64B
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HOSPITALS, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HIGHFIELD_SQ) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810413", "6810747", "6810401" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810401", "6810727", "6810413" })) //
				.compileBothTripSort());
		map2.put(65l, new RouteTripSpec(65l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PLAZA_BLVD, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, KILLAM) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810406", "6810906", "6810785" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810785", "6810809", "6810401", "6810406" })) //
				.compileBothTripSort());
		map2.put(66l, new RouteTripSpec(66l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CALEDONIA, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HIGHFIELD_SQ) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810413", "6810419", "6810883" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810883", "6810918", "6810413" })) //
				.compileBothTripSort());
		map2.put(67l, new RouteTripSpec(67l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, HIGHFIELD_SQ, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, EDINBURGH) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810806", "6810768", "6810413" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810413", "6810799", "6810806" })) //
				.compileBothTripSort());
		map2.put(68l, new RouteTripSpec(68l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, HIGHFIELD_SQ, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, KE_SPENCER_MEMORIAL_HOME) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810483", "6810493", "6810413" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810413", "6810471", "6810483" })) //
				.compileBothTripSort());
		map2.put(70l, new RouteTripSpec(70l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CRANDALL_U, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PLAZA_BLVD) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810785", "6810505", "6810512" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810512", "6810520", "6810785" })) //
				.compileBothTripSort());
		map2.put(71l, new RouteTripSpec(71l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, PLAZA_BLVD, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, COLISEUM) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810841", "6810537", "6810808", "6810785" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810785", "6810526", "6810841" })) //
				.compileBothTripSort());
		map2.put(80l, new RouteTripSpec(80l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HIGHFIELD_SQ, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, BRIDGEDALE) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810552", "6810561", "6810413" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810413", "6810544", "6810552" })) //
				.compileBothTripSort());
		map2.put(81l, new RouteTripSpec(81l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, HIGHFIELD_SQ, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, RIVERVIEW) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810588", "6810568", "6810413" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810413", "6810568", "6810588" })) //
				.compileBothTripSort());
		map2.put(93l, new RouteTripSpec(93l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, ADÉLARD_SAVOIE_DIEPPE_BLVD, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHAMPLAIN_PL) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810200", "6810613", "6810641" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810641", "6810880", "6810200" })) //
				.compileBothTripSort());
		map2.put(94l, new RouteTripSpec(94l, //
				MDirectionType.EAST.intValue(), MTrip.HEADSIGN_TYPE_STRING, BOURQUE_CHARTERSVILLE, //
				MDirectionType.WEST.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHAMPLAIN_PL) //
				.addTripSort(MDirectionType.EAST.intValue(), //
						Arrays.asList(new String[] { "6810200", "6810870", "6810935" })) //
				.addTripSort(MDirectionType.WEST.intValue(), //
						Arrays.asList(new String[] { "6810935", "6810627", "6810200" })) //
				.compileBothTripSort());
		map2.put(95l, new RouteTripSpec(95l, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, CHAMPLAIN_PL, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, FOX_CRK_AMIRAULT) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(new String[] { "6810672", "6810859", "6810200" })) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(new String[] { "6810200", "6810664", "6810672" })) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.id)) {
			return ALL_ROUTE_TRIPS2.get(mRoute.id).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.id)) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.id));
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.id)) {
			return; // split
		}
		String tripHeadsign = gTrip.getTripHeadsign();
		if (StringUtils.isEmpty(tripHeadsign)) {
			tripHeadsign = mRoute.getLongName();
		}
		int directionId = gTrip.getDirectionId() == null ? 0 : gTrip.getDirectionId();
		mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), directionId);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern AND = Pattern.compile("((^|\\W){1}(and)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String AND_REPLACEMENT = "$2&$4";

	private static final Pattern AT = Pattern.compile("((^|\\W){1}(at)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = "$2/$4";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = AND.matcher(gStopName).replaceAll(AND_REPLACEMENT);
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
