/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.thejholmes.dimwit.internal;

import com.google.gson.Gson;
import com.thejholmes.dimwit.LightZoneManager;
import com.thejholmes.dimwit.handler.DimwitZoneHandler;
import java.io.File;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
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
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.dimwit", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class DimwitHandlerFactory extends BaseThingHandlerFactory {
  private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS =
      new HashSet<>(Collections.singletonList(ZONE_HANDLER));

  private final Logger logger = LoggerFactory.getLogger(DimwitZoneHandler.class);
  private LightZoneManager lightZoneManager;

  public DimwitHandlerFactory() {
  }

  @Override protected void activate(ComponentContext componentContext) {
    super.activate(componentContext);

    Dictionary<String, Object> properties = componentContext.getProperties();
    //String dataPath = (String) properties.get(DimwitBindingConstants.TWILIGHT_DATA_PATH);
    String dataPath = "/opt/sunrise-data/data";

    lightZoneManager = new LightZoneManager(new Gson(), new File(dataPath));
    lightZoneManager.start();
  }

  @Override protected void deactivate(ComponentContext componentContext) {
    lightZoneManager.stop();
    super.deactivate(componentContext);
  }

  @Override public boolean supportsThingType(ThingTypeUID thingTypeUID) {
    return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
  }

  @Override protected ThingHandler createHandler(Thing thing) {
    return new DimwitZoneHandler(thing, lightZoneManager.getParser());
  }
}
