package de.wfb.rail.service;

public final class RouteUtils {

	private RouteUtils() {

	}

	public static boolean isEmpty(final Route route) {

		if (route == null) {
			return true;
		}

		return route.isEmpty();
	}

	public static boolean isNotEmpty(final Route route) {

		return !RouteUtils.isEmpty(route);
	}

}
