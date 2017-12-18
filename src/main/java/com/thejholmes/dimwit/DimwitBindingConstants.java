/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.thejholmes.dimwit;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DimwitBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jason Holmes - Initial contribution
 */
public class DimwitBindingConstants {
    private static final String BINDING_ID = "dimwit";

    // List of all Thing Type UIDs
    public static final ThingTypeUID ZONE_HANDLER = new ThingTypeUID(BINDING_ID, "zone");

    // Zone Channels
    public static final String CHANNEL_HIGHLEVEL = "highLevel"; // OFF/ON/MASTER
    public static final String CHANNEL_LOWLEVEL = "lowLevel"; // 0-100
}
