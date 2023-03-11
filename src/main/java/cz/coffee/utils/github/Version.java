/**
 * This file is part of skJson.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */
package cz.coffee.utils.github;

import static cz.coffee.utils.ErrorHandler.Level.INFO;
import static cz.coffee.utils.ErrorHandler.sendMessage;

@SuppressWarnings("unused")
public class Version {

    private final static int STATIC_VERSION = 1165;
    private static int serverVersion;

    public Version(String version) {
        serverVersion = Integer.parseInt(version.split("-")[0].replaceAll("[.]", ""));
    }

    public void check() {
        if (serverVersion > STATIC_VERSION) {
            sendMessage(serverVersion, INFO);
        }
    }

    public boolean isLegacy() {
        return serverVersion <= STATIC_VERSION;
    }

    public int getServerVersion() {
        return serverVersion;
    }

}