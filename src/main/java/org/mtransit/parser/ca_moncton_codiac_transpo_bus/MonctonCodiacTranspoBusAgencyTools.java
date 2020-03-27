package org.mtransit.parser.ca_moncton_codiac_transpo_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
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

// https://catalogue-moncton.opendata.arcgis.com/datasets/transit-files-gtfs
// https://www7.moncton.ca/opendata/google_transit.zip
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
		MTLog.log("Generating Codiac Transpo bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this, true);
		super.start(args);
		MTLog.log("Generating Codiac Transpo bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
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
	public boolean excludeRoute(GRoute gRoute) {
		return super.excludeRoute(gRoute);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	private static final long RID_ENDS_WITH_A = 10_000L;
	private static final long RID_ENDS_WITH_B = 20_000L;
	private static final long RID_ENDS_WITH_C = 30_000L;
	private static final long RID_ENDS_WITH_D = 40_000L;
	private static final long RID_ENDS_WITH_P = 160_000L;
	private static final long RID_ENDS_WITH_S = 190_000L;
	//
	private static final long RID_ENDS_WITH_C1 = 27L * 10_000L;
	private static final long RID_ENDS_WITH_C2 = 28L * 10_000L;
	private static final long RID_ENDS_WITH_LT = 29L * 10_000L;
	private static final long RID_ENDS_WITH_LTS = 30L * 10_000L;

	private static final String A = "a";
	private static final String B = "b";
	private static final String C = "c";
	private static final String D = "d";
	private static final String P = "p";
	private static final String S = "s";
	//
	private static final String C1 = "c1";
	private static final String C2 = "c2";
	private static final String LT = "lt";
	private static final String LTS = "lts";

	private static final long RID_MM = 99_000L;

	private static final String MM_RID = "MM";

	@Override
	public long getRouteId(GRoute gRoute) {
		String rsn = gRoute.getRouteShortName().toLowerCase(Locale.ENGLISH);
		if (Utils.isDigitsOnly(rsn)) {
			return Long.parseLong(rsn); // use route short name as route ID
		}
		if (MM_RID.equalsIgnoreCase(rsn)) {
			return RID_MM;
		}
		Matcher matcher = DIGITS.matcher(rsn);
		if (matcher.find()) {
			long id = Long.parseLong(matcher.group());
			if (rsn.endsWith(LTS)) {
				return RID_ENDS_WITH_LTS + id;
			} else if (rsn.endsWith(LT)) {
				return RID_ENDS_WITH_LT + id;
			} else if (rsn.endsWith(A)) {
				return RID_ENDS_WITH_A + id;
			} else if (rsn.endsWith(B)) {
				return RID_ENDS_WITH_B + id;
			} else if (rsn.endsWith(C)) {
				return RID_ENDS_WITH_C + id;
			} else if (rsn.endsWith(D)) {
				return RID_ENDS_WITH_D + id;
			} else if (rsn.endsWith(P)) {
				return RID_ENDS_WITH_P + id;
			} else if (rsn.endsWith(S)) {
				return RID_ENDS_WITH_S + id;
			} else if (rsn.endsWith(C1)) {
				return RID_ENDS_WITH_C1 + id;
			} else if (rsn.endsWith(C2)) {
				return RID_ENDS_WITH_C2 + id;
			}
		}
		MTLog.logFatal("Unexpected route ID for %s!", gRoute);
		return -1L;
	}

	@Override
	public boolean mergeRouteLongName(MRoute mRoute, MRoute mRouteToMerge) {
		MTLog.logFatal("Unexpected routes to merge %s & %s!", mRoute, mRouteToMerge);
		return false;
	}

	private static final String AGENCY_COLOR_GREEN = "005238"; // GREEN (from PDF)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			long routeId = getRouteId(gRoute);
			switch ((int) routeId) {
			// @formatter:off
			case 50: return "ED1D24";
			case 51: return "00A651";
			case 52: return "0072BC";
			case 60: return "E977AF";
			case 61: return "684287";
			case 62: return "DC62A4";
			case 63: return "F7941E";
			case 64: return "A6664C";
			case 65: return "FBAF34";
			case 66: return "65A6BB";
			case 67: return "2E3092";
			case 68: return "00AEEF";
			case 70: return "3EC7F4";
			case 71: return "8DC63F";
			case 72: return "8DC63F";
			case 73: return "6A3B0C";
			case 75: return "732600";
			case 80: return "CF8B2D";
			case 81: return "942976";
			case 82: return "FDCC08";
			case 83: return "B63030";
			case 93: return "8FB73E";
			case 94: return "41827C";
			case 95: return "F58473";
			case 939495: return null; // agency color
			// @formatter:on
			}
			if (RID_MM == routeId) { // MM
				return null; // agency color
			} else if (60L + RID_ENDS_WITH_LT == routeId) {
				return "E977AF"; // same as 60
			} else if (60L + RID_ENDS_WITH_LTS == routeId) {
				return "E977AF"; // same as 60
			} else if (60_67L + RID_ENDS_WITH_C == routeId) { // 6067C
				return null; // agency color
			} else if (61L + RID_ENDS_WITH_B == routeId) { // 61B
				return "B0A0C5";
			} else if (6851L + RID_ENDS_WITH_D == routeId) { // 6851D
				return null;
			} else if (80_81L + RID_ENDS_WITH_C1 == routeId) { // 8081C1
				return null; // agency color
			} else if (80_81L + RID_ENDS_WITH_C2 == routeId) { // 8081C2
				return null; // agency color
			} else if (81L + RID_ENDS_WITH_S == routeId) { // 81S
				return "942976"; // same as 81
			} else if (93L + RID_ENDS_WITH_A == routeId) { // 93A
				return "A94D3F"; // same as 93
			}
			MTLog.logFatal("Unexpected route color for %s!", gRoute);
			return null;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String PLAZA_BLVD = "Plz Blvd";
	private static final String _140_MILLENNIUM = "140 Millennium";
	private static final String _1111_MAIN = "1111 Main";
	private static final String ELMWOOD = "Elmwood";
	private static final String CHAMPLAIN_PL = "Champlain Pl";
	private static final String CALEDONIA = "Caledonia";
	private static final String HIGHFIELD_SQ = "Highfield Sq";
	private static final String BESSBOROUGH = "Bessborough";
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
	private static final String SALISBURY_RD = "Salisbury Rd";
	private static final String FOX_CRK_AMIRAULT = "Fox Crk / Amirault";
	private static final String MOUNTAIN = "Mountain";
	private static final String LAKEBURN = "Lakeburn";

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
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
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 61L + RID_ENDS_WITH_A) { // 61A
			if (Arrays.asList( //
					"Elmwood Dr & Donald Ave", //
					"CF Champlain" //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("CF Champlain", mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 64L) { //
			if (Arrays.asList( //
					"Ctr Hospitalier Universaire", //
					"1111 Main" //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("1111 Main", mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 65L) { //
			if (Arrays.asList( //
					"Killam Dr & Purdy Ave", //
					"North Plz" //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("North Plz", mTrip.getHeadsignId());
				return true;
			}
		}
		if (mTrip.getRouteId() == 82L) { //
			if (Arrays.asList( //
					"Riverview Pl Routing", //
					"Gunningsville" //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Gunningsville", mTrip.getHeadsignId());
				return true;
			}
		}
		MTLog.logFatal("Unexpected trips to merge %s & %s!", mTrip, mTripToMerge);
		return false;
	}

	private static final Pattern TOWARDS = Pattern.compile("((^|\\W){1}(towards)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	private static final Pattern AVENIR_CENTER_ = CleanUtils.cleanWords("Avenir Centre Avenir");
	private static final String AVENIR_CENTER_REPLACEMENT = CleanUtils.cleanWordsReplacement("Avenir Ctr");

	private static final Pattern CF_CHAMPLAIN_ = CleanUtils.cleanWords("cf champlaim");
	private static final String CF_CHAMPLAIN_REPLACEMENT = CleanUtils.cleanWordsReplacement("CF Champlain");

	private static final Pattern NORTH_PLAZA_ = CleanUtils.cleanWords("north plaza nord");
	private static final String NORTH_PLAZA_REPLACEMENT = CleanUtils.cleanWordsReplacement("North Plz");

	private static final Pattern SOUTH_PLAZA_ = CleanUtils.cleanWords("south plaza sud");
	private static final String SOUTH_PLAZA_REPLACEMENT = CleanUtils.cleanWordsReplacement("South Plz");

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		Matcher matcherTOWARDS = TOWARDS.matcher(tripHeadsign);
		if (matcherTOWARDS.find()) {
			String gTripHeadsignAfterTOWARDS = tripHeadsign.substring(matcherTOWARDS.end());
			tripHeadsign = gTripHeadsignAfterTOWARDS;
		}
		tripHeadsign = AVENIR_CENTER_.matcher(tripHeadsign).replaceAll(AVENIR_CENTER_REPLACEMENT);
		tripHeadsign = CF_CHAMPLAIN_.matcher(tripHeadsign).replaceAll(CF_CHAMPLAIN_REPLACEMENT);
		tripHeadsign = NORTH_PLAZA_.matcher(tripHeadsign).replaceAll(NORTH_PLAZA_REPLACEMENT);
		tripHeadsign = SOUTH_PLAZA_.matcher(tripHeadsign).replaceAll(SOUTH_PLAZA_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern UNIVERSITY_ENCODING = Pattern.compile("((^|\\W){1}(universit�)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String UNIVERSITY_ENCODING_REPLACEMENT = "$2University$4";

	private static final Pattern ADELARD_ENCODING = Pattern.compile("((^|\\W){1}(ad�lard)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String ADELARD_ENCODING_REPLACEMENT = "$2Adelard$4";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = UNIVERSITY_ENCODING.matcher(gStopName).replaceAll(UNIVERSITY_ENCODING_REPLACEMENT);
		gStopName = ADELARD_ENCODING.matcher(gStopName).replaceAll(ADELARD_ENCODING_REPLACEMENT);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.removePoints(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(GStop gStop) {
		String stopCode = gStop.getStopCode();
		if (Utils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode);
		}
		Matcher matcher = DIGITS.matcher(stopCode);
		if (matcher.find()) {
			int digits = Integer.parseInt(matcher.group());
			if (true) { // LIKE BEFORE
				return 6_810_000 + digits;
			}
			if (stopCode.startsWith("D")) {
				return 40_000 + digits;
			} else if (stopCode.startsWith("M")) {
				return 130_000 + digits;
			} else if (stopCode.startsWith("R")) {
				return 180_000 + digits;
			}
		}
		MTLog.logFatal("Unexpected stop ID for %s!", gStop);
		return -1;
	}
}
