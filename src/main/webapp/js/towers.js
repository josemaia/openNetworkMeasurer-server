var map;
var ajaxRequest;
var plotlist;
var plotlayers=[];
var circlelayers=[];
var towerDetailLayer;

L.AwesomeMarkers.Icon.prototype.options.prefix = 'ion';

var redIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'red'
});
var darkRedIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'darkred'
});
var orangeIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'orange'
});
var greenIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'green'
});
var darkGreenIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'darkgreen'
});
var blueIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'blue'
});
var purpleIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'purple'
});
var grayIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'gray'
});
var beigeIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'beige'
});
var cadetBlueIcon = new L.AwesomeMarkers.icon({
    icon:'radio-waves',
    markerColor:'cadetblue'
});

$(document).ready(function() {
    // process the form
    $('form').submit(function(event) {
        queryByCid($('input[name=cid]').val(),$('input[name=lac]').val());
        event.preventDefault();
    });
});

function queryByCid(cell,area,mcc,net,layer) {
    var msg='/towervalues?cell='+cell+'&area='+area+'&mcc='+mcc+'&net='+net;
    ajaxRequest = getXmlHttpObject();
    ajaxRequest.open('GET', msg, true);
    towerDetailLayer = layer;
    ajaxRequest.onreadystatechange = showTowerDetails;
    $('#loading').show();
    ajaxRequest.send(null);
}

function getXmlHttpObject() {
    if (window.XMLHttpRequest) { return new XMLHttpRequest(); }
    if (window.ActiveXObject)  { return new ActiveXObject("Microsoft.XMLHTTP"); }
    return null;
}

function showTowerDetails() {
    if (ajaxRequest.readyState == 4) {
        if (ajaxRequest.status == 200) {
            $('#loading').hide();
            plotlist = eval("(" + ajaxRequest.responseText + ")");
            removeCircleMarkers();
            var toBind = null;
            if (plotlist[1].geometry!=null){
                var tower = plotlist[1];
                var towerLayer = L.geoJson(tower,
                    {
                        onEachFeature: function (feature, layer) { //TODO: unnecessary to perform extra query here - values already in towerDetailLayer
                            var samples;
                            if (feature.properties.samples == 1)
                                samples = "1 measurement";
                            else
                                samples = feature.properties.samples+" measurements";

                            var towerType;
                            if (feature.properties.changeable === "true")
                                towerType = "Tower location estimated from "+samples;
                            else
                                towerType = "Known tower position.";
                            toBind =
                                "<h3 class=featurePopup> Operator - (" + feature.properties.mcc+" "+feature.properties.mnc+ "): " + feature.properties.radio + " tower </h3>" + "\n"
                                +"<p class=featurePopup> Cell ID:"+feature.properties.cid+" Local Area Code: "+feature.properties.lac+"</p>" + "\n"
                                +"<p class=featurePopup>"+towerType+"</p>"
                            ;
                        }
                    }
                );
                if (towerLayer!=null){
                    if (toBind!=null) {
                        towerDetailLayer.bindPopup(toBind);
                        towerDetailLayer.openPopup();
                    }
                }
            }
            if (plotlist[0].geometry!=null){
                var circle = plotlist[0];
                var circleLayer = L.geoJson(circle, {
                        onEachFeature: function (feature, layer) {
                            layer.bindPopup(
                                "<h3 class=featurePopup> Observations performed within this area. </h3>"
                            );
                        }
                    }
                );
                if (circleLayer!=null){
                    map.addLayer(circleLayer);
                    circlelayers.push(circleLayer);
                }
            }
        }
    }
}

function onMapMove(){
    var bounds=map.getBounds();
    var minll=bounds.getSouthWest();
    var maxll=bounds.getNorthEast();
    var msg='/towerbox?&left='+minll.lng+'&bottom='+minll.lat+'&right='+maxll.lng+'&top='+maxll.lat;
    ajaxRequest.onreadystatechange = showAllTowers;
    ajaxRequest.open('GET', msg, true);
    $('#loading').show();
    ajaxRequest.send(null);
}

function showAllTowers(){
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
                        onEachFeature: function (feature, layer) {
                            layer.on('click',function(e) {
                                queryByCid(feature.properties.cell, feature.properties.area, feature.properties.mcc, feature.properties.net, layer);
                            });
                            setIcon(layer,feature.properties.color);
                        }
                    }
                );
                geojsonlayer.addTo(map);
                plotlayers.push(geojsonlayer);
            }
        }
    }
}

function setIcon(layer,color){
    var icon = new L.AwesomeMarkers.icon({
        icon:'radio-waves',
        markerColor:color
    });
    layer.setIcon(icon);
}

function removeMarkers() {
    for (i=0;i<plotlayers.length;i++){
        map.removeLayer(plotlayers[i]);
    }
    plotlayers=[];
}

function removeCircleMarkers() {
    for (i=0;i<circlelayers.length;i++){
        map.removeLayer(circlelayers[i]);
    }
    circlelayers=[];
}

function initmap() {
    // set up AJAX request
    ajaxRequest = getXmlHttpObject();
    if (ajaxRequest == null) {
        alert("This browser does not support HTTP Request");
        return;
    }

    // set up the map
    map = L.map('map', {
        maxZoom: 16,
        minZoom: 14
    });

    map.locate({setView: true, maxZoom: 16});
    map.on('locationerror', onLocationError);
    map.on('locationfound', onLocationFound);
}

function onLocationFound(e){
    var mqLayer = L.tileLayer("http://otile{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png", {
        subdomains: "1234",
        attribution: "&copy; <a href='http://www.openstreetmap.org/'>OpenStreetMap</a> and contributors, under an <a href='http://www.openstreetmap.org/copyright' title='ODbL'>open license</a>. Tiles Courtesy of <a href='http://www.mapquest.com/'>MapQuest</a> <img src='http://developer.mapquest.com/content/osm/mq_logo.png'>. Tower icons by <a href='http://www.ionicons.com'>ionicons</a>."
    });
    mqLayer.addTo(map);

    var mqAerialLayer = L.tileLayer("http://otile{s}.mqcdn.com/tiles/1.0.0/sat/{z}/{x}/{y}.png", {
        subdomains: "1234",
        attribution: "&copy; <a href='http://www.openstreetmap.org/'>OpenStreetMap</a> and contributors, under an <a href='http://www.openstreetmap.org/copyright' title='ODbL'>open license</a>. Tiles Courtesy of <a href='http://www.mapquest.com/'>MapQuest</a> <img src='http://developer.mapquest.com/content/osm/mq_logo.png'>. Tower icons by <a href='http://www.ionicons.com'>ionicons</a>."
    });

    var baseMaps = {
        "Street map": mqLayer,
        "Aerial map (limited coverage)": mqAerialLayer
    };

    L.control.layers(baseMaps).addTo(map);

    onMapMove();
    map.on('moveend', onMapMove);
}

function onLocationError(e) {
    map.setView([41.147627, -8.567756], 13);
    onLocationFound();
}
