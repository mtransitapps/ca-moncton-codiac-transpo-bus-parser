package org.mtransit.parser.ca_moncton_codiac_transpo_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// https://open.moncton.ca/datasets/transit-files-gtfs
// https://www7.moncton.ca/opendata/google_transit.zip
public class MonctonCodiacTranspoBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new MonctonCodiacTranspoBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "Codiac Transpo";
	}

	@NotNull
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
	private static final long RID_METS = 99_001L;

	private static final String MM_RID = "MM";
	private static final String METS_RID = "METS";

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		final String rsn = gRoute.getRouteShortName().toLowerCase(Locale.ENGLISH);
		if (CharUtils.isDigitsOnly(rsn)) {
			return Long.parseLong(rsn); // use route short name as route ID
		}
		if (MM_RID.equalsIgnoreCase(rsn)) {
			return RID_MM;
		} else if (METS_RID.equalsIgnoreCase(rsn)) {
			return RID_METS;
		}
		final Matcher matcher = DIGITS.matcher(rsn);
		if (matcher.find()) {
			final long id = Long.parseLong(matcher.group());
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
		throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute.toStringPlus());
	}

	@Override
	public boolean mergeRouteLongName(@NotNull MRoute mRoute, @NotNull MRoute mRouteToMerge) {
		throw new MTLog.Fatal("Unexpected routes to merge %s & %s!", mRoute, mRouteToMerge);
	}

	private static final String AGENCY_COLOR_GREEN = "005238"; // GREEN (from PDF)

	private static final String AGENCY_COLOR = AGENCY_COLOR_GREEN;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		if (StringUtils.isEmpty(gRoute.getRouteColor())) {
			final long routeId = getRouteId(gRoute);
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
			} else if (RID_METS == routeId) { // METS
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
			throw new MTLog.Fatal("Unexpected route color for %s!", gRoute.toStringPlus());
		}
		return super.getRouteColor(gRoute);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public List<Integer> getDirectionTypes() {
		return Arrays.asList(
				MTrip.HEADSIGN_TYPE_DIRECTION,
				MTrip.HEADSIGN_TYPE_STRING
		);
	}

	@NotNull
	@Override
	public String cleanDirectionHeadsign(boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = super.cleanDirectionHeadsign(fromStopName, directionHeadSign);
		directionHeadSign = CleanUtils.cleanBounds(directionHeadSign); // only kept EN for now
		return directionHeadSign;
	}

	private static final Pattern AVENIR_CENTER_ = CleanUtils.cleanWords("Avenir Centre Avenir");
	private static final String AVENIR_CENTER_REPLACEMENT = CleanUtils.cleanWordsReplacement("Avenir Ctr");

	private static final Pattern CF_CHAMPLAIN_ = CleanUtils.cleanWords("cf champlaim");
	private static final String CF_CHAMPLAIN_REPLACEMENT = CleanUtils.cleanWordsReplacement("CF Champlain");

	private static final Pattern NORTH_PLAZA_ = CleanUtils.cleanWords("north plaza nord", "north plz nord");
	private static final String NORTH_PLAZA_REPLACEMENT = CleanUtils.cleanWordsReplacement("North Plz");

	private static final Pattern SOUTH_PLAZA_ = CleanUtils.cleanWords("south plaza sud", "south plz sud");
	private static final String SOUTH_PLAZA_REPLACEMENT = CleanUtils.cleanWordsReplacement("South Plz");

	private static final Pattern WEST_MONCTON_ = CleanUtils.cleanWord("west Moncton ouest");
	private static final String WEST_MONCTON_REPLACEMENT = CleanUtils.cleanWordsReplacement("West Moncton");

	private static final Pattern EAST_MONCTON_ = CleanUtils.cleanWord("east Moncton est");
	private static final String EAST_MONCTON_REPLACEMENT = CleanUtils.cleanWordsReplacement("East Moncton");

	private static final Pattern COLISEUM_ = Pattern.compile("(coliseum - colisée)", Pattern.CASE_INSENSITIVE);
	private static final String COLISEUM_REPLACEMENT = "Coliseum"; // FIXME support for head-sign string i18n

	private static final Pattern HOSP_ = Pattern.compile("(hospitals - hôpitaux)", Pattern.CASE_INSENSITIVE);
	private static final String HOSP_REPLACEMENT = "Hospitals"; // FIXME support for head-sign string i18n

	private static final Pattern STARTS_W_SHUTTLE_FOR_ = Pattern.compile("(^shuttle for )", Pattern.CASE_INSENSITIVE);

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = AVENIR_CENTER_.matcher(tripHeadsign).replaceAll(AVENIR_CENTER_REPLACEMENT);
		tripHeadsign = CF_CHAMPLAIN_.matcher(tripHeadsign).replaceAll(CF_CHAMPLAIN_REPLACEMENT);
		tripHeadsign = WEST_MONCTON_.matcher(tripHeadsign).replaceAll(WEST_MONCTON_REPLACEMENT);
		tripHeadsign = EAST_MONCTON_.matcher(tripHeadsign).replaceAll(EAST_MONCTON_REPLACEMENT);
		tripHeadsign = NORTH_PLAZA_.matcher(tripHeadsign).replaceAll(NORTH_PLAZA_REPLACEMENT);
		tripHeadsign = SOUTH_PLAZA_.matcher(tripHeadsign).replaceAll(SOUTH_PLAZA_REPLACEMENT);
		tripHeadsign = COLISEUM_.matcher(tripHeadsign).replaceAll(COLISEUM_REPLACEMENT);
		tripHeadsign = HOSP_.matcher(tripHeadsign).replaceAll(HOSP_REPLACEMENT);
		tripHeadsign = STARTS_W_SHUTTLE_FOR_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern UNIVERSITY_ENCODING = Pattern.compile("((^|\\W)(universit�)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String UNIVERSITY_ENCODING_REPLACEMENT = "$2" + "University" + "$4";

	private static final Pattern ADELARD_ENCODING = Pattern.compile("((^|\\W)(ad�lard)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String ADELARD_ENCODING_REPLACEMENT = "$2" + "Adelard" + "$4";

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = UNIVERSITY_ENCODING.matcher(gStopName).replaceAll(UNIVERSITY_ENCODING_REPLACEMENT);
		gStopName = ADELARD_ENCODING.matcher(gStopName).replaceAll(ADELARD_ENCODING_REPLACEMENT);
		gStopName = CleanUtils.cleanSlashes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		final String stopCode = gStop.getStopCode();
		if (stopCode.length() > 0 && CharUtils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode);
		}
		final Matcher matcher = DIGITS.matcher(stopCode);
		if (matcher.find()) {
			final int digits = Integer.parseInt(matcher.group());
			//noinspection ConstantConditions
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
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
	}
}
