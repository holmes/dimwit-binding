/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.thejholmes.dimwit.handler;

import com.thejholmes.dimwit.LightLevels;
import com.thejholmes.dimwit.LightZone;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.thejholmes.dimwit.DimwitBindingConstants.CHANNEL_HIGHLEVEL;
import static com.thejholmes.dimwit.DimwitBindingConstants.CHANNEL_LOWLEVEL;

/**
 * This does the actual refreshing and updating of LightZones based on the current time.
 *
 * @author Jason Holmes - Initial contribution
 */
public class DimwitZoneHandler extends BaseThingHandler {
  private final Logger logger = LoggerFactory.getLogger(DimwitZoneHandler.class);
  private final LightZone lightZone;
  private final Observable<LightLevels> levels;
  private CompositeDisposable disposable;

  // Used for caching last levels when a refresh is requested.
  private PercentType lowLevel;
  private PercentType highLevel;

  public DimwitZoneHandler(Thing thing, LightZone lightZone) {
    super(thing);
    this.disposable = new CompositeDisposable();
    this.lightZone = lightZone;
    this.levels = lightZone.getLightLevels();

    this.lowLevel = PercentType.ZERO;
    this.highLevel = PercentType.HUNDRED;
  }

  @SuppressWarnings("deprecation") // not sure what else to do here.
  @Override public void initialize() {
    this.updateStatus(ThingStatus.ONLINE);

    disposable.dispose();
    disposable = new CompositeDisposable();

    disposable.add( //
        levels.subscribe(levels -> {
          logger.debug("Observed {} @ {} to {}/{}", lightZone.getDeviceId(), levels.getNow(),
              levels.getLowLevel(), levels.getHighLevel());

          lowLevel = new PercentType(levels.getLowLevel());
          highLevel = new PercentType(levels.getHighLevel());
          updateState();
        }));
  }

  @Override public void dispose() {
    disposable.dispose();
    super.dispose();
  }

  @Override public void handleCommand(ChannelUID channelUID, Command command) {
    logger.debug("Dimwit is being told to refresh", command);
    if (command instanceof RefreshType) {
      updateState();
    }
  }

  private void updateState() {
    logger.debug("Updating {} to {}/{}", lightZone.getDeviceId(), lowLevel, highLevel);
    updateState(CHANNEL_LOWLEVEL, lowLevel);
    updateState(CHANNEL_HIGHLEVEL, highLevel);
  }
}
