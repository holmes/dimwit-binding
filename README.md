## Dimwit Binding

#### What is it?
Lights can be bright. Each room in my house is controlled by a dimmer. I hate having to change the
dimmer value. I need it low in the morning and high in the afternoon. And if I'm vegging out on the
couch I'd like it to be dimmer at 10PM than it was at 6PM when I turned on the lights.

In fact, wouldn't it be amazing if the lights turned on and dimmed themselves automatically based
on the time of day, when sunset and sunrise were? And if you could control each room individually? Well
now you can - home automation to the rescue!  

You can use this binding to create a Thing for each light zone in your house. Once configured, it'll 
update 2 channels (high & low) with calculated light levels.

What are these light levels? The low level is your ideal light situation for that room at any given
time. In the mornings and evening you probably want it low - in the afternoon when the sun is at it's
peak you probably want it high. Between those times you'd like it between those values.

#### How does it work?
There are 3 parts to this binding. You need to set up sunrise/sunset/solarNoon, you need to configure
each zone, and you need to apply the changes via a rule.

1. Currently this is hardcoded to look for values based on data scraped from https://sunrise-sunset.org/.
Hooking this up to an Astro plugin probably wouldn't be that hard, but I already had this data and didn't
feel like doing it. <p>
These files contain twilightBegin/End, sunriseBegin/End & solarNoon for my location. Fresh data is loaded
when we cross date boundaries so your light levels automatically adjust as days get longer and shorter.<p>
To use this you've got to have these files located in `/opt/sunrise/data`. I should probably make that 
configurable.

1. You need to configure a zone. Configuration is very easy, it's just a json object w/ times and levels.
The TimeFrames specify an end time. These build upon each other, so you can think of the endTime being
from the end of the previous zone to this zone's endTime. The low levels are the basis of what's calculated.
The high level is what we'd want to switch to if the light switch was tapped on again.
```text
{
  "deviceId": "Nursery",
  "timeFrames": [
    {
      "endTime": "7:00",
      "lowLevel": 1,
      "highLevel": 60
    },
    {
      "endTime": "solarNoon:0",
      "lowLevel": 80,
      "highLevel": 100
    },
    {
      "endTime": "twilightEnd:0",
      "lowLevel": 50,
      "highLevel": 80
    },
    {
      "endTime": "23:59",
      "lowLevel": 5,
      "highLevel": 40
    }
  ]
}
```

1. Finally, you need to apply the changes. If you want to auto dim the lights you just need to set the
values when the item changes. If you have switches that handle instant notification (Homeseer) then you 
can set the light value immediately when the switch is tapped.


### Sample Code
  
#### Things
```text
dimwit:room:family_room_levels "Family Room - Light Levels" @ "Family Room" [ 
    zoneJson="{\"deviceId\": \"FamilyRoom\", \"timeFrames\": [{\"endTime\": \"sunrise:0\", \"lowLevel\": 1, \"highLevel\": 35}, {\"endTime\": \"sunrise:60\", \"lowLevel\": 30, \"highLevel\": 60}, {\"endTime\": \"solarNoon:0\", \"lowLevel\": 60, \"highLevel\": 85}, {\"endTime\": \"twilightEnd:0\", \"lowLevel\": 30, \"highLevel\": 60}, {\"endTime\": \"23:59\", \"lowLevel\": 1, \"highLevel\": 35}]}"
    ]
```

#### Items
```text
Dimmer family_room_low_level "Family Room - Light Level Low" { channel="dimwit:room:family_room_levels:low"}
Dimmer family_room_high_level "Family Room - Light Level High" { channel="dimwit:room:family_room_levels:high"}
Dimmer family_room_dimmer "Family Room Dimmer Switch" <slider> (Dimmers) { channel="zwave:device:34770927:node13:switch_dimmer" }
```

#### Rules - Toggle when light is tapped
```text
val org.eclipse.xtext.xbase.lib.Functions$Function3<DimmerItem, DimmerItem, DimmerItem, Void> newToggleLevel = [
	DimmerItem dimmer, DimmerItem lowLevel, DimmerItem highLevel |
	val Number currentLevel = dimmer.state as Number
	logInfo("RULE.TOGGLER", "{} is currently at: {}", dimmer.label, currentLevel)

	var DimmerItem lightLevel
	if (currentLevel == 0) {
		lightLevel = lowLevel
	} else if (currentLevel < lowLevel.state as Number) {
		lightLevel = lowLevel
	} else if (currentLevel < highLevel.state as Number ) {
		lightLevel = highLevel
	} else {
		lightLevel = lowLevel
	}

	
	if (currentLevel != 0) {
		logInfo("RULE.TOGGLER", "Sleeping for 250ms since the light is already on")	
		Thread::sleep(250)
	}
	
	logInfo("RULE.TOGGLER", "About to set {} to {}", dimmer.label, lightLevel)	
	dimmer.sendCommand(lightLevel.state.toString)
]

rule "Family Room Sitemap Proxy Action"
when
	Item family_room_dimmer received update
then {
	if (family_room_dimmer_proxy.state == ON) {
		newToggleLevel.apply(family_room_dimmer, family_room_low_level, family_room_high_level)
	} else {
		family_room_dimmer.sendCommand(OFF)
	}
} end
```

#### Rules - AutoDim
```text
val org.eclipse.xtext.xbase.lib.Functions$Function2<DimmerItem, String, Void> newAutoDim = [
	DimmerItem dimmer, DimmerItem lowLevelItem |
	val Number currentLevel = dimmer.state as Number
	val Number lowLevel = lowLevelItem.state as Number
	logInfo("RULE.AUTODIM", "{} is currently at: {}", dimmer.label, currentLevel)

	if (currentLevel == 0) {
		return null;
	}

	if (Math.abs(lowLevel.intValue - currentLevel.intValue) <= 2) {
		logInfo("RULE.AUTODIM", "About to set {} to {}", dimmer.label, lowLevel)	
		dimmer.sendCommand(lowLevelItem.state.toString)
	}
]

rule "AutoDim Family Room Lights"
when 
	Item family_room_low_level received update
then {
	newAutoDim.apply(family_room_dimmer, family_room_low_level)
}
end
```
