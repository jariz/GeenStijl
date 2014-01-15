/*
 * Copyright 2014 Jari Zwarts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*******************************************************************************
 * This file is part of RedReader.
 *
 * RedReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See thez
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RedReader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package io.jari.geenstijl.API;

import android.content.Context;
import android.content.res.Resources;

/**
 * Modified time class from the RedReader project (excellent reddit reader)
 */
public class RRTime {
    static Resources resources;
    static String packageName;

	public static String formatDurationMs(final long totalMs, Context context) {
		long ms = totalMs;
        RRTime.resources = context.getResources();
        RRTime.packageName = context.getPackageName();

		final long years = ms / (365L * 24L * 60L * 60L * 1000L);
		ms %= (365L * 24L * 60L * 60L * 1000L);

		final long months = ms / (30L * 24L * 60L * 60L * 1000L);
		ms %= (30L * 24L * 60L * 60L * 1000L);

		if(years > 0) {
			if(months > 0) {
				return String.format("%d %s, %d %s", years, s("year", years), months, s("month", months));
			} else {
				return String.format("%d %s", years, s("year", years));
			}
		}

		final long days = ms / (24L * 60L * 60L * 1000L);
		ms %= (24L * 60L * 60L * 1000L);

		if(months > 0) {
			if(days > 0) {
				return String.format("%d %s, %d %s", months, s("month", months), days, s("day", days));
			} else {
				return String.format("%d %s", months, s("month", months));
			}
		}

		final long hours = ms / (60L * 60L * 1000L);
		ms %= (60L * 60L * 1000L);

		if(days > 0) {
			if(hours > 0) {
				return String.format("%d %s, %d %s", days, s("day", days), hours, s("hour", hours));
			} else {
				return String.format("%d %s", days, s("day", days));
			}
		}

		final long mins = ms / (60L * 1000L);
		ms %= (60L * 1000L);

		if(hours > 0) {
			if(mins > 0) {
				return String.format("%d %s, %d %s", hours, s("hour", hours), mins, s("min", mins));
			} else {
				return String.format("%d %s", hours, s("hour", hours));
			}
		}

		final long secs = ms / 1000;
		ms %= 1000;

		if(mins > 0) {
			if(secs > 0) {
				return String.format("%d %s, %d %s", mins, s("min", mins), secs, s("sec", secs));
			} else {
				return String.format("%d %s", mins, s("min", mins));
			}
		}

		if(secs > 0) {
			if(ms > 0) {
				return String.format("%d %s, %d %s", secs, s("sec", secs), ms, "ms");
			} else {
				return String.format("%d %s", secs, s("sec", secs));
			}
		}

		return ms + " ms";
	}

	private static String s(final String str, final long n) {
		if(n == 1) return resources.getString(resources.getIdentifier(str, "string", packageName));
        return resources.getString(resources.getIdentifier(str+"s", "string", packageName));
	}
}