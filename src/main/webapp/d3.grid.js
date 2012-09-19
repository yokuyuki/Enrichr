/**
 * @desc	Creates a grid based on correlation values from a json file and highlights the top X amount from results.
 * @params	jsonLocation: location where the json file that defines the correlation values for the grid
 * 			results: the array that contains the nodes that need to be highlighted the grid
 * 			container: the HTML element that will contain the grid
 *			options: configurable options for the grid, possible options include:
 *				{
 *					canvasSize: 225,	// size of canvas in pixels
 *					highlightCount: 10,	// top x to highlight from results
 *					highlightValue: function(d) { return d[0]; },	// function used to select items from the results array to highlight
 					highlightFunction: function(c) {},	// function performed on highlighted circles where c is the DOM path to them
 					highlightColor: #FFFFFF,	// Color of highlight circles
 					clusterFunction: function(z) {},	// function performed on the z-score of the cluster function
 					maxColor: #FF6666, 	// Grid ranges from black to this color using an exponential scale
 					cache: true	// Controls whether grids are cached
 *				}
 * @author	Edward Y. Chen
 * @since	9/17/2012
 */

function createGrid(jsonLocation, results, container, options) {
	// Don't create grid if nowhere to put it
	if (d3.select(container).empty())
		return;

	// Grid actually goes in svg-container		
	if (d3.select(container + ' div.svg-container').empty())
		d3.select(container).append('div').classed('svg-container', true);
	container += ' div.svg-container';

	// Grid attributes
	var gridAttr = {
		canvasSize: 225,
		highlightCount: 10,
		highlightValue: function(d) { return d[0]; },
		highlightColor: '#FFFFFF',
		maxColor: '#FF6666',
		color: d3.scale.pow().exponent(5).interpolate(d3.interpolateRgb),
		cache: true
	}

	// Copy attributes from options to gridAttr
	for (var key in options)
		gridAttr[key] = options[key];

	d3.json(jsonLocation + (gridAttr.cache ? '' : '?_=' + new Date().getTime()), function(json) {	// Control caching
		gridAttr.width = Math.sqrt(json.length),
		gridAttr.pixels = 225 / gridAttr.width;
		gridAttr.color.domain([0, d3.max(json.map(function(d) { return d[1]; }))])
			.range(['#000000', gridAttr.maxColor]);

		drawCanvas(json, container);	
		fill(results.filter(function(d, i) { return i < gridAttr.highlightCount; }), container);
		if (typeof gridAttr.clusterFunction != 'undefined')
			calcClustering(container);
	});	

	function drawCanvas(nodes, container) {		
		var canvas = d3.select(container)
					.append('svg:svg')
					.attr('width', gridAttr.canvasSize)
					.attr('height', gridAttr.canvasSize);

		canvas.selectAll('rect')
			.data(nodes)
			.enter()
			.append('svg:rect')
			.attr('x', function(d, i) { return i % gridAttr.width * gridAttr.pixels; })
			.attr('y', function(d, i) { return Math.floor(i / gridAttr.width) * gridAttr.pixels; })
			.attr('width', gridAttr.pixels)
			.attr('height', gridAttr.pixels)
			.attr('fill', function(d) { return gridAttr.color(d[1]); })
			.append('title')
			.text(function(d) { return d[0]; });

		canvas.selectAll('circle')
			.data(nodes)
			.enter()
			.append('svg:circle')
			.attr('cx', function(d, i) { return i % gridAttr.width * gridAttr.pixels + gridAttr.pixels/2; })
			.attr('cy', function(d, i) { return Math.floor(i / gridAttr.width) * gridAttr.pixels + gridAttr.pixels/2; })
			.attr('fill', gridAttr.highlightColor)
			.attr('fill-opacity', 0)
			.attr('r', Math.floor(gridAttr.pixels/3))
			.attr('label', function(d) { return d[0]; })
			.append('title')
			.text(function(d) { return d[0]; });
	}

	function fill(elementList, container) {
		for (e in elementList) {
			d3.selectAll(container + ' circle[label="' + gridAttr.highlightValue(elementList[e]) + '"]')
				.datum(function() { return elementList[e]; })
				.attr('fill-opacity', 1)
				.classed('highlight', true);
		}

		if (typeof gridAttr.highlightFunction != 'undefined')
			gridAttr.highlightFunction(container + ' circle.highlight');
	}

	function calcClustering(container) {
		var mean = 0.6291 * Math.pow(gridAttr.highlightCount / Math.pow(gridAttr.width, 2), -0.503301);
		var std = 0.328498 * Math.pow(gridAttr.highlightCount, -1.00728) * Math.pow(gridAttr.width, 1.00939);

		var getCoord = function(value) { return (value - gridAttr.pixels/2) / gridAttr.pixels; };
		var circles = d3.selectAll(container + ' circle.highlight')[0];
		var avg = d3.mean(circles, function(a) {
			return d3.min(circles, function(b) {	// Find nearest neighbor
				if (a != b) {
					return manhattanDistance(getCoord(a.getAttribute('cx')),
						getCoord(a.getAttribute('cy')),
						getCoord(b.getAttribute('cx')),
						getCoord(b.getAttribute('cy')));
				}
			});
		});

		gridAttr.clusterFunction((avg - mean) / std);	// Z-score
	}

	function manhattanDistance(x1, y1, x2, y2) {
		var dx = Math.abs(x1 - x2);
		var dy = Math.abs(y1 - y2);

		// Correct x and y distances for torus
		return Math.min(dx, gridAttr.width - dx) + Math.min(dy, gridAttr.width - dy);
	}
}