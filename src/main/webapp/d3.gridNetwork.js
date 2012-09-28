d3.gridNetwork = {
	createGridNetwork: function(jsonLocation, results, container, userOptions) {
		// Don't create gridNetwork if nowhere to put it
		if (d3.select(container).empty())
			return;

		// GridNetwork actually goes in svg-container		
		if (d3.select(container + ' div.svg-container').empty())
			d3.select(container).append('div').classed('svg-container', true);
		container += ' div.svg-container';

		// GridNetwork attributes
		var options = {
			width: 225,
			height: 225,
			highlightCount: 10,
			nodeColor: '#D90000',
			nodeOutline: '#eee',
			linkColor: '#ddd',
			cache: true
		};

		// Copy attributes from userOptions to options
		for (var key in userOptions)
			options[key] = userOptions[key];

		d3.json(jsonLocation + (options.cache ? '' : '?_=' + new Date().getTime()), function(nodes) {	// Control caching			
			var links = d3.gridNetwork.calculateBestDistance(nodes, results.filter(function(d, i) { return i < options.highlightCount; }));
			d3.gridNetwork.buildNetwork(links, container, options);
		});
	},
	euclideanDistance: function(x1, y1, x2, y2, width) {
		var dx = Math.abs(x1 - x2);
		var dy = Math.abs(y1 - y2);

		// Correct x and y distances for torus
		return Math.sqrt(Math.pow(Math.min(dx, width - dx), 2) + Math.pow(Math.min(dy, width - dy), 2));
	},
	calculateBestDistance: function(nodes, selection) {
		// Constants		
		var width = Math.sqrt(nodes.length);
		var edgeLimit = Math.ceil(selection.length * 1.5);
		var maxDistance = Math.sqrt(2) * Math.floor(width/2);

		// Utility functions
		var getX = function(index) { return index % width; };
		var getY = function(index) { return Math.floor(index / width); };

		// Data structures
		var distanceArray = [];
		var leastDistance = {};		
		var selectedNodes = [];

		// Extract selected nodes using selection on nodes
		for (var i in nodes) {
			var nodeName = nodes[i][0];

			selection.forEach(function(d) {
				if (d[1] == nodeName) {
					selectedNodes.push([nodeName, getX(i), getY(i)]);
					leastDistance[nodeName] = ["", maxDistance];
				}
			});
		}

		// Push every unique edge to distanceArray and find nearest neighbor
		for (var i in selectedNodes) {
			var name1 = selectedNodes[i][0];
			var x1 = selectedNodes[i][1];
			var y1 = selectedNodes[i][2];

			for (var j = i+1; j < selectedNodes.length; j++) {
				var name2 = selectedNodes[j][0];
				var x2 = selectedNodes[j][1];
				var y2 = selectedNodes[j][2];

				var distance = d3.gridNetwork.euclideanDistance(x1, y1, x2, y2, width);
				distanceArray.push([name1, name2, distance]);

				if (leastDistance[name1][1] > distance)
					leastDistance[name1] = [name2, distance];
				if (leastDistance[name2][1] > distance)
					leastDistance[name2] = [name1, distance];
			}
		}

		// Remove any bigger distances past edgeLimit
		distanceArray.sort(function(a,b) { return a[2]-b[2]; });		
		var distanceArray = distanceArray.slice(0, edgeLimit);

		var trackNodes = {};
		var links = distanceArray.map(function(d) {
			trackNodes[d[0]] = true;
			trackNodes[d[1]] = true;
			return {source: d[0], target: d[1], type: 'default'};
		});

		for (var chosen = 0; chosen < selectedNodes.length; chosen++) {
			if (!(selectedNodes[chosen].searchText in trackNodes))
				links.push({source: selectedNodes[chosen][0], target: leastDistance[selectedNodes[chosen][0]][0], type: 'default'})
		}

		return links;
	},
	buildNetwork: function(links, container, options) {
		var nodes = {};

		links.forEach(function(link) {
				link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
				link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
			}
		);

		var force = d3.layout.force().nodes(d3.values(nodes))
					.links(links)
					.size([options.width, options.height])
					.linkDistance(60)
					.charge(-300)
					.on('tick', tick).start();

		var svg = d3.select(container).append('svg:svg')
					.attr('class', 'gridNetwork')
					.attr('height', options.height)
					.attr('width', options.width)
					.attr('pointer-events', 'all')
					.append('svg:g')
					.call(d3.behavior.zoom().on('zoom', redraw))
					.append('svg:g');

		svg.append('svg:rect').attr('width', options.width*15)
			.attr('height', options.height*15)
			.attr('x', -7.5*options.width)
			.attr('y', -7.5*options.height)
			.attr('opacity', 0);

		var link = svg.selectAll('.link').data(force.links()).enter()
					.append('line')
					.attr('class', 'link')
					.style('stroke', options.linkColor)
					.style('stroke-width', '1.5px');

		var node = svg.selectAll('.node').data(force.nodes()).enter()
					.append('g')
					.attr('class', 'node')
					.on('mouseover', mouseover)
					.on('mouseout', mouseout)
					.call(force.drag);

		node.append('circle').attr('r', 8)
			.style('fill', options.nodeColor)
			.style('stroke', options.nodeOutline)
			.style('stroke-width', '1.5px');

		node.append('text').attr('x', 12)
			.attr('dy', '.35em')
			.style('font', '10px sans-serif')
			.style('pointer-events', 'none')
			.text(function(d) { return d.name; });

		function tick() {
			link.attr('x1', function(d) { return d.source.x; })
				.attr('y1', function(d) { return d.source.y; })
				.attr('x2', function(d) { return d.target.x; })
				.attr('y2', function(d) { return d.target.y; });

			node.attr('transform', function(d){ 
				return 'translate(' + d.x + ',' + d.y + ')'; 
			});
		}

		function mouseover() {
			d3.select(this).select('circle').transition().duration(750).attr('r', 16);
		}

		function mouseout(d) {
			d3.select(this).select('circle').transition().duration(750).attr('r', 8);
			d.fixed = true;
		}

		function redraw() {
			svg.attr('transform','translate(' + d3.event.translate + ')' + ' scale(' + d3.event.scale + ')');
		}
	}
};