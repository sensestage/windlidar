// future TODO: allow multiple files to be added

ZLData {
	classvar <dataToIndex;

	var <dataFiles;

	var <meteoData;
	var <windData;

	*initClass{
		dataToIndex = IdentityDictionary.new;
		dataToIndex.put( \timeDate, 1 );
		dataToIndex.put( \timeStamp, 2 );
		dataToIndex.put( \upperTemperature, 7 );
		dataToIndex.put( \lowerTemperature, 8 );
		dataToIndex.put( \podHumidity, 9 );
		dataToIndex.put( \GPS, 10 );
		dataToIndex.put( \compass, 11 );
		dataToIndex.put( \tilt, 12 );
		dataToIndex.put( \temperature, 13 );
		dataToIndex.put( \pressure, 14 );
		dataToIndex.put( \windSpeed, 16 );
		dataToIndex.put( \windDirection, 17 );
		dataToIndex.put( \percentageRain, 18 );

		dataToIndex.put( \h249, 19 );
		dataToIndex.put( \h199, 27 );
		dataToIndex.put( \h179, 35 );
		dataToIndex.put( \h156, 43 );
		dataToIndex.put( \h145, 51 );
		dataToIndex.put( \h119, 59 );
		dataToIndex.put( \h94, 67 );
		dataToIndex.put( \h75, 75 );
		dataToIndex.put( \h63, 83 );
		dataToIndex.put( \h38, 91 );
		dataToIndex.put( \h14, 99 );
	}

	*new{ |fn|
		^super.new.init( fn );
	}

	init{ |fn|
		dataFiles = List.new;
		if ( fn.notNil ){
			this.addFile( fn );
		}
	}

	at{ |key|
		if ( key.isSequenceableCollection ){ ^this.atAll( key ) };
		^meteoData.at( ZLData.dataToIndex.at( key ) );
	}

	atAll { |keys|
		^keys.collect{ |k| k.postln; this.at( k ) }
	}

	addFile{ |fn|
		var newMeteoData, newWindData, dataFile;
		dataFile = ZLDataFile.new( fn );
		dataFiles.add( dataFile );

		// parse meteo data
		newMeteoData = dataFile.getMeteoData;
		if ( meteoData.isNil ){
			meteoData = newMeteoData;
		}{
			meteoData = (meteoData.flop ++ newMeteoData.flop).flop;
		};

		// parse wind data
		newWindData = dataFile.getWindData;
		if ( windData.isNil ){
			windData = newWindData;
		}{
			windData.keysValuesDo{ |key,val|
				val.appendData( newWindData.at( key ) );
			};
		};
	}

}

ZLWindData {
	classvar <dataToIndex;
	var <height;
	var <data;

	*initClass{
		dataToIndex = IdentityDictionary.new;
		dataToIndex.put( \packets, 0 );
		dataToIndex.put( \windDirection, 1 );
		dataToIndex.put( \horizontalWind, 2 );
		dataToIndex.put( \horizontalWindMin, 3 );
		dataToIndex.put( \horizontalWindMax, 4 );
		dataToIndex.put( \horizontalWindStdDev, 5 );
		dataToIndex.put( \verticalWind, 6 );
		dataToIndex.put( \TI, 7 );
	}

	*new{ |height, startIndex, indata|
		^super.new.init( height, startIndex, indata );
	}

	init{ |h,startIndex, indata|
		height = h;
		data = indata.copyRange( startIndex, startIndex+7 );
		data = data.collect{ |col| col.collect{ |it| it.interpret } };
	}

	at{ |key|
		// [ key, key.isSequenceableCollection ].postln;

		if ( key.isSequenceableCollection ){ ^this.atAll( key ) };

		// this.class.dataToIndex.at( key ).postln;
		if ( this.class.dataToIndex.at( key ).notNil ){
			^data.at( this.class.dataToIndex.at( key ) );
		};
	}

	atAll { |keys|
		^keys.collect{ |k| k.postln; this.at( k ) }
	}

	range{ |key|
		var min, max;
		var wdata = this.at( key );
		min = wdata.minItem;
		max = wdata.maxItem;
		^[ min, max ]
	}

	// input is another instance of ZLWindData
	appendData{ |zlwd|
		data = (data.flop ++ zlwd.data.flop).flop;
	}

}

ZLDataFile{
	var <rawData;
	var <dataNoHeader;

	*new{ |fn|
		^super.new.init(fn);
	}

	init{ |fn|
		rawData = CSVFileReader.read( fn, true );
	}

	header{
		^rawData[1].copyFromStart( 106 );
	}

	dataWoHeader{
		if ( dataNoHeader.isNil ){
			var data;
			data = rawData.copyToEnd( 2 );
			data = data.flop;
			data = data.drop( -1 );
			dataNoHeader = data;
		};
		^dataNoHeader;
	}

	getMeteoData {
		var meteoData;
		var indices = [0,2,5,6,7,8,9,11,12,13,14,16,17,18];
		var data = this.dataWoHeader;
		meteoData = data.copyFromStart( 18 );
		meteoData = meteoData.collect{ |it,i|
			if ( indices.includes( i ), { it.collect{ |jt| jt.interpret } }, { it } );
		};
		^meteoData
	}

	getWindData {
		var windData;
		var data = this.dataWoHeader;
		windData = IdentityDictionary.new;
		windData.put( 249, ZLWindData.new( 249, ZLData.dataToIndex.at( \h249 ), data ) );
		windData.put( 199, ZLWindData.new( 199, ZLData.dataToIndex.at( \h199 ), data ) );
		windData.put( 179, ZLWindData.new( 179, ZLData.dataToIndex.at( \h179 ), data ) );
		windData.put( 156, ZLWindData.new( 156, ZLData.dataToIndex.at( \h156 ), data ) );
		windData.put( 145, ZLWindData.new( 145, ZLData.dataToIndex.at( \h145 ), data ) );
		windData.put( 119, ZLWindData.new( 119, ZLData.dataToIndex.at( \h119 ), data ) );
		windData.put( 94, ZLWindData.new( 94, ZLData.dataToIndex.at( \h94 ), data ) );
		windData.put( 75, ZLWindData.new( 75, ZLData.dataToIndex.at( \h75 ), data ) );
		windData.put( 63, ZLWindData.new( 63, ZLData.dataToIndex.at( \h63 ), data ) );
		windData.put( 38, ZLWindData.new( 38, ZLData.dataToIndex.at( \h38 ), data ) );
		windData.put( 14, ZLWindData.new( 14, ZLData.dataToIndex.at( \h14 ), data ) );
		^windData;
	}

}
