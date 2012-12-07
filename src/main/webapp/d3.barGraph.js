/**
 * @desc 	Visualizes bar graphs and grids. Depends on d3.
 * @params	results: the array of results
 * 			container: the HTML element that will contain the grid
 *			userOptions: configurable options fo rthe bar graph, possible options include:
 *			{
 *				width: 708,	// width of bar graph in pixels
 *				height: 299,	// height  of bar graph in pixels
 *				bars: 10,	// number of bars to show
 *				minColor: '#446688',	// starting minimum color
 *				maxColor: '#88ddff',	// starting maximum color
 *				modes: [function(d) { return -Math.log(d[2]); }],	// array of functions to apply on results for different modes
 *				modeFunction: function(options) {}	// function performed when mode is changed
 *			}
 * @author	Edward.Chen@mssm.edu (Edward Y. Chen)
 * @since	10/04/2012
 */
d3.barGraph = {	
	createBarGraph: function(results, container, userOptions) {
		// Don't create bar graph if nowhere to put it
		if (d3.select(container).empty())
			return;

		// Bar graph actually goes in svg-container		
		if (d3.select(container + ' div.svg-container').empty())
			d3.select(container).append('div').classed('svg-container', true);
		container += ' div.svg-container';

		// Bar graph attributes
		var options = {
			width: 708,
			height: 299,
			bars: 10,
			minColor: '#446688',
			maxColor: '#88ddff',
			modes: [function(d) { return -Math.log(d[2]); }],
			modeFunction: null
		};

		// Copy attributes from userOptions to options
		for (var key in userOptions)
			options[key] = userOptions[key];

		var sortId = container + ' span.method';

		// Initial data
		var filteredData = results.filter(function(d, i) { 
			return i < options.bars; 
		});
		var dataValues = filteredData.map(function(d) { 
			return options.modes[0](d); 
		});

		// Interpolators
		options.x = d3.scale.linear()
			.domain([d3.min(dataValues), d3.max(dataValues)])
			.range([0, options.width]);
		options.y = d3.scale.ordinal()
			.domain(d3.range(options.bars))
			.rangeBands([0, options.height], .2),
		options.color = d3.scale.linear()
			.domain([d3.min(dataValues), d3.max(dataValues)])
			.interpolate(d3.interpolateRgb)
			.range([options.minColor, options.maxColor]);

		// Create graph
		var chart = d3.select(container)
			.append('svg:svg')
			.datum(options)
			.attr('xmlns', "http://www.w3.org/2000/svg")
			.attr('version', '1.1')
			.attr('width', options.width)
			.attr('height', options.height);

		// Group that contains bar, shadow, and label
		var barGroup = chart.selectAll('g.bar')
			.data(filteredData)
			.enter().append('svg:g')
			.attr('class', 'bar')
			.attr('transform', function(d, i) { return 'translate(0,' + options.y(i) + ')'; });

		d3.barGraph.drawBar(barGroup, options);

		if (options.modes.length < 2) { return; }	// Don't bind click if < 2 modes
		chart.on('click', function() {
			// Change mode
			var mode = options.modes.shift()
			options.modes.push(mode);
			mode = options.modes[0];

			if (options.modeFunction)
				options.modeFunction(options)

			// Adjust data
			results.sort(function(a, b) { return mode(b) - mode(a); });
			filteredData = results.filter(function(d, i) { 
				return i < options.bars; 
			});
			dataValues = filteredData.map(function(d) { 
				return options.modes[0](d); 
			});

			// Remap interpolators
			options.x.domain([d3.min(dataValues), d3.max(dataValues)]);
			options.color.domain([d3.min(dataValues), d3.max(dataValues)]);

			// Join with existing data and remove old data
			var newBars = chart.selectAll('g.bar').data(filteredData, function(d) { return d; });
			var newBarGroup = newBars.enter().append('svg:g')
								.attr('class', 'bar')
								.attr('transform', function(d, i) { return 'translate(0,' + options.height + ')'; });
			d3.barGraph.drawBar(newBarGroup, options);
			newBars.exit().remove();

			// Sort bars
			chart.selectAll('g.bar')
				.sort(function(a, b) { return mode(b) - mode(a); })
				.transition()
				.duration(1000)
				.delay(500)
				.attr('transform', function(d, i) { return 'translate(0,' + options.y(i) + ')'; });

			// Adjust bar length
			chart.selectAll('rect.bar')
				.transition()
				.duration(500)
				.attr('index', function(d, i) { return i; })
				.attr('width', function(d) { return options.x(mode(d)); })
				.attr('fill', function(d) { return options.color(mode(d)); });

			// Adjust bar shadow
			chart.selectAll('rect.shadow')
				.transition()
				.duration(500)
				.delay(1500)
				.attr('opacity', 0)
				.transition()
				.duration(0)
				.delay(2000)
				.attr('width', function(d) { return options.x(mode(d)); })
				.attr('fill', function(d) { return options.color(mode(d)); })
				.transition()
				.duration(0)
				.delay(2500)
				.attr('opacity', 0.3);

			// Adjust label
			chart.selectAll('text.label')
				.transition()
				.text(function(d) { return d[1]; });
		});
	},
	drawBar: function(selection, options) {
		var mode = options.modes[0];

		// Bar shadow
		selection.append('svg:rect')
			.attr('class', 'shadow')
			.attr('fill', function(d) { return options.color(mode(d)); })
			.attr('opacity', 0.3)
			.attr('width', function(d) { return options.x(mode(d)); })
			.attr('height', options.y.rangeBand());

		// Bar
		selection.append('svg:rect')
			.attr('class', 'bar')
			.attr('fill', function(d) { return options.color(mode(d)); })
			.attr('width', function(d) { return options.x(mode(d)); })
			.attr('height', options.y.rangeBand());

		// Labels
		selection.append('svg:text')
			.attr('class', 'label')
			.text(function (d, i) { return d[1]; })
			.attr('width', function(d) { return mode(d); })
			.attr('x', 3)
			.attr('y', options.y.rangeBand() / 2)
			.attr("dy", "0.35em")
			.attr('fill', 'black');
	},
	recolor: function(container, newColor) {
		var canvas = d3.select(container + ' div.svg-container svg');
		var color = canvas.datum().color.range([d3.barGraph.scaleColor(newColor), newColor]);
		var mode = canvas.datum().modes[0];
		canvas.selectAll('rect.bar').transition().duration(400).attr('fill', function(d) { return color(mode(d)); });
		canvas.selectAll('rect.shadow').attr('fill', function(d) { return color(mode(d)); });
	},
	scaleColor: function(webColor) {
		var hsl = d3.barGraph.rgbToHsl(d3.barGraph.webColorToRgb(webColor));
		hsl[1] -= 2/3;
		hsl[2] -= 11/30;
		return d3.barGraph.rgbToHex(d3.barGraph.hslToRgb(hsl));
	},
	webColorToRgb: function(webColor) {
		if (webColor[0] == '#')
			return d3.barGraph.hexToRgb(webColor);
		else
			return webColor.substring(3).replace(/[\)\(\s]/g, '').split(',').map(function(d) { return parseInt(d); });
	},
	hexToRgb: function(hexString) {
		var hex = parseInt(hexString.substring(1), 16);
		var r = (hex & 0xff0000) >> 16;
		var g = (hex & 0x00ff00) >> 8;
		var b = hex & 0x0000ff;
		return [r, g, b];
	},
	rgbToHex: function(rgb) {
		return "#" + ((1 << 24) + (rgb[0] << 16) + (rgb[1] << 8) + rgb[2]).toString(16).slice(1);
	},
	rgbToHsl: function(rgb) {
		rgb = rgb.map(function(d) { return d / 255; });
		var minRgb = d3.min(rgb);
		var maxRgb = d3.max(rgb);
		var deltaRgb = maxRgb - minRgb;

		var h = 0, s = 0, l = (minRgb + maxRgb) / 2;

		if (deltaRgb != 0) {
			if (l < 0.5) s = deltaRgb / (maxRgb + minRgb);
			else s = deltaRgb / (2 - maxRgb - minRgb);

			deltaR = ((maxRgb - rgb[0]) / 6 + deltaRgb / 2) / deltaRgb;
			deltaG = ((maxRgb - rgb[1]) / 6 + deltaRgb / 2) / deltaRgb;
			deltaB = ((maxRgb - rgb[2]) / 6 + deltaRgb / 2) / deltaRgb;

			if (rgb[0] == maxRgb) h = deltaB - deltaG;
			else if (rgb[1] == maxRgb) h = 1/3 + deltaR - deltaB;
			else if (rgb[2] == maxRgb) h = 2/3 + deltaG - deltaR;

			if (h < 0) h++;
			if (h > 1) h--;
		}

		return [h, s, l];
	},
	hslToRgb: function(hsl) {
		var r = g = b = hsl[2] * 255;

		if (hsl[1] != 0) {
			if (hsl[2] < 0.5) var v2 = hsl[2] * (1 + hsl[1]);
			else var v2 = hsl[2] + hsl[1] - hsl[1] * hsl[2];

			var v1 = 2 * hsl[2] - v2;

			r = 255 * d3.barGraph.hueToRgb(v1, v2, hsl[0] + 1/3);
			g = 255 * d3.barGraph.hueToRgb(v1, v2, hsl[0]);
			b = 255 * d3.barGraph.hueToRgb(v1, v2, hsl[0] - 1/3);
		}

		return [r, g, b].map(function(d) { return Math.round(d); });
	},
	hueToRgb: function(v1, v2, vH) {
		if (vH < 0) vH += 1;
		if (vH > 1) vH -= 1;
		if ((6 * vH) < 1) return v1 + (v2 - v1) * 6 * vH;
		if ((2 * vH) < 1) return v2;
		if ((3 * vH) < 2) return v1 + (v2 - v1) * (2/3 - vH) * 6;
		return v1;
	}
}