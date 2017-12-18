/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.thejholmes.dimwit.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This does the actual communicating to the Russound device from the various Things that represent
 * the zones.
 *
 * @author Jason Holmes - Initial contribution
 */
public class DimwitZoneHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DimwitZoneHandler.class);

    public DimwitZoneHandler(Thing thing) {
        super(thing);
    }

    @Override public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handling a command");
    }
}
