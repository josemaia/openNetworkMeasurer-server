var map;
var ajaxRequest;
var plotlist;
var twoglayer;
var threeglayer;
var fourglayer;
var currentOperator;

function onMapMove(e) {
    operatorFilter();
    askForPlots(); }

function askForPlots() {
    // request the marker info with AJAX for the current bounds
    var bounds=map.getBounds();
    var minll=bounds.getSouthWest();
    var maxll=bounds.getNorthEast();
    var msg='/boundingbox?&left='+minll.lng+'&bottom='+minll.lat+'&right='+maxll.lng+'&top='+maxll.lat;
    ajaxRequest.onreadystatechange = stateChanged;
    ajaxRequest.open('GET', msg, true);
    ajaxRequest.send(null);
}

function operatorFilter(){
    var bounds=map.getBounds();
    var minll=bounds.getSouthWest();
    var maxll=bounds.getNorthEast();
    $('#loading').show();
    $.ajax({
        url: '/operators',
        data: {
            left: minll.lng,
            bottom: minll.lat,
            right: maxll.lng,
            top: maxll.lat
        },
        type: 'GET',
        dataType: 'json',
        success: function (json) {
            var container = $('.myRadioContainer');
            container.empty();
            for (i = 0; i < json.length; i++) {
                $('<p><input type="radio" name="dynradio" id=' + json[i].operatorId + ' value=' + json[i].operatorId + '>' + json[i].operatorName + '('
                + json[i].operatorId + ') </input></p>').appendTo(container);
            }
            $('<p><input type="radio" name="dynradio" id=' + 0 + ' value=' + 0 + '>' + 'Show all operators </input></p>').appendTo(container);
            $('input[id='+currentOperator+']').prop('checked',true);
            $('input[name=dynradio]').on('change', function () {
                currentOperator = this.value;
                askForPlots();
            })
        }
    })
}

function getXmlHttpObject() {
    if (window.XMLHttpRequest) { return new XMLHttpRequest(); }
    if (window.ActiveXObject)  { return new ActiveXObject("Microsoft.XMLHTTP"); }
    return null;
}

function stateChanged() {
    // if AJAX returned a list of markers, add them to the map
    if (ajaxRequest.readyState == 4) {
        //use the info here that was returned
        if (ajaxRequest.status == 200) {
            $('#loading').hide();
            plotlist = eval("(" + ajaxRequest.responseText + ")");
            removeMarkers();
            for (i = 0; i < plotlist.length; i++) {

                var geojsonfeature = plotlist[i];
                var geojsonlayer = L.geoJson(geojsonfeature,
                    {
                        style: function(feature) {
                            switch (feature.properties.level) {
                                case '0':
                                    return {color: "#ffffff"}; //should this happen?
                                case '1':
                                    return {color: "#E50002"};
                                case '2':
                                    return {color: "#E37F08"};
                                case '3':
                                    return {color: "#CEE211"};
                                case '4':
                                    return {color: "#5FE01A"};
                            }
                        },
                        onEachFeature: function (feature, layer) {
                            var samples;
                            if (feature.properties.numsamples == 1)
                                samples = "1 measurement";
                            else
                                samples = feature.properties.numsamples+" measurements";
                            layer.bindPopup(
                                "<h3 class=featurePopup>"+feature.properties.operatorName+" ("+feature.properties.operatorId+"): "+feature.properties.networkClass+"</h3>"+"\n"
                                 +"<p class=featurePopup>Average signal = "+feature.properties.signalAsu+" ASU, "+feature.properties.signalDbm+" dBm"+"</p>\n"
                                +"<p class=featurePopup>Calculated from "+samples+"</p>"
                            );
                        },
                        filter: function (feature,layer) {
                            if (currentOperator == 0) return true;
                            else return feature.properties.operatorId == currentOperator;
                        }
                    }
                );
                switch (geojsonfeature.properties.networkClass){
                    case "2G":
                        twoglayer.addLayer(geojsonlayer);
                        break;
                    case "3G":
                        threeglayer.addLayer(geojsonlayer);
                        break;
                    case "4G":
                        fourglayer.addLayer(geojsonlayer);
                        break;
                }
            }
        }
    }
}

function removeMarkers() {
    twoglayer.clearLayers();
    threeglayer.clearLayers();
    fourglayer.clearLayers();
}



function initmap() {
    // set up AJAX request
    ajaxRequest = getXmlHttpObject();
    if (ajaxRequest == null) {
        alert("This browser does not support HTTP Request");
        return;
    }

    // set up the map
    map = L.map('map');

    currentOperator = 0;

    map.locate({setView: true, maxZoom: 16});
    map.on('locationerror', onLocationError);
    map.on('locationfound', onLocationFound);
}

function onLocationFound(e){
    var mqLayer = L.tileLayer("http://otile{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png", {
        subdomains: "1234",
        attribution: "&copy; <a href='http://www.openstreetmap.org/'>OpenStreetMap</a> and contributors, under an <a href='http://www.openstreetmap.org/copyright' title='ODbL'>open license</a>. Tiles Courtesy of <a href='http://www.mapquest.com/'>MapQuest</a> <img src='http://developer.mapquest.com/content/osm/mq_logo.png'>"
    });
    mqLayer.addTo(map);

    var mqAerialLayer = L.tileLayer("http://otile{s}.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.png", {
        subdomains: "1234",
        attribution: "&copy; <a href='http://www.openstreetmap.org/'>OpenStreetMap</a> and contributors, under an <a href='http://www.openstreetmap.org/copyright' title='ODbL'>open license</a>. Tiles Courtesy of <a href='http://www.mapquest.com/'>MapQuest</a> <img src='http://developer.mapquest.com/content/osm/mq_logo.png'>"
    });

    twoglayer = L.layerGroup();
    twoglayer.addTo(map);
    threeglayer = L.layerGroup();
    threeglayer.addTo(map);
    fourglayer = L.layerGroup();
    fourglayer.addTo(map);

    var baseMaps = {
        "Street map": mqLayer,
        "Aerial map (limited coverage)": mqAerialLayer
    };

    var overlayMaps = {
        "2G": twoglayer,
        "3G": threeglayer,
        "4G": fourglayer
    };

    var layercontrol = L.control.layers(baseMaps, overlayMaps, {collapsed: false});
    if ($(window).width() < 800 || $(window).height() < 600){
        layercontrol.options.collapsed = true;
    }
    layercontrol.addTo(map);

    var containerform = $('.leaflet-control-layers-list');
    containerform.append('<div class="leaflet-control-layers-separator"></div>');
    containerform.append('<div class="myRadioContainer"/>');

    onMapMove();
    map.on('moveend', onMapMove);

}

function onLocationError(e) {
    map.setView([41.147627, -8.567756], 13);
    onLocationFound();
}