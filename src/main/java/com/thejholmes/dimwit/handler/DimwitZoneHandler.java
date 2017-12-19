/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.thejholmes.dimwit.handler;

import com.thejholmes.dimwit.DimwitBindingConstants;
import com.thejholmes.dimwit.LightZone;
import java.time.LocalTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import kotlin.jvm.functions.Function0;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This does the actual refreshing and updating of LightZones based on the current time.
 *
 * @author Jason Holmes - Initial contribution
 */
public class DimwitZoneHandler extends BaseThingHandler {
  private final Runnable refreshRunnable = this::handleRefresh;
  private final LightZone lightZone;
  private final Integer refreshRate;

  private ScheduledFuture<?> refreshFuture;

  public DimwitZoneHandler(Thing thing, LightZone lightZone, Integer refreshRate) {
    super(thing);
    this.lightZone = lightZone;
    this.refreshRate = refreshRate;
  }

  @Override public void initialize() {
    refreshFuture = scheduler.scheduleAtFixedRate(refreshRunnable, 0, refreshRate, SECONDS);
    this.updateStatus(ThingStatus.ONLINE);
  }

  @Override public void dispose() {
    if (refreshFuture != null) {
      refreshFuture.cancel(true);
    }
  }

  @Override public void handleCommand(ChannelUID channelUID, Command command) {
    if (command instanceof RefreshType) {
      handleRefresh();
    }

    throw new IllegalArgumentException("Dimwit Binding only handles refreshes");
  }

  private void handleRefresh() {
    Function0<LocalTime> now = LocalTime::now;
    int lowLevel = lightZone.calculateLightLevel(now);
    int highLevel = lightZone.highLevel(now);

    updateState(DimwitBindingConstants.CHANNEL_LOWLEVEL, new PercentType(lowLevel));
    updateState(DimwitBindingConstants.CHANNEL_HIGHLEVEL, new PercentType(highLevel));
  }
}
