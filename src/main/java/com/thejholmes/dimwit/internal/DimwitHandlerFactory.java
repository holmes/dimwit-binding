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
import com.thejholmes.dimwit.DimwitBindingConstants;
import com.thejholmes.dimwit.LightZone;
import com.thejholmes.dimwit.LightZoneParser;
import com.thejholmes.dimwit.handler.DimwitZoneHandler;
import com.thejholmes.dimwit.twilight.Twilight;
import com.thejholmes.dimwit.twilight.TwilightProvider;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.osgi.service.component.ComponentContext;

import static com.thejholmes.dimwit.DimwitBindingConstants.REFRESH_RATE_SECONDS;
import static com.thejholmes.dimwit.DimwitBindingConstants.ZONE_HANDLER;
import static com.thejholmes.dimwit.DimwitBindingConstants.ZONE_JSON;
import static java.util.concurrent.TimeUnit.HOURS;

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
      new HashSet<>(Collections.singletonList(ZONE_HANDLER));

  private LightZoneParser lightZoneParser;
  private ScheduledFuture<?> refreshFuture;

  public DimwitHandlerFactory() {
  }

  @Override protected void activate(ComponentContext componentContext) {
    super.activate(componentContext);

    Dictionary<String, Object> properties = componentContext.getProperties();
    String dataPath = (String) properties.get(DimwitBindingConstants.TWILIGHT_DATA_PATH);

    Gson gson = new Gson();
    TwilightProvider twilightProvider =
        new TwilightProvider(gson, LocalDate::now, new File(dataPath));
    lightZoneParser = new LightZoneParser(gson, new Twilight(twilightProvider::twilight));

    // Refresh the zone data
    ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("thingHandler");
    refreshFuture = scheduler.scheduleWithFixedDelay(twilightProvider::refresh, 0, 8, HOURS);
  }

  @Override protected void deactivate(ComponentContext componentContext) {
    if (refreshFuture != null) {
      refreshFuture.cancel(true);
    }
    super.deactivate(componentContext);
  }

  @Override public boolean supportsThingType(ThingTypeUID thingTypeUID) {
    return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
  }

  @Override protected ThingHandler createHandler(Thing thing) {
    final Integer refreshRate = parseRefreshRate(thing);
    final LightZone lightZone = parseLightZone(thing);

    return new DimwitZoneHandler(thing, lightZone, refreshRate);
  }

  private LightZone parseLightZone(Thing thing) {
    Object rawLightZone = thing.getConfiguration().get(ZONE_JSON);
    String lightZoneJson = (String) rawLightZone;

    return lightZoneParser.parse(lightZoneJson);
  }

  private Integer parseRefreshRate(Thing thing) {
    final Integer refreshRate;
    final Object rawRefreshRate = thing.getConfiguration().get(REFRESH_RATE_SECONDS);
    if (rawRefreshRate instanceof BigDecimal) {
      refreshRate = ((BigDecimal) rawRefreshRate).intValue();
    } else if (rawRefreshRate instanceof Integer) {
      refreshRate = (Integer) rawRefreshRate;
    } else {
      refreshRate = 300;
    }
    return refreshRate;
  }
}
