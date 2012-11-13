/**
 * @desc	Creates a grid based on correlation values from a json file and highlights the top X amount from results.
 * @params	jsonLocation: location where the json file that defines the correlation values for the grid
 * 			results: the array that contains the nodes that need to be highlighted the grid
 * 			container: the HTML element that will contain the grid
 *			options: configurable options for the grid, possible options include:
 *			{
 *				canvasSize: 225,	// size of canvas in pixels
 *				highlightCount: 10,	// top x to highlight from results
 *				highlightName: function(d) { return d[0]; },	// function used to select the name of the highlight items from the results array
 *				highlightValue: function(d) {},	// function used to select the value of the highlighted items from the results array
 *				highlightFunction: function(c) {},	// function performed on highlighted circles where c is the DOM path to them
 *				highlightColor: #FFFFFF,	// Color of highlight circles
 *				clusterFunction: function(z) {},	// function performed on the z-score of the cluster function
 *				maxColor: #FF6666, 	// Grid ranges from black to this color using an exponential scale
 *				cache: true	// Controls whether grids are cached
 *			}
 * @author	Edward.Chen@mssm.edu (Edward Y. Chen)
 * @since	9/17/2012
 */
d3.grid = {
	createGrid: function(jsonLocation, results, container, userOptions) {
		// Don't create grid if nowhere to put it
		if (d3.select(container).empty())
			return;

		// Grid actually goes in svg-container
		if (d3.select(container + ' div.svg-container').empty())
			d3.select(container).append('div').classed('svg-container', true);
		container += ' div.svg-container';

		// Grid attributes
		var options = {
			canvasSize: 225,
			highlightCount: 10,
			highlightName: function(d) { return d[0]; },
			highlightValue: null,
			highlightFunction: null,
			highlightColor: '#FFFFFF',
			clusterFunction: null,
			maxColor: '#FF6666',
			cache: true
		};

		// Copy attributes from userOptions to options
		for (var key in userOptions)
			options[key] = userOptions[key];

		d3.json(jsonLocation + (options.cache ? '' : '?_=' + new Date().getTime()), function(json) {	// Control caching
			options.width = Math.sqrt(json.length),
			options.pixels = 225 / options.width;
			options.color = d3.scale.pow().exponent(5)
				.interpolate(d3.interpolateRgb)
				.domain([0, d3.max(json.map(function(d) { return d[1]; }))])
				.range(['#000000', options.maxColor]);

			d3.grid.drawCanvas(json, container, options);
			d3.grid.drawHighlights(results.filter(function(d, i) { return i < options.highlightCount; }), container, options);
			if (options.clusterFunction)
				d3.grid.calcClustering(container, options);

			// Toggle between differen views of grid
			d3.select(container + ' svg').on('click', function() {
				var circles = d3.select(container + ' svg g.circle');
				circles.transition()
					.attr('opacity', (parseInt(circles.attr('opacity'))) ^ 1)
					.each('end', function() {
						d3.grid.recolor(container, options.maxColor);
					});
			});
		});	
	},
	drawCanvas: function(nodes, container, options) {
		var canvas = d3.select(container)
					.append('svg:svg')
					.datum(options)
					.attr('xmlns', "http://www.w3.org/2000/svg")
					.attr('version', '1.1')
					.attr('width', options.canvasSize)
					.attr('height', options.canvasSize);

		canvas.append('svg:g').classed('rect', true)
			.selectAll('rect')
			.data(nodes)
			.enter()
			.append('svg:rect')
			.attr('x', function(d, i) { return i % options.width * options.pixels; })
			.attr('y', function(d, i) { return Math.floor(i / options.width) * options.pixels; })
			.attr('width', options.pixels)
			.attr('height', options.pixels)
			.attr('fill', function(d) { return options.color(d[1]); })
			.attr('label', function(d) { return d[0]; })
			.append('title')
			.text(function(d) { return d[0]; });

		canvas.append('svg:g').classed('circle', true).attr('opacity', 1)
			.selectAll('circle')
			.data(nodes)
			.enter()
			.append('svg:circle')
			.attr('cx', function(d, i) { return i % options.width * options.pixels + options.pixels/2; })
			.attr('cy', function(d, i) { return Math.floor(i / options.width) * options.pixels + options.pixels/2; })
			.attr('fill', options.highlightColor)
			.attr('fill-opacity', 0)
			.attr('r', Math.floor(options.pixels/3))
			.attr('label', function(d) { return d[0]; })
			.append('title')
			.text(function(d) { return d[0]; });
	},
	drawHighlights: function(elementList, container, options) {
		if (options.highlightValue) {
			var maxValue = d3.max(elementList.map(function(d) { return options.highlightValue(d); }))
			options.highlightColor = d3.scale.linear()
				.domain([0, maxValue])
				.interpolate(d3.interpolateNumber)
				.range([0.25, 1]);
			options.altColor = d3.scale.linear()
				.domain([0, maxValue])
				.interpolate(d3.interpolateRgb)
				.range(['#000000', options.maxColor]);
		}

		for (e in elementList) {
			d3.selectAll(container + ' circle[label="' + options.highlightName(elementList[e]) + '"]')
				.datum(function() { return elementList[e]; })
				.attr('fill-opacity', function(d) {
					return (options.highlightColor) ? options.highlightColor(options.highlightValue(d)) : 1;
				})
				.classed('highlight', true);
			d3.selectAll(container + ' rect[label="' + options.highlightName(elementList[e]) + '"]')
				.classed('highlight', true);
		}

		if (options.highlightFunction)
			options.highlightFunction(container + ' circle.highlight');
	},
	drawAltHighlights: function(container) {
		var canvas = d3.select(container +  ' svg');
		var options = canvas.datum();

		canvas.selectAll('rect').transition().duration(500).attr('fill', function(d, i) {
			if (d3.select(this).classed('highlight')) {
				var value = options.highlightValue(d3.select(container + ' svg g.circle > :nth-child(' + (i+1) + ')').datum());
				return (options.highlightValue) ? options.altColor(value) : options.maxColor;
			}
			else
				return '#000000';
		});
	},
	recolor: function(container, newColor) {
		var canvas = d3.select(container + ' svg');
		canvas.datum().maxColor = newColor;
		var color = canvas.datum().color.range(['#000000', newColor]);
		canvas.datum().altColor.range(['#000000', newColor]);

		if (canvas.select('g.circle').attr('opacity') == 0)
			d3.grid.drawAltHighlights(container);
		else
			canvas.selectAll('rect').transition().duration(500).attr('fill', function(d) { return color(d[1]); });
	},	
	calcClustering: function(container, options) {
		var mean = 0.6291 * Math.pow(options.highlightCount / Math.pow(options.width, 2), -0.503301);
		var std = 0.328498 * Math.pow(options.highlightCount, -1.00728) * Math.pow(options.width, 1.00939);

		var getCoord = function(value) { return (value - options.pixels/2) / options.pixels; };
		var circles = d3.selectAll(container + ' circle.highlight')[0];
		var avg = d3.mean(circles, function(a) {
			return d3.min(circles, function(b) {	// Find nearest neighbor
				if (a != b) {
					return d3.grid.manhattanDistance(getCoord(a.getAttribute('cx')),
						getCoord(a.getAttribute('cy')),
						getCoord(b.getAttribute('cx')),
						getCoord(b.getAttribute('cy')),
						options.width);
				}
			});
		});

		options.clusterFunction((avg - mean) / std);	// Z-score
	},
	manhattanDistance: function(x1, y1, x2, y2, width) {
		var dx = Math.abs(x1 - x2);
		var dy = Math.abs(y1 - y2);

		// Correct x and y distances for torus
		return Math.min(dx, width - dx) + Math.min(dy, width - dy);
	}
}