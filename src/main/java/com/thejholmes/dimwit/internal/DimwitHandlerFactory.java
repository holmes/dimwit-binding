/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.thejholmes.dimwit.internal;

import com.thejholmes.dimwit.handler.DimwitZoneHandler;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.thejholmes.dimwit.DimwitBindingConstants.ZONE_HANDLER;

/**
 * The {@link DimwitHandlerFactory} is responsible for creating things and thing handlers.
 *
 * There is one DimwitZoneHandler per binding, and one RussoundSerialZoneHandler
 * created per zone.
 *
 * @author Jason Holmes - Initial contribution
 */
public class DimwitHandlerFactory extends BaseThingHandlerFactory {
  private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS =
      new HashSet<>(Arrays.asList(ZONE_HANDLER));

  private final Logger logger = LoggerFactory.getLogger(DimwitHandlerFactory.class);

  @Override public boolean supportsThingType(ThingTypeUID thingTypeUID) {
    return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
  }

  @Override protected ThingHandler createHandler(Thing thing) {
    logger.debug("creating ThingHandler for thing: {}", thing);
    return new DimwitZoneHandler(thing);
  }
}
