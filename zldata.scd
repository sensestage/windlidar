// Data files from: https://dataplatform.knmi.nl/dataset/access/windlidar-nz-wp-platform-10min-1

// this code uses the SenseWorld quark (for the .specMap method)

//~basePath = "/home/nescivi/Documents/Projects/TheTurbinePlays/lidar-data/";

~basePath = "/media/nescivi/opslag/Projects/TheTurbinePlays/KNMI-data"


a = ZLData.new( ~basePath +/+ "ZephIR_windlidar_BSA_10min_201911210000_201911212350_v1.CSV" );

(
[
	"ZephIR_windlidar_BSA_10min_201911220000_201911222350_v1.CSV",
	"ZephIR_windlidar_BSA_10min_201911230000_201911232350_v1.CSV",
	"ZephIR_windlidar_BSA_10min_201911240000_201911242350_v1.CSV",
	"ZephIR_windlidar_BSA_10min_201911250000_201911252350_v1.CSV"
].do{ |fn| a.addFile( ~basePath +/+ fn ) };
);

// different types of data in main set and heights
ZLData.dataToIndex.keys
// different types of data for each height
ZLWindData.dataToIndex.keys;

// check the time range:
a.at( \timeDate ).first
a.at( \timeDate ).last

// plot the temperature
a.at( \temperature ) .plot;
// plot the barometric pressure
a.at( \pressure ) .plot;

// plot the windspeed of the anemometer (close to the device)
a.at( \windSpeed ) .plot;
// plot the wind direction (close to the device)
a.at( \windDirection ) .plot;


// plot wind at different heights
a.windData[63].at( \verticalWind ).plot;
a.windData[63].at( \windDirection ).plot;

a.windData[249].at( \windDirection ).plot;
a.windData[249].at( \horizontalWindMax ).plot;

a.windData[14].at( \horizontalWindMax ).plot;


( // plot average, maximum and minimum
p = a.windData[14].at( [\horizontalWind, \horizontalWindMax, \horizontalWindMin ] ).plot;
p.superpose_( true );
p.setProperties( \plotColor, [
	Color.red, Color.blue, Color.green, Color.magenta, Color.cyan, Color.yellow,
	Color.red(0.9), Color.blue(0.9), Color.green(0.9), Color.magenta(0.9), Color.cyan(0.9), Color.yellow(0.9),
] );
);

// TODO:
// if value is larger than 9000, it is probably a wrong measurement. Then replace with the last value, or interpolate to the next value

// start the audio engine
s.boot;

// example of mapping to a frequency

~range = a.windData[14].range( \horizontalWind ).asSpec;
~scale = Scale.major;
~scale.degreeToFreq( 0.3 * 12, 110, 2 );
~freqRange = [ 110, 1100, \exp ].asSpec;

(
Tdef( \playData, {
	a.windData[14].at( \horizontalWind ).do{ |it|
		// [ it, ~range.unmap( it ), ~scale.degreeToFreq( ~range.unmap( it ) * 12, 110, 1 ), it.specMap( ~range, ~freqRange ) ].postln;
		// ( dur: 0.2, freq:  ~scale.degreeToFreq( ~range.unmap( it ) * 12, 55, 0 ) ).play;
		( dur: 0.1, freq: it.specMap( ~range, ~freqRange ) ).play;
		0.2.wait;
	};
}).play;
);

s.dumpOSC( 0 );

Tdef( \playData ).stop;
Tdef( \playData ).play;
