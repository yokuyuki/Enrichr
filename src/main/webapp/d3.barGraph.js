/**
 * @desc 	Visualizes bar graphs and grids. Depends on d3.
 * @params
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
			maxColor: '#88ddff'
		};

		// Copy attributes from userOptions to options
		for (var key in userOptions)
			options[key] = userOptions[key];

		var mode = 1, sortId = container + ' span.method';

		// Initial data
		var filteredData = results.filter(function(d, i) { return i < options.bars; });

		// Interpolators
		options.x = d3.scale.linear()
			.domain([0, d3.max(filteredData.map(function(d) { 
				return d3.barGraph.displayValue(d); 
			}))])
			.range([0, options.width]);
		options.y = d3.scale.ordinal()
			.domain(d3.range(options.bars))
			.rangeBands([0, options.height], .2),
		options.color = d3.scale.linear()
			.domain([0, d3.max(filteredData.map(function(d) { return d3.barGraph.displayValue(d); }))])
			.interpolate(d3.interpolateRgb)
			.range([options.minColor, options.maxColor]);

		// Create graph
		var chart = d3.select(container)
			.append('svg:svg')
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

		// chart.on('click', function() {
		// 	// Change mode
		// 	mode = ++mode % 3;
		// 	switch (mode) {
		// 		case 0: $(sortId).text('combined score'); break;
		// 		case 1: $(sortId).text('p-value'); break;
		// 		case 2: $(sortId).text('rank'); break;
		// 	}

		// 	// Adjust data
		// 	results.sort(function(a, b) { return d3.barGraph.displayValue(b, mode) - d3.barGraph.displayValue(a, mode); });
		// 	filteredData = results.filter(function(d, i) { return i < options.bars; });

		// 	// Remap interpolators
		// 	x.domain([0, d3.max(filteredData.map(function(d) { return d3.barGraph.displayValue(d); }))]);
		// 	color.domain([0, d3.max(filteredData.map(function(d) { return d3.barGraph.displayValue(d); }))]);

		// 	// Join with existing data and remove old data
		// 	var newBars = chart.selectAll('g.bar').data(filteredData, function(d) { return d; });
		// 	var newBarGroup = newBars.enter().append('svg:g')
		// 						.attr('class', 'bar')
		// 						.attr('transform', function(d, i) { return 'translate(0,' + height + ')'; });			
		// 	d3.barGraph.drawBar(newBarGroup, options);
		// 	newBars.exit().remove();

		// 	// Sort bars
		// 	chart.selectAll('g.bar')
		// 		.sort(function(a, b) { return d3.barGraph.displayValue(b, mode) - d3.barGraph.displayValue(a, mode); })
		// 		.transition()
		// 		.duration(1000)
		// 		.delay(500)
		// 		.attr('transform', function(d, i) { return 'translate(0,' + options.y(i) + ')'; });

		// 	// Adjust bar length
		// 	chart.selectAll('rect.bar')					
		// 		.transition()
		// 		.duration(500)
		// 		.attr("index", function(d, i) {return i})
		// 		.attr('width', function(d) { return options.x(d3.barGraph.displayValue(d)); })
		// 		.attr('fill', function(d) { return options.color(d3.barGraph.displayValue(d)); });

		// 	// Adjust bar shadow
		// 	chart.selectAll('rect.shadow')					
		// 		.transition()
		// 		.duration(500)
		// 		.delay(1500)
		// 		.attr('opacity', 0)
		// 		.transition()
		// 		.duration(0)
		// 		.delay(2000)
		// 		.attr('width', function(d) { return options.x(d3.barGraph.displayValue(d)); })
		// 		.attr('fill', function(d) { return options.color(d3.barGraph.displayValue(d)); })
		// 		.transition()
		// 		.duration(0)
		// 		.delay(2500)
		// 		.attr('opacity', 0.3);

		// 	// Adjust label
		// 	chart.selectAll('text.label')
		// 		.transition()
		// 		.text(function(d) { return d[1]; });
		// });		
	},
	drawBar: function(selection, options) {
		// Bar shadow
		selection.append('svg:rect')
			.attr('class', 'shadow')
			.attr('fill', function(d) { return options.color(d3.barGraph.displayValue(d)); })
			.attr('opacity', 0.3)
			.attr('width', function(d) { return options.x(d3.barGraph.displayValue(d)); })
			.attr('height', options.y.rangeBand());

		// Bar
		selection.append('svg:rect')
			.attr('class', 'bar')
			.attr('fill', function(d) { return options.color(d3.barGraph.displayValue(d)); })
			.attr('width', function(d) { return options.x(d3.barGraph.displayValue(d)); })
			.attr('height', options.y.rangeBand());

		// Labels
		selection.append('svg:text')
			.attr('class', 'label')
			.text(function (d, i) { return d[1]; })
			.attr('width', function(d) { return d3.barGraph.displayValue(d); })
			.attr('x', 3)
			.attr('y', options.y.rangeBand() / 2)
			.attr("dy", "0.35em")
			.attr('fill', 'black');
	},
	// Allow displaying of the different sorting method by correcting the value
	displayValue: function(value) {
		switch(1) {
		// switch(mode) {
			case 0:
				return value[4]; break;
			case 1:
				return -Math.log(value[2]); break;
			case 2:
				return -value[3]; break;
		}
	}
}